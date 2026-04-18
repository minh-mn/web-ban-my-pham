package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.model.User;
import com.webshop.app.service.ForgotPasswordService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String token = req.getParameter("token");
        User u = forgotPasswordService.validateTokenAndGetUser(token);

        if (u == null) {
            // token invalid -> JSP sẽ rơi vào nhánh empty token
            req.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            req.setAttribute("token", null);
            render(req, resp);
            return;
        }

        req.setAttribute("token", token);
        render(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String token = req.getParameter("token");
        String newPass = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        // 1) Validate token trước (tránh post token rác/hết hạn)
        User u = forgotPasswordService.validateTokenAndGetUser(token);
        if (u == null) {
            req.setAttribute("error", "Token không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu link mới.");
            req.setAttribute("token", null);
            render(req, resp);
            return;
        }

        // 2) Validate mật khẩu
        String np = (newPass != null) ? newPass.trim() : "";
        if (np.length() < 6) {
            req.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
            req.setAttribute("token", token);
            render(req, resp);
            return;
        }

        if (confirm == null || !np.equals(confirm)) {
            req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            req.setAttribute("token", token);
            render(req, resp);
            return;
        }

        try {
            forgotPasswordService.resetPassword(token, np);

            // ✅ Redirect về login + báo thành công (PRG)
            resp.sendRedirect(req.getContextPath() + "/login?reset=success");

        } catch (Exception e) {
            e.printStackTrace();

            req.setAttribute("error", "Không thể đặt lại mật khẩu lúc này. Vui lòng thử lại.");
            req.setAttribute("token", token);
            render(req, resp);
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("pageTitle", "MyCosmetic | Đặt lại mật khẩu");

        // ✅ Đồng bộ chuẩn auth (nếu base.jsp load CSS theo contextPath/assets/css/${pageCss})
        req.setAttribute("pageCss", "login.css");

        req.setAttribute("pageContent", "/jsp/auth/reset_password.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
