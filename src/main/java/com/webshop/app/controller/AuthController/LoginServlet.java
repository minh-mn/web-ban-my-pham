package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.service.RememberMeService;
import com.webshop.app.utils.CartUtil;

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
        String remember = req.getParameter("remember");
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

        if (user.getId() <= 0) {
            req.setAttribute("error", "Tài khoản không hợp lệ, không tìm thấy users.id.");
            req.setAttribute("pageTitle", "MyCosmetic | Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        /*
         * Issue 132:
         * Sau khi đăng nhập thành công, gộp giỏ hàng đã lưu trong database
         * với giỏ hàng hiện có trong session.
         */
        CartUtil.mergeDatabaseCartIntoSession(session, user.getId());

        boolean rememberMe = "on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember);

        if (rememberMe) {
            rememberMeService.rememberLogin(resp, user.getId());
        } else {
            rememberMeService.clearCookie(resp);
        }

        clearLegacyRememberToken(resp, req.getContextPath());

        String ctx = req.getContextPath();

        if (redirect != null && !redirect.isBlank()) {
            if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                resp.sendRedirect(ctx + redirect);
                return;
            }
        }

        resp.sendRedirect(ctx + "/");
    }

    private void clearLegacyRememberToken(HttpServletResponse resp, String contextPath) {
        Cookie ck = new Cookie("REMEMBER_TOKEN", "");
        ck.setHttpOnly(true);
        ck.setMaxAge(0);
        ck.setPath("/");
        resp.addCookie(ck);

        Cookie ck2 = new Cookie("REMEMBER_TOKEN", "");
        ck2.setHttpOnly(true);
        ck2.setMaxAge(0);
        ck2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(ck2);
    }
}
