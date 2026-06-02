package com.webshop.app.controller.AuthController;

import com.webshop.app.model.User;
import com.webshop.app.utils.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Random;

@WebServlet("/resend-otp")
public class ResendOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User pendingUser = (User) session.getAttribute("pendingUser");

        if (pendingUser == null) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Phiên đăng ký đã hết hạn. Vui lòng điền lại form!\"}");
            return;
        }

        // Tạo mã OTP mới 6 số
        String newOtp = String.format("%06d", new Random().nextInt(999999));
        session.setAttribute("REGISTER_OTP", newOtp);
        session.setAttribute("OTP_TIME", System.currentTimeMillis());

        try {
            String subject = "Mã xác thực mới - MyCosmetic";
            String content = "<h2>Xác thực tài khoản</h2>"
                    + "<p>Mã OTP mới của bạn là: <b style='font-size: 20px; color: #d81b60;'>" + newOtp + "</b></p>";

            EmailUtil.sendHtml(pendingUser.getEmail(), subject, content);
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"Mã OTP mới đã được gửi lại thành công!\"}");
        } catch (Exception e) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi gửi mail: " + e.getMessage() + "\"}");
        }
    }
}