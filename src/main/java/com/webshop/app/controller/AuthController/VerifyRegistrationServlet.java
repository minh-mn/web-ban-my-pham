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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Hiển thị trang nhập OTP
        req.setAttribute("pageTitle", "Xác thực OTP");
        req.setAttribute("pageContent", "/jsp/auth/verify-otp.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String inputOtp = req.getParameter("otp_input");
        HttpSession session = req.getSession();
        String serverOtp = (String) session.getAttribute("REGISTER_OTP");
        User pendingUser = (User) session.getAttribute("pendingUser");

        if (serverOtp != null && serverOtp.equals(inputOtp)) {
            // OTP đúng -> Lưu vào Database chính thức
            UserDAO userDAO = new UserDAO();
            boolean isSaved = userDAO.insert(pendingUser); // Giả sử bạn có hàm insert

            if (isSaved) {
                // Xóa session tạm
                session.removeAttribute("REGISTER_OTP");
                session.removeAttribute("pendingUser");
                // Thông báo đăng ký thành công
                resp.sendRedirect(req.getContextPath() + "/login?status=success");
            } else {
                req.setAttribute("error", "Có lỗi xảy ra khi lưu tài khoản.");
                req.getRequestDispatcher("/jsp/auth/verify-otp.jsp").forward(req, resp);
            }
        } else {
            // OTP sai
            req.setAttribute("error", "Mã OTP không chính xác. Vui lòng thử lại.");
            req.getRequestDispatcher("/jsp/auth/verify-otp.jsp").forward(req, resp);
        }
    }
}