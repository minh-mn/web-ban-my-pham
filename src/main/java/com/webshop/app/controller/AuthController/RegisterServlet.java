package com.webshop.app.controller.AuthController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.utils.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Hiển thị trang đăng ký
        req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");
        req.setAttribute("pageContent", "/jsp/auth/register.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        Map<String, String> errors = new HashMap<>();

        // 1. Lấy dữ liệu từ form
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String fullName = req.getParameter("fullName");
        String userName = req.getParameter("userName");
        String phone = req.getParameter("phone");
        String birthday = req.getParameter("birthday");
        String gender = req.getParameter("gender");

        // 2. Validate dữ liệu cơ bản (Server-side validation)
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Email không đúng định dạng.");
        }
        if (password == null || password.length() < 8) {
            errors.put("password", "Mật khẩu phải từ 8 ký tự.");
        }
        if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không khớp.");
        }
        if (userName == null || userName.length() < 3) {
            errors.put("userName", "Tên đăng nhập quá ngắn.");
        }

        // 3. Kiểm tra trùng lặp trong Database
        if (errors.isEmpty()) {
            if (userDAO.findByEmail(email) != null) {
                errors.put("email", "Email này đã được sử dụng.");
            }
            if (userDAO.findByUsername(userName) != null) {
                errors.put("userName", "Tên đăng nhập này đã tồn tại.");
            }
        }

        // 4. Xử lý kết quả validate
        if (!errors.isEmpty()) {
            // Có lỗi -> Quay lại trang đăng ký và hiển thị lỗi
            req.setAttribute("errors", errors);
            // Giữ lại các giá trị cũ để user không phải nhập lại (trừ password)
            req.setAttribute("oldEmail", email);
            req.setAttribute("oldFullName", fullName);
            req.setAttribute("pageContent", "/jsp/auth/register.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // 5. Nếu mọi thứ OK -> Chuẩn bị User tạm thời
        User pendingUser = new User();
        pendingUser.setEmail(email);
        pendingUser.setUsername(userName);
        pendingUser.setPassword(password); // Nên BCrypt.hashpw(...) trước khi set
        pendingUser.setFullName(fullName);
        pendingUser.setPhone(phone);
       // pendingUser.setBirthday(java.sql.Date.valueOf(birthday));
        //pendingUser.setGender(gender);

        // 6. Tạo mã OTP (6 chữ số)
        String otp = String.format("%06d", new Random().nextInt(999999));

        // 7. Lưu User và OTP vào Session
        HttpSession session = req.getSession();
        session.setAttribute("pendingUser", pendingUser);
        String otpp = String.format("%06d", new Random().nextInt(1000000));
        session.setAttribute("REGISTER_OTP", otp);
        session.setAttribute("OTP-TIME", System.currentTimeMillis());
        session.setAttribute("OTP_ATTEMPTS", 0);
        session.setAttribute("LAST_OTP_SENT", System.currentTimeMillis());


        // 8. Gửi OTP qua Email
        try {
            String subject = "Mã xác thực đăng ký tài khoản MyCosmetic";
            String content = "<h2>Xác thực tài khoản</h2>"
                    + "<p>Chào " + fullName + ",</p>"
                    + "<p>Mã OTP của bạn là: <b style='font-size: 20px; color: #d81b60;'>" + otp + "</b></p>"
                    + "<p>Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>";

            EmailUtil.sendHtml(email, subject, content);

            // Chuyển hướng sang trang nhập OTP
            resp.sendRedirect(req.getContextPath() + "/verify-otp");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("status", "fail");
            req.setAttribute("message", "Lỗi gửi email: " + e.getMessage());
            req.setAttribute("pageContent", "/jsp/auth/register.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
        }
    }
}
