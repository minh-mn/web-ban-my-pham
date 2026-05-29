package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.PolicyDAO;
import com.webshop.app.model.Policy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/policy/upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 20 * 1024 * 1024,
        maxRequestSize = 30 * 1024 * 1024
)
public class PolicyUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf",
            "png",
            "jpg",
            "jpeg",
            "webp",
            "gif"
    );

    private final PolicyDAO policyDAO = new PolicyDAO();

    @Override
    public void init() throws ServletException {
        super.init();
        UploadConfig.ensureUploadDirectories();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        /*
         * Nếu file đã được copy vào MyCosmeticShopUploads/policy
         * nhưng insert SQL bị lỗi, dùng biến này để xóa lại file rác.
         */
        String savedFileNameForRollback = null;

        try {
            String title = trim(request.getParameter("title"));
            String slug = trim(request.getParameter("slug"));

            if (title.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập tiêu đề chính sách.");
            }

            if (slug.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập slug chính sách.");
            }

            Part filePart = request.getPart("file");

            if (filePart == null || filePart.getSize() <= 0) {
                throw new IllegalArgumentException("Vui lòng chọn file chính sách.");
            }

            String originalFileName = getSubmittedFileName(filePart);

            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException("Tên file upload không hợp lệ.");
            }

            String extension = getExtensionLower(originalFileName);

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("File không hợp lệ. Chỉ chấp nhận PDF, PNG, JPG, JPEG, WEBP, GIF.");
            }

            validateContentType(filePart, extension);

            Files.createDirectories(UploadConfig.POLICY_DIR);

            String savedFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            Path policyDir = UploadConfig.POLICY_DIR.toAbsolutePath().normalize();
            Path destination = UploadConfig.resolvePolicyFile(savedFileName)
                    .toAbsolutePath()
                    .normalize();

            if (!destination.startsWith(policyDir)) {
                throw new IllegalArgumentException("Đường dẫn upload chính sách không hợp lệ.");
            }

            try (InputStream inputStream = filePart.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            /*
             * Từ thời điểm này, file đã tồn tại vật lý.
             * Nếu insert SQL lỗi thì cần xóa lại file này.
             */
            savedFileNameForRollback = savedFileName;

            Policy policy = new Policy();
            policy.setTitle(title);
            policy.setSlug(slug);

            /*
             * Database chỉ lưu tên file.
             *
             * File thật:
             * MyCosmeticShopUploads/policy/{savedFileName}
             *
             * URL public khi hiển thị:
             * /uploads/policy/{savedFileName}
             */
            policy.setFileName(savedFileName);

            /*
             * Insert SQL.
             * Nếu lỗi ở đây, catch sẽ xóa file vật lý vừa upload.
             */
            policyDAO.insert(policy);

            /*
             * SQL thành công thì không rollback file nữa.
             */
            savedFileNameForRollback = null;

            response.sendRedirect(request.getContextPath() + "/admin/policy/list");

        } catch (IllegalArgumentException ex) {
            rollbackPolicyUpload(savedFileNameForRollback);

            String error = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/policy/list?error=" + error);

        } catch (Exception ex) {
            rollbackPolicyUpload(savedFileNameForRollback);

            throw new ServletException("PolicyUploadServlet upload error", ex);
        }
    }

    private void rollbackPolicyUpload(String savedFileName) {
        if (savedFileName == null || savedFileName.isBlank()) {
            return;
        }

        /*
         * Chỉ xóa trong MyCosmeticShopUploads/policy.
         */
        UploadConfig.deletePolicyFile(savedFileName);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String getSubmittedFileName(Part part) {
        if (part == null) {
            return null;
        }

        String submittedFileName = part.getSubmittedFileName();

        if (submittedFileName == null || submittedFileName.isBlank()) {
            return null;
        }

        return Paths.get(submittedFileName)
                .getFileName()
                .toString();
    }

    private static String getExtensionLower(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int dot = fileName.lastIndexOf('.');

        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static void validateContentType(Part part, String extension) {
        String contentType = part.getContentType();

        if (contentType == null || contentType.isBlank()) {
            return;
        }

        String lowerContentType = contentType.toLowerCase(Locale.ROOT);

        if ("pdf".equals(extension)) {
            if (!"application/pdf".equals(lowerContentType)) {
                throw new IllegalArgumentException("File PDF không đúng định dạng.");
            }

            return;
        }

        if (!lowerContentType.startsWith("image/")) {
            throw new IllegalArgumentException("File ảnh không đúng định dạng.");
        }
    }
}