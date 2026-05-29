package com.webshop.app.controller.AuthController;

import java.io.IOException;

import com.webshop.app.service.ForgotPasswordService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final ForgotPasswordService forgotPasswordService = new ForgotPasswordService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Flash message (hiển thị 1 lần)
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object msg = session.getAttribute("flash_message");
            Object err = session.getAttribute("flash_error");
            if (msg != null) req.setAttribute("message", msg);
            if (err != null) req.setAttribute("error", err);

            session.removeAttribute("flash_message");
            session.removeAttribute("flash_error");
        }

        render(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");

        HttpSession session = req.getSession(true);

        try {
            forgotPasswordService.requestReset(email);
            session.setAttribute("flash_message",
                    "Chúng tôi đã gửi email hướng dẫn đặt lại mật khẩu cho bạn.");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Không thể gửi email lúc này. Vui lòng thử lại sau.");
        }

        resp.sendRedirect(req.getContextPath() + "/forgot-password");
    }

    private void render(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("pageTitle", "MyCosmetic | Quên mật khẩu");
        req.setAttribute("pageCss", "login.css");
        req.setAttribute("pageContent", "/jsp/auth/forgot_password.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
