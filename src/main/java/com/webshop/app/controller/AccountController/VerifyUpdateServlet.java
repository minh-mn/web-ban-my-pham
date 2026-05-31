package com.webshop.app.controller.AccountController;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import jakarta.servlet.ServletException; // Import này
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException; // Import này

@WebServlet("/verify-update")
public class VerifyUpdateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Cấu hình trả về JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        String inputOtp = req.getParameter("otp_input");
        String serverOtp = (String) session.getAttribute("REGISTER_OTP");
        User pendingUpdate = (User) session.getAttribute("pendingUpdate");

        // 2. Kiểm tra OTP
        if (serverOtp != null && serverOtp.equals(inputOtp) && pendingUpdate != null) {
            UserDAO userDAO = new UserDAO();

            // Cập nhật database
            userDAO.updateFullProfile(pendingUpdate);

            // 2. Lấy đối tượng User hiện tại đang lưu trong Session
            User currentUser = (User) session.getAttribute("user");

            // 3. Chỉ cập nhật các trường đã thay đổi vào đối tượng đang có
            if (currentUser != null) {
                currentUser.setFullName(pendingUpdate.getFullName());
                currentUser.setEmail(pendingUpdate.getEmail());
                currentUser.setPhone(pendingUpdate.getPhone());
                currentUser.setAddress(pendingUpdate.getAddress());

                // 4. Lưu lại đối tượng đã cập nhật vào Session
                session.setAttribute("user", currentUser);
            }

            // Xóa session tạm
            session.removeAttribute("pendingUpdate");
            session.removeAttribute("REGISTER_OTP");

            // TRẢ VỀ JSON THÀNH CÔNG
            resp.getWriter().write("{\"status\":\"success\", \"message\":\"Cập nhật thành công!\"}");
        } else {
            // TRẢ VỀ JSON THẤT BẠI
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Mã OTP không chính xác hoặc đã hết hạn!\"}");
        }
    }
}