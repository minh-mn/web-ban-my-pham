package com.webshop.app.controller.AuthController;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/verify-registration")
public class VerifyRegistrationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "Xác thực OTP");
        req.setAttribute("pageContent", "/jsp/auth/verify-otp.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String inputOtp = req.getParameter("otp_input");
        HttpSession session = req.getSession();

        String serverOtp = (String) session.getAttribute("REGISTER_OTP");
        User pendingUser = (User) session.getAttribute("pendingUser");
        String pendingProvider = (String) session.getAttribute("pendingProvider");
        String pendingSocialId = (String) session.getAttribute("pendingSocialId");

        if (serverOtp == null || !serverOtp.equals(inputOtp)) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Mã OTP xác thực không chính xác!\"}");
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();

            if (pendingProvider != null && pendingSocialId != null) {
                userDAO.saveSocialUser(pendingUser, pendingProvider, pendingSocialId);
            } else {
                userDAO.insert(pendingUser); // Tiến hành lưu chính thức vào DATABASE
            }

            // Giải phóng bộ nhớ Session
            session.removeAttribute("REGISTER_OTP");
            session.removeAttribute("pendingUser");
            session.removeAttribute("pendingProvider");
            session.removeAttribute("pendingSocialId");
            session.removeAttribute("OTP_TIME");

            // Trả về JSON thành công đúng với câu thông báo bạn yêu cầu
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"Đăng ký tài khoản thành công vui lòng đăng nhập để trải nghiệm\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi lưu tài khoản vào cơ sở dữ liệu!\"}");
        }
    }
}
