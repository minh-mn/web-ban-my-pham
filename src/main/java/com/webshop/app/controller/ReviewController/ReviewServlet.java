package com.webshop.app.controller.ReviewController;

import com.webshop.app.dao.ReviewSubmitDAO;
import com.webshop.app.model.ReviewMedia;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/orders/review/submit", "/review"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 50L * 1024 * 1024,
        maxRequestSize = 90L * 1024 * 1024
)
public class ReviewServlet extends HttpServlet {

    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50L * 1024 * 1024;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/webp"
    };

    private static final String[] ALLOWED_VIDEO_TYPES = {
            "video/mp4", "video/webm"
    };

    private final ReviewSubmitDAO reviewSubmitDAO = new ReviewSubmitDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long orderId = parseLong(request.getParameter("orderId"), 0L);
        long productId = parseLong(request.getParameter("productId"), 0L);
        Long orderItemId = parseNullableLong(request.getParameter("orderItemId"));

        int rating = parseInt(request.getParameter("rating"), 5);
        rating = Math.max(1, Math.min(5, rating));

        String comment = clean(request.getParameter("comment"));
        boolean anonymous = "1".equals(request.getParameter("anonymous")) || "on".equalsIgnoreCase(request.getParameter("anonymous"));

        int sellerServiceRating = clampRating(parseInt(request.getParameter("sellerServiceRating"), 5));
        int deliverySpeedRating = clampRating(parseInt(request.getParameter("deliverySpeedRating"), 5));
        int shipperRating = clampRating(parseInt(request.getParameter("shipperRating"), 5));
        String serviceComment = clean(request.getParameter("serviceComment"));
        String serviceTags = joinParameterValues(request.getParameterValues("serviceTags"));

        String backToForm = request.getContextPath() + "/orders/review?orderId=" + orderId + "&productId=" + productId;
        if (orderItemId != null) {
            backToForm += "&orderItemId=" + orderItemId;
        }

        if (orderId <= 0 || productId <= 0) {
            redirectWithError(response, backToForm, "Thiếu thông tin đơn hàng hoặc sản phẩm.");
            return;
        }

        if (comment.length() < 10) {
            redirectWithError(response, backToForm, "Vui lòng nhập nội dung đánh giá ít nhất 10 ký tự.");
            return;
        }

        if (!reviewSubmitDAO.canReview(user.getId(), orderId, productId, orderItemId)) {
            redirectWithError(response, request.getContextPath() + "/orders/detail?id=" + orderId,
                    "Bạn chỉ có thể đánh giá sản phẩm trong đơn hàng đã giao thành công.");
            return;
        }

        if (reviewSubmitDAO.hasReviewedOrderItem(user.getId(), orderId, productId, orderItemId)) {
            redirectWithError(response, request.getContextPath() + "/orders/detail?id=" + orderId,
                    "Bạn đã đánh giá sản phẩm này trong đơn hàng rồi.");
            return;
        }

        List<ReviewMedia> mediaList;
        try {
            mediaList = saveReviewMedia(request);
        } catch (IllegalArgumentException e) {
            redirectWithError(response, backToForm, e.getMessage());
            return;
        }

        String firstImageUrl = mediaList.stream()
                .filter(ReviewMedia::isImage)
                .map(ReviewMedia::getMediaUrl)
                .findFirst()
                .orElse(null);

        String firstVideoUrl = mediaList.stream()
                .filter(ReviewMedia::isVideo)
                .map(ReviewMedia::getMediaUrl)
                .findFirst()
                .orElse(null);

        int rewardPoints = calculatePotentialRewardPoints(comment, !mediaList.isEmpty());

        ReviewSubmitDAO.CreateReviewRequest createRequest = new ReviewSubmitDAO.CreateReviewRequest(
                user.getId(),
                orderId,
                productId,
                orderItemId,
                rating,
                comment,
                anonymous,
                rewardPoints,
                sellerServiceRating,
                deliverySpeedRating,
                shipperRating,
                serviceTags,
                serviceComment,
                firstImageUrl,
                firstVideoUrl,
                mediaList
        );

        reviewSubmitDAO.createReview(createRequest);

        request.getSession().setAttribute("successMessage",
                "Đánh giá của bạn đã được gửi và đang chờ quản trị viên duyệt. Xu sẽ được cộng sau khi đánh giá được duyệt.");
        response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId);
    }

    private List<ReviewMedia> saveReviewMedia(HttpServletRequest request) throws IOException, ServletException {
        List<ReviewMedia> result = new ArrayList<>();

        Collection<Part> imageParts = request.getParts()
                .stream()
                .filter(part -> "reviewImages".equals(part.getName()) && part.getSize() > 0)
                .toList();

        if (imageParts.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Bạn chỉ được upload tối đa 5 ảnh cho mỗi đánh giá.");
        }

        for (Part part : imageParts) {
            validatePart(part, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "Ảnh", "JPG, PNG hoặc WEBP");
            result.add(savePart(part, "IMAGE"));
        }

        Part videoPart = getPartQuietly(request, "reviewVideo");
        if (videoPart != null && videoPart.getSize() > 0) {
            validatePart(videoPart, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "Video", "MP4 hoặc WEBM");
            result.add(savePart(videoPart, "VIDEO"));
        }

        return result;
    }

    private ReviewMedia savePart(Part part, String mediaType) throws IOException {
        String originalName = extractFileName(part);
        String extension = getExtension(originalName);
        String safeName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        Path targetDir = getUploadDir(mediaType);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(safeName);
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        ReviewMedia media = new ReviewMedia();
        media.setMediaType(mediaType);
        media.setMediaUrl("/uploads/review/" + ("IMAGE".equals(mediaType) ? "image" : "video") + "/" + safeName);
        media.setOriginalName(originalName);
        media.setFileSize(part.getSize());
        media.setMimeType(part.getContentType());
        return media;
    }

    private Path getUploadDir(String mediaType) {
        String configuredDir = System.getProperty("mycosmetic.upload.dir");

        Path baseDir;
        if (configuredDir != null && !configuredDir.isBlank()) {
            baseDir = Paths.get(configuredDir.trim());
        } else {
            baseDir = Paths.get(System.getProperty("user.home"), "MyCosmeticShopUploads");
        }

        if ("IMAGE".equals(mediaType)) {
            return baseDir.resolve("review").resolve("image");
        }
        return baseDir.resolve("review").resolve("video");
    }

    private void validatePart(Part part,
                              String[] allowedTypes,
                              long maxSize,
                              String label,
                              String allowedText) {
        String contentType = part.getContentType();
        boolean typeOk = false;

        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                typeOk = true;
                break;
            }
        }

        if (!typeOk) {
            throw new IllegalArgumentException(label + " chỉ hỗ trợ định dạng " + allowedText + ".");
        }

        if (part.getSize() > maxSize) {
            throw new IllegalArgumentException(label + " vượt quá dung lượng cho phép. Dung lượng tối đa là " + formatSize(maxSize) + ".");
        }
    }

    private Part getPartQuietly(HttpServletRequest request, String name) throws IOException, ServletException {
        try {
            return request.getPart(name);
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    private int calculatePotentialRewardPoints(String comment, boolean hasMedia) {
        int points = 0;

        if (comment != null && comment.trim().length() >= 50) {
            points += 200;
        }

        if (hasMedia) {
            points += 200;
        }

        // 200 xu chỉ được cộng thực tế khi admin duyệt, nhưng lưu vào reward_points để thể hiện tổng có thể nhận.
        points += 200;

        return Math.min(points, 600);
    }

    private static String extractFileName(Part part) {
        String submittedFileName = part.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.isBlank()) {
            return "upload";
        }
        return Paths.get(submittedFileName).getFileName().toString();
    }

    private static String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);

        int dot = normalized.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }

        String extension = normalized.substring(dot);
        return extension.replaceAll("[^a-z0-9.]", "");
    }

    private static int clampRating(int value) {
        return Math.max(1, Math.min(5, value));
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String joinParameterValues(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return java.util.Arrays.stream(values)
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private static String formatSize(long bytes) {
        return String.format(Locale.US, "%.1fMB", bytes / 1024.0 / 1024.0);
    }

    private void redirectWithError(HttpServletResponse response, String url, String message) throws IOException {
        response.sendRedirect(url + (url.contains("?") ? "&" : "?") + "error=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
    }
}
