package com.webshop.app.controller.ReviewController;

import com.webshop.app.dao.ReviewDAO;
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

@WebServlet(name = "ReviewServlet", urlPatterns = {"/orders/review/submit", "/review"})
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int productId = parseInt(request.getParameter("productId"), -1);
        int orderId = parseInt(request.getParameter("orderId"), -1);
        int orderItemId = parseInt(request.getParameter("orderItemId"), -1);
        int rating = parseInt(request.getParameter("rating"), 5);

        String comment = safeTrim(request.getParameter("comment"));
        String imageUrl = safeTrim(request.getParameter("imageUrl"));
        String videoUrl = safeTrim(request.getParameter("videoUrl"));
        String slug = safeTrim(request.getParameter("slug"));

        rating = Math.max(1, Math.min(5, rating));

        if (productId <= 0) {
            redirectWithError(request, response, "/products", "product_invalid");
            return;
        }

        if (comment == null || comment.length() < 5) {
            redirectBack(request, response, orderId, productId, slug, "comment_required");
            return;
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setAuthorId(user.getId());
        review.setRating(rating);
        review.setComment(comment);
        review.setHasEmoji(containsEmoji(comment));
        review.setSentiment(detectSimpleSentiment(comment));

        boolean canReview;
        try {
            if (orderId > 0 && orderItemId > 0) {
                canReview = reviewDAO.canUserReviewOrderItem(user.getId(), orderId, orderItemId, productId);
                review.setOrderId(orderId);
                review.setOrderItemId(orderItemId);
            } else {
                canReview = reviewDAO.canUserReviewProduct(user.getId(), productId);
                if (orderId > 0) {
                    review.setOrderId(orderId);
                }
            }
        } catch (RuntimeException ex) {
            throw new ServletException("Không thể kiểm tra điều kiện gửi đánh giá", ex);
        }

        if (!canReview) {
            redirectBack(request, response, orderId, productId, slug, "not_eligible");
            return;
        }

        try {
            reviewDAO.createOrUpdate(review, imageUrl, videoUrl);
        } catch (RuntimeException ex) {
            throw new ServletException("Không thể lưu đánh giá sản phẩm", ex);
        }

        if (orderId > 0) {
            response.sendRedirect(request.getContextPath()
                    + "/orders/detail?id=" + orderId
                    + "&success=review_pending");
            return;
        }

        if (slug != null && !slug.isBlank()) {
            String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.name());
            response.sendRedirect(request.getContextPath()
                    + "/product/" + encodedSlug
                    + "?success=review_pending#reviews");
        } else {
            response.sendRedirect(request.getContextPath() + "/products?success=review_pending");
        }
    }

    private void redirectBack(HttpServletRequest request,
                              HttpServletResponse response,
                              int orderId,
                              int productId,
                              String slug,
                              String error) throws IOException {
        if (orderId > 0) {
            response.sendRedirect(request.getContextPath()
                    + "/orders/review?orderId=" + orderId
                    + "&productId=" + productId
                    + "&error=" + encode(error));
            return;
        }

        if (slug != null && !slug.isBlank()) {
            String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.name());
            response.sendRedirect(request.getContextPath()
                    + "/product/" + encodedSlug
                    + "?error=" + encode(error)
                    + "#reviews");
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/products?error=" + encode(error));
        }
    }

    private void redirectWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String path,
                                   String error) throws IOException {
        response.sendRedirect(request.getContextPath() + path + "?error=" + encode(error));
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String safeTrim(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private boolean containsEmoji(String text) {
        if (text == null) {
            return false;
        }

        return text.codePoints().anyMatch(cp ->
                (cp >= 0x1F300 && cp <= 0x1FAFF)
                        || (cp >= 0x2600 && cp <= 0x27BF)
        );
    }

    private int detectSimpleSentiment(String comment) {
        if (comment == null) {
            return 1;
        }

        String value = comment.toLowerCase();
        String[] negativeWords = {
                "tệ", "xấu", "kém", "không tốt", "thất vọng",
                "lỗi", "hỏng", "dở", "khó chịu", "không hài lòng"
        };

        for (String word : negativeWords) {
            if (value.contains(word)) {
                return 0;
            }
        }
        return 1;
    }
}
