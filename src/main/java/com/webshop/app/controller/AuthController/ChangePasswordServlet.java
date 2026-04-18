package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/account/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("pageTitle", "MyCosmetic | Đổi mật khẩu");
        req.setAttribute("pageCss", "login.css");
        req.setAttribute("pageContent", "/jsp/auth/change_password.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        String error = null;

        if (oldPass == null || oldPass.isBlank()) {
            error = "Vui lòng nhập mật khẩu hiện tại";
        } else if (newPass == null || newPass.length() < 6) {
            error = "Mật khẩu mới phải ≥ 6 ký tự";
        } else if (!newPass.equals(confirm)) {
            error = "Mật khẩu xác nhận không khớp";
        } else if (!userDAO.checkPassword(user.getId(), oldPass)) {
            error = "Mật khẩu hiện tại không đúng";
        }

        if (error == null) {
            userDAO.updatePassword(user.getId(), newPass);
            req.setAttribute("success", "Đổi mật khẩu thành công");
        } else {
            req.setAttribute("error", error);
        }

        req.setAttribute("pageTitle", "MyCosmetic | Đổi mật khẩu");
        req.setAttribute("pageCss", req.getContextPath() + "/assets/css/login.css");
        req.setAttribute("pageContent", "/jsp/auth/change_password.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
