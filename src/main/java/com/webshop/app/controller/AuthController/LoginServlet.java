package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.service.RememberMeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/account");
            return;
        }

        req.setAttribute("pageTitle", "Đăng nhập");
        req.setAttribute("pageContent", "/jsp/auth/login.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // Checkbox name="remember"
        String remember = req.getParameter("remember");

        // Redirect back after login, ví dụ: /checkout
        String redirect = req.getParameter("redirect");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            req.setAttribute("pageTitle", "Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.login(username.trim(), password);

        if (user == null) {
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
            req.setAttribute("pageTitle", "MyCosmetic | Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // user.id phải tồn tại trong bảng users
        if (user.getId() <= 0) {
            req.setAttribute("error", "Tài khoản không hợp lệ, không tìm thấy users.id.");
            req.setAttribute("pageTitle", "MyCosmetic | Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // ===== SET SESSION =====
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        // ===== REMEMBER ME - NEW VERSION =====
        // Cơ chế mới dùng bảng remember_tokens và cookie REMEMBER_ME
        boolean rememberMe = "on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember);

        if (rememberMe) {
            rememberMeService.rememberLogin(resp, user.getId());
        } else {
            rememberMeService.clearCookie(resp);
        }

        // ===== CLEAR LEGACY COOKIE ONLY =====
        // Không còn dùng bảng user_tokens.
        // Chỉ xóa cookie cũ REMEMBER_TOKEN nếu trình duyệt còn lưu.
        clearLegacyRememberToken(resp, req.getContextPath());

        // ===== REDIRECT =====
        String ctx = req.getContextPath();

        if (redirect != null && !redirect.isBlank()) {
            // Chặn open redirect: chỉ cho redirect nội bộ
            if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                resp.sendRedirect(ctx + redirect);
                return;
            }
        }

        resp.sendRedirect(ctx + "/");
    }

    private void clearLegacyRememberToken(HttpServletResponse resp, String contextPath) {
        // Cookie cũ có thể từng được set path = "/"
        Cookie ck = new Cookie("REMEMBER_TOKEN", "");
        ck.setHttpOnly(true);
        ck.setMaxAge(0);
        ck.setPath("/");
        resp.addCookie(ck);

        // Cookie cũ cũng có thể từng được set path = contextPath
        Cookie ck2 = new Cookie("REMEMBER_TOKEN", "");
        ck2.setHttpOnly(true);
        ck2.setMaxAge(0);
        ck2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(ck2);
    }
}