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

        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;

        if (comment == null) comment = "";
        comment = comment.trim();

        // =========================================================
        // ✅ FIX CHÍNH:
        // 1) ƯU TIÊN DÙNG authorId TRONG SESSION (user.getId()).
        // 2) KHÔNG BAO GIỜ set lại session user id theo "resolver" (tránh nhảy admin).
        // =========================================================
        int authorId = user.getId();

        // Nếu id trong session hợp lệ và tồn tại => dùng luôn
        if (authorId > 0 && existsAuthUserId(authorId)) {
            // ok
        } else {
            // ===== OPTIONAL FALLBACK (chỉ để tránh FK) =====
            // Nếu bạn muốn an toàn tuyệt đối không nhảy role/user,
            // thì thay đoạn fallback này bằng: redirect login.
            Integer resolved = resolveAuthUserIdNoSessionOverwrite(user);

            if (resolved == null || resolved <= 0) {
                // Không resolve được => bắt login lại (đúng chuẩn)
                if (session != null) session.removeAttribute("user");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // ✅ dùng resolved authorId cho lần review này
            // ❌ KHÔNG set user.setId(...) và KHÔNG set session lại
            authorId = resolved;
        }

        // ===== BUILD REVIEW =====
        Review r = new Review();
        r.setProductId(productId);
        r.setAuthorId(authorId);
        r.setRating(rating);
        r.setComment(comment);

        r.setHasEmoji(false);
        r.setSentiment(1);

        // ✅ chống duplicate: có thì UPDATE, chưa có thì INSERT
        reviewDAO.createOrUpdate(r);

        // ✅ redirect về đúng trang detail theo slug: /product/{slug}
        if (slug != null && !slug.isBlank()) {
            String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/product/" + encodedSlug);
        } else {
            resp.sendRedirect(req.getContextPath() + "/products");
        }
    }

    // =========================================================
    // RESOLVE (NO SESSION OVERWRITE)
    // Chỉ dùng khi user.getId() không hợp lệ.
    // Tuyệt đối KHÔNG set lại session user tại đây.
    // =========================================================
    private Integer resolveAuthUserIdNoSessionOverwrite(User user) {

        // 1) Try by username
        String username = safeString(getUsernameSafe(user));
        if (!username.isEmpty()) {
            Integer found = findAuthUserIdByUsername(username);
            if (found != null && found > 0) return found;
        }

        // 2) Try by email (nếu có)
        String email = safeString(getEmailSafe(user));
        if (!email.isEmpty()) {
            Integer found = findAuthUserIdByEmail(email);
            if (found != null && found > 0) return found;
        }

        return null;
    }

    private boolean existsAuthUserId(int id) {
        String sql = "SELECT 1 FROM dbo.auth_user WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    private Integer findAuthUserIdByUsername(String username) {
        // Nếu DB bạn không có cột username => đổi lại ở đây (vd: user_name)
        String sql = "SELECT id FROM dbo.auth_user WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    private Integer findAuthUserIdByEmail(String email) {
        String sql = "SELECT id FROM dbo.auth_user WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt(1);
            }

        } catch (Exception e) {
            return null;
        }
    }

    // ===== helpers =====
    private int parseIntOrDefault(String s, int def) {
        try {
            if (s == null || s.isBlank()) return def;
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
