package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.AdminReviewDAO;
import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AdminReviewDAO reviewDAO = new AdminReviewDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safeText(req.getParameter("action"));
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "detail" -> showDetail(req, resp);
            case "list" -> showList(req, resp);
            default -> resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safeText(req.getParameter("action"));
        if (action == null) {
            action = "delete";
        }

        long id = safeLong(req.getParameter("id"), -1);
        int adminId = currentAdminId(req);
        String adminNote = safeText(req.getParameter("adminNote"));

        boolean success = false;
        String message = null;

        if (id <= 0) {
            message = "Không tìm thấy ID bình luận cần xử lý.";
            redirectBack(req, resp, false, message);
            return;
        }

        try {
            Review currentReview = reviewDAO.findById(id);

            switch (action) {
                case "approve" -> {
                    success = reviewDAO.approve(id, adminId, adminNote);
                    message = success ? "Đã duyệt bình luận thành công." : "Không thể duyệt bình luận.";
                }
                case "reject" -> {
                    success = reviewDAO.reject(id, adminId, adminNote);
                    message = success ? "Đã từ chối bình luận thành công." : "Không thể từ chối bình luận.";
                }
                case "hide" -> {
                    success = reviewDAO.hide(id);
                    message = success ? "Đã ẩn bình luận thành công." : "Không thể ẩn bình luận.";
                }
                case "unhide" -> {
                    success = reviewDAO.unhide(id);
                    message = success ? "Đã hiện lại bình luận thành công." : "Không thể hiện lại bình luận.";
                }
                case "delete" -> {
                    success = reviewDAO.delete(id);
                    message = success ? "Đã xóa bình luận thành công." : "Không thể xóa bình luận.";
                }
                default -> {
                    success = false;
                    message = "Thao tác không hợp lệ.";
                }
            }

            if (success) {
                notifyReviewModerationSafely(
                        currentReview,
                        action,
                        adminNote
                );
            }
        } catch (Exception e) {
            success = false;
            message = "Lỗi xử lý bình luận: " + e.getMessage();
        }

        redirectBack(req, resp, success, message);
    }

    /* =========================================================
       NOTIFICATION - ISSUE 114
    ========================================================= */

    private void notifyReviewModerationSafely(Review review,
                                              String action,
                                              String adminNote) {
        if (review == null || review.getId() <= 0 || review.getAuthorId() <= 0) {
            return;
        }

        ReviewNotificationPayload payload = buildReviewNotificationPayload(
                review,
                action,
                adminNote
        );

        if (payload == null) {
            return;
        }

        try {
            notificationDAO.createUserNotification(
                    review.getAuthorId(),
                    payload.type(),
                    payload.title(),
                    payload.message(),
                    payload.targetUrl(),
                    "REVIEW",
                    (long) review.getId()
            );
        } catch (Exception e) {
            /*
             * Không để lỗi notification làm hỏng thao tác quản lý đánh giá.
             */
            e.printStackTrace();
        }
    }

    private ReviewNotificationPayload buildReviewNotificationPayload(Review review,
                                                                     String action,
                                                                     String adminNote) {
        String safeAction = action == null ? "" : action.trim().toLowerCase();
        String productName = safeText(review.getProductName());

        if (productName == null) {
            productName = "sản phẩm #" + review.getProductId();
        }

        String targetUrl = review.getOrderId() != null && review.getOrderId() > 0
                ? "/orders/detail?id=" + review.getOrderId()
                : "/notifications";

        return switch (safeAction) {
            case "approve" -> new ReviewNotificationPayload(
                    "REVIEW_APPROVED",
                    "Đánh giá đã được duyệt",
                    "Đánh giá của bạn cho sản phẩm \"" + productName + "\" đã được duyệt và hiển thị trên hệ thống.",
                    targetUrl
            );

            case "reject" -> new ReviewNotificationPayload(
                    "REVIEW_REJECTED",
                    "Đánh giá bị từ chối",
                    "Đánh giá của bạn cho sản phẩm \"" + productName + "\" đã bị từ chối."
                            + buildAdminNoteText(adminNote),
                    targetUrl
            );

            case "hide" -> new ReviewNotificationPayload(
                    "REVIEW_HIDDEN",
                    "Đánh giá bị ẩn",
                    "Đánh giá của bạn cho sản phẩm \"" + productName + "\" đã bị ẩn."
                            + buildAdminNoteText(adminNote),
                    targetUrl
            );

            case "unhide" -> new ReviewNotificationPayload(
                    "REVIEW_APPROVED",
                    "Đánh giá đã được hiển thị lại",
                    "Đánh giá của bạn cho sản phẩm \"" + productName + "\" đã được hiển thị lại trên hệ thống.",
                    targetUrl
            );

            default -> null;
        };
    }

    private String buildAdminNoteText(String adminNote) {
        String note = safeText(adminNote);

        if (note == null) {
            return "";
        }

        return " Lý do: " + note;
    }

    private record ReviewNotificationPayload(
            String type,
            String title,
            String message,
            String targetUrl
    ) {
    }



    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer rating = nullableInt(req.getParameter("rating"));
        Long productId = nullableLong(req.getParameter("productId"));
        Long authorId = nullableLong(req.getParameter("authorId"));
        String status = safeText(req.getParameter("status"));
        String mediaType = safeText(req.getParameter("media"));
        String keyword = safeText(req.getParameter("keyword"));

        List<Review> reviews = reviewDAO.search(rating, productId, authorId, status, mediaType, keyword);

        req.setAttribute("reviews", reviews);
        req.setAttribute("totalReviews", reviews.size());

        req.setAttribute("rating", rating);
        req.setAttribute("productId", productId);
        req.setAttribute("authorId", authorId);
        req.setAttribute("status", status);
        req.setAttribute("media", mediaType);
        req.setAttribute("keyword", keyword);

        moveFlashMessage(req);

        req.getRequestDispatcher("/jsp/admin/review/review_list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long id = safeLong(req.getParameter("id"), -1);
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + urlEncode("ID bình luận không hợp lệ."));
            return;
        }

        Review review = reviewDAO.findById(id);
        if (review == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + urlEncode("Không tìm thấy bình luận."));
            return;
        }

        req.setAttribute("review", review);
        moveFlashMessage(req);

        req.getRequestDispatcher("/jsp/admin/review/review_detail.jsp").forward(req, resp);
    }

    private int currentAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return 0;
        }

        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof User user)) {
            userObject = session.getAttribute("authUser");
        }
        if (!(userObject instanceof User user)) {
            userObject = session.getAttribute("currentUser");
        }

        if (userObject instanceof User user) {
            return Math.max(user.getId(), 0);
        }

        return 0;
    }

    private void redirectBack(HttpServletRequest req, HttpServletResponse resp, boolean success, String message)
            throws IOException {

        HttpSession session = req.getSession();
        session.setAttribute(success ? "successMessage" : "errorMessage", message);

        String referer = req.getHeader("Referer");
        String contextPath = req.getContextPath();

        if (referer != null && referer.contains(contextPath + "/admin/reviews")) {
            resp.sendRedirect(resp.encodeRedirectURL(referer));
            return;
        }

        resp.sendRedirect(resp.encodeRedirectURL(contextPath + "/admin/reviews"));
    }

    private void moveFlashMessage(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }

        Object successMessage = session.getAttribute("successMessage");
        Object errorMessage = session.getAttribute("errorMessage");

        if (successMessage != null) {
            req.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
    }

    private long safeLong(String value, long defaultValue) {
        try {
            if (value == null) {
                return defaultValue;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return defaultValue;
            }

            return Long.parseLong(trimmed);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Integer nullableInt(String value) {
        try {
            if (value == null) {
                return null;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return null;
            }

            int parsed = Integer.parseInt(trimmed);
            return parsed >= 1 && parsed <= 5 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long nullableLong(String value) {
        try {
            if (value == null) {
                return null;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return null;
            }

            long parsed = Long.parseLong(trimmed);
            return parsed > 0 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
