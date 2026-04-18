package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.service.RememberMeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO(); // dùng để dọn legacy user_tokens
    private final RememberMeService rememberMeService = new RememberMeService(); // cơ chế mới remember_tokens

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================
        // 1) LOGOUT NEW REMEMBER-ME (remember_tokens + cookie REMEMBER_ME)
        // =====================
        // - revoke token DB nếu có
        // - clear cookie REMEMBER_ME
        rememberMeService.logoutByRequest(req, resp);

        // =====================
        // 2) CLEANUP LEGACY REMEMBER (user_tokens + cookie REMEMBER_TOKEN)
        // =====================
        String legacyToken = getCookieValue(req, "REMEMBER_TOKEN");
        if (legacyToken != null && !legacyToken.isBlank()) {
            try {
                userDAO.deleteRememberToken(legacyToken);
            } catch (Exception ignored) {
            }
        }
        clearCookie(resp, "REMEMBER_TOKEN", req.getContextPath());

        // =====================
        // 3) INVALIDATE SESSION
        // =====================
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * Xóa cookie chắc chắn: clear cả path "/" và contextPath (vì cookie cũ có thể set khác nhau)
     */
    private void clearCookie(HttpServletResponse resp, String name, String contextPath) {

        // clear path "/"
        Cookie c1 = new Cookie(name, "");
        c1.setHttpOnly(true);
        c1.setMaxAge(0);
        c1.setPath("/");
        resp.addCookie(c1);

        // clear path contextPath
        Cookie c2 = new Cookie(name, "");
        c2.setHttpOnly(true);
        c2.setMaxAge(0);
        c2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(c2);
    }
}
