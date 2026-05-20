package com.webshop.app.controller.ReviewController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/review")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ===== READ PARAMS =====
        String productIdRaw = req.getParameter("productId");
        String slug = req.getParameter("slug");
        String ratingRaw = req.getParameter("rating");
        String comment = req.getParameter("comment");

        int productId = parseIntOrDefault(productIdRaw, -1);
        int rating = parseIntOrDefault(ratingRaw, 5);

        if (productId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        if (rating < 1) {
            rating = 1;
        }

        if (rating > 5) {
            rating = 5;
        }

        if (comment == null) {
            comment = "";
        }

        comment = comment.trim();

        // =========================================================
        // FIX:
        // 1) Ưu tiên dùng authorId trong session: user.getId().
        // 2) Không set lại session user id theo resolver để tránh nhảy nhầm user/admin.
        // 3) Database hiện tại dùng bảng users, không dùng dbo.auth_user.
        // =========================================================
        int authorId = user.getId();

        // Nếu id trong session hợp lệ và tồn tại trong bảng users thì dùng luôn
        if (authorId > 0 && existsUserId(authorId)) {
            // OK
        } else {
            // Fallback chỉ dùng để tránh lỗi khóa ngoại author_id
            Integer resolved = resolveUserIdNoSessionOverwrite(user);

            if (resolved == null || resolved <= 0) {
                // Không resolve được user hợp lệ thì bắt đăng nhập lại
                if (session != null) {
                    session.removeAttribute("user");
                }

                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // Dùng resolved authorId cho lần review này
            // Không update lại session user
            authorId = resolved;
        }

        // ===== BUILD REVIEW =====
        Review review = new Review();
        review.setProductId(productId);
        review.setAuthorId(authorId);
        review.setRating(rating);
        review.setComment(comment);

        review.setHasEmoji(false);
        review.setSentiment(1);

        // Chống duplicate: có thì UPDATE, chưa có thì INSERT
        reviewDAO.createOrUpdate(review);

        // Redirect về đúng trang chi tiết sản phẩm: /product/{slug}
        if (slug != null && !slug.isBlank()) {
            String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/product/" + encodedSlug);
        } else {
            resp.sendRedirect(req.getContextPath() + "/products");
        }
    }

    // =========================================================
    // RESOLVE USER ID - NO SESSION OVERWRITE
    // Chỉ dùng khi user.getId() không hợp lệ.
    // Tuyệt đối không set lại session user tại đây.
    // =========================================================
    private Integer resolveUserIdNoSessionOverwrite(User user) {

        // 1) Tìm theo username
        String username = safeString(getUsernameSafe(user));

        if (!username.isEmpty()) {
            Integer found = findUserIdByUsername(username);

            if (found != null && found > 0) {
                return found;
            }
        }

        // 2) Tìm theo email
        String email = safeString(getEmailSafe(user));

        if (!email.isEmpty()) {
            Integer found = findUserIdByEmail(email);

            if (found != null && found > 0) {
                return found;
            }
        }

        return null;
    }

    private boolean existsUserId(int id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    private Integer findUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    private Integer findUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    // ===== HELPERS =====
    private int parseIntOrDefault(String s, int def) {
        try {
            if (s == null || s.isBlank()) {
                return def;
            }

            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private String safeString(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String getUsernameSafe(User user) {
        try {
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private String getEmailSafe(User user) {
        try {
            return user.getEmail();
        } catch (Exception e) {
            return null;
        }
    }
}