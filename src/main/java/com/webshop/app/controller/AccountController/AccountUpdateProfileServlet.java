package com.webshop.app.controller.AccountController;

import java.io.IOException;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/account/update-profile")
public class AccountUpdateProfileServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8"); 
        resp.setContentType("application/json"); 

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Bạn cần đăng nhập!\"}");
            return;
        }

        // 1. Nhận thông tin từ form
        User pendingUpdate = new User();
        pendingUpdate.setId(user.getId());
        pendingUpdate.setFullName(req.getParameter("fullName"));
        pendingUpdate.setEmail(req.getParameter("email"));
        pendingUpdate.setPhone(req.getParameter("phone"));
        pendingUpdate.setAddress(req.getParameter("address"));

        // 2. Tạo OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));

        try {
            // 3. Gửi email
            com.webshop.app.utils.EmailUtil.sendHtml(pendingUpdate.getEmail(),
                    "Xác thực thay đổi thông tin", "Mã OTP của bạn là: <b>" + otp + "</b>");

            // 4. Lưu vào session
            session.setAttribute("pendingUpdate", pendingUpdate);
            session.setAttribute("REGISTER_OTP", otp);

            // 5. Trả về JSON thành công thay vì sendRedirect
            resp.getWriter().write("{\"status\":\"success\"}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Lỗi gửi email xác thực!\"}");
        }

        String email = pendingUpdate.getEmail();
        // Biểu thức kiểm tra email chuẩn (có @, có dấu chấm, không chứa khoảng trắng)
        String emailRegex = "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

        if (email == null || email.isBlank()) {
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Email không được để trống!\"}");
            return;
        }

        if (!email.matches(emailRegex)) {
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Định dạng email không hợp lệ (ví dụ đúng: abc@gmail.com)!\"}");
            return;
        }

        // Kiểm tra Số điện thoại
        String phone = pendingUpdate.getPhone();
        String phoneRegex = "^(03|05|07|08|09)\\d{8}$";

        if (phone == null || phone.isBlank()) {
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Số điện thoại không được để trống!\"}");
            return;
        }

        if (!phone.matches(phoneRegex)) {
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Số điện thoại không hợp lệ (10 số, bắt đầu 03/05/07/08/09)!\"}");
            return;
        }
    }
}
