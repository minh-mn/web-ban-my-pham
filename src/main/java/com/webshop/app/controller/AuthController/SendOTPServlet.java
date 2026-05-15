package com.webshop.app.controller.AuthController;

import com.webshop.app.utils.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Random;

@WebServlet("/send-otp")
public class SendOTPServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        HttpSession session = request.getSession();

        // 1. CHỐNG SPAM OTP (60s)
        Long lastSent = (Long) session.getAttribute("LAST_OTP_SENT");
        long now = System.currentTimeMillis();

        if (lastSent != null && now - lastSent < 60000) {
            response.getWriter().write("Please wait 60s before requesting OTP again");
            return;
        }

        // 2. Tạo OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // 3. Lưu OTP + thời gian
        session.setAttribute("REGISTER_OTP", otp);
        session.setAttribute("OTP_TIME", now);
        session.setAttribute("OTP_ATTEMPTS", 0);
        session.setAttribute("LAST_OTP_SENT", now);

        // 4. Email
        String subject = "OTP Verification";
        String content = "<h3>Mã OTP: <b>" + otp + "</b></h3>"
                + "<p>Hiệu lực 5 phút</p>";

        try {
            EmailUtil.sendHtml(email, subject, content);
            response.getWriter().write("success");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("error");
        }
    }
}
