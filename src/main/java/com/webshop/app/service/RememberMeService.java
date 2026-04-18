package com.webshop.app.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import com.webshop.app.dao.RememberTokenDAO;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RememberMeService {

    public static final String COOKIE_NAME = "REMEMBER_ME";

    private static final int COOKIE_AGE_SECONDS = 60 * 60 * 24 * 14; // 14 ngày
    private static final int TOKEN_BYTES = 32;

    private final RememberTokenDAO rememberTokenDAO = new RememberTokenDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom random = new SecureRandom();

    /**
     * Token -> User (an toàn, không làm vỡ request)
     */
    public User authenticateByToken(String token) {
        if (token == null || token.isBlank()) return null;

        try {
            Integer userId = rememberTokenDAO.findUserIdByToken(token);
            if (userId == null || userId <= 0) return null;

            User user = userDAO.findById(userId);

            // ✅ Guard: user phải tồn tại và user.id phải đúng users.id
            if (user == null) return null;
            if (user.getId() <= 0 || user.getId() != userId) {
                System.out.println("[RememberMeService] INVALID user mapping. tokenUserId="
                        + userId + ", loadedUserId=" + user.getId());
                return null;
            }

            if (!user.isActive()) return null;

            return user;

        } catch (Exception e) {
            System.out.println("[RememberMeService] authenticateByToken error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tạo token ngẫu nhiên (URL-safe) + lưu DB (expires = now + 14 ngày)
     */
    public String issueToken(int userId) {
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(COOKIE_AGE_SECONDS);

        rememberTokenDAO.saveToken(userId, token, expiresAt);
        return token;
    }

    /**
     * Dùng khi login có tick "remember me":
     * - tạo token
     * - set cookie
     *
     * ✅ Nên truyền req để set cookie path theo contextPath.
     */
    public void rememberLogin(HttpServletRequest req, HttpServletResponse resp, int userId) {
        String token = issueToken(userId);
        setCookie(req, resp, token);
    }

    /**
     * Backward compatible: nếu bạn chưa muốn sửa chỗ gọi, vẫn dùng được,
     * nhưng cookie sẽ set path="/".
     */
    public void rememberLogin(HttpServletResponse resp, int userId) {
        String token = issueToken(userId);
        setCookie(resp, token);
    }

    /**
     * Logout chuẩn:
     * - revoke token trong DB (nếu có)
     * - clear cookie (cả "/" và contextPath)
     */
    public void logoutByRequest(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String token = getCookieValue(req, COOKIE_NAME);
            if (token != null && !token.isBlank()) {
                rememberTokenDAO.revokeToken(token);
            }
        } catch (Exception e) {
            System.out.println("[RememberMeService] logoutByRequest error: " + e.getMessage());
        } finally {
            clearCookie(resp, req.getContextPath());
        }
    }

    /**
     * Khi token không hợp lệ:
     * - revoke token DB
     * - clear cookie
     */
    public void invalidateToken(HttpServletRequest req, HttpServletResponse resp, String token) {
        try {
            if (token != null && !token.isBlank()) {
                rememberTokenDAO.revokeToken(token);
            }
        } catch (Exception e) {
            System.out.println("[RememberMeService] invalidateToken error: " + e.getMessage());
        } finally {
            clearCookie(resp, req.getContextPath());
        }
    }

    // ================= COOKIE =================

    /**
     * Clear cookie chắc chắn: clear cả "/" và contextPath.
     */
    public void clearCookie(HttpServletResponse resp, String contextPath) {
        // clear "/"
        Cookie c1 = new Cookie(COOKIE_NAME, "");
        c1.setPath("/");
        c1.setMaxAge(0);
        c1.setHttpOnly(true);
        resp.addCookie(c1);

        // clear contextPath
        Cookie c2 = new Cookie(COOKIE_NAME, "");
        c2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        c2.setMaxAge(0);
        c2.setHttpOnly(true);
        resp.addCookie(c2);
    }

    /**
     * Giữ hàm cũ để tương thích.
     */
    public void clearCookie(HttpServletResponse resp) {
        Cookie c = new Cookie(COOKIE_NAME, "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    /**
     * Set cookie theo contextPath (khuyến nghị).
     */
    public void setCookie(HttpServletRequest req, HttpServletResponse resp, String token) {
        String path = (req.getContextPath() == null || req.getContextPath().isEmpty())
                ? "/" : req.getContextPath();

        Cookie c = new Cookie(COOKIE_NAME, token);
        c.setPath(path);
        c.setMaxAge(COOKIE_AGE_SECONDS);
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    /**
     * Giữ hàm cũ để tương thích (path="/").
     */
    public void setCookie(HttpServletResponse resp, String token) {
        Cookie c = new Cookie(COOKIE_NAME, token);
        c.setPath("/");
        c.setMaxAge(COOKIE_AGE_SECONDS);
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    // ================= INTERNAL =================

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
