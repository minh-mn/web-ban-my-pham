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
        req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");
        req.setAttribute("pageContent", "/jsp/auth/register.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Cấu hình bắt buộc để trả về định dạng JSON sạch
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> errors = new HashMap<>();

            // Lấy dữ liệu từ form gửi lên
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String confirmPassword = req.getParameter("confirmPassword");
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String birthday = req.getParameter("birthDate");
            String gender = req.getParameter("gender");

            String userName = req.getParameter("userName");
            if (userName == null || userName.trim().isEmpty()) {
                userName = email;
            }

            // 2. Kiểm tra dữ liệu hợp lệ cơ bản
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.put("email", "Email không đúng định dạng.");
            }
            if (password == null || password.length() < 6) {
                errors.put("password", "Mật khẩu phải từ 6 ký tự.");
            }
            if (confirmPassword != null && !confirmPassword.equals(password)) {
                errors.put("confirmPassword", "Mật khẩu xác nhận không khớp.");
            }

            // 3. Kiểm tra trùng lặp email trong DB (Đã bọc bảo vệ)
            if (errors.isEmpty()) {
                try {
                    if (userDAO.findByEmail(email) != null) {
                        errors.put("email", "Email này đã được sử dụng.");
                    }
                } catch (Exception dbEx) {
                    dbEx.printStackTrace();
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi kết nối Database! Hãy kiểm tra XAMPP xem đã bật MySQL chưa.\"}");
                    return;
                }
            }

            // 4. Nếu có lỗi nhập liệu -> Xuất chuỗi JSON báo lỗi
            if (!errors.isEmpty()) {
                StringBuilder json = new StringBuilder("{\"status\":\"error\",\"errors\":{");
                int i = 0;
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                    if (i < errors.size() - 1) json.append(",");
                    i++;
                }
                json.append("}}");
                resp.getWriter().write(json.toString());
                return;
            }

            // 5. Nếu mọi thứ hợp lệ -> Tạo đối tượng User tạm thời
            User pendingUser = new User();
            pendingUser.setEmail(email);
            pendingUser.setUsername(userName);
            pendingUser.setPassword(password);
            pendingUser.setFullName(fullName);
            pendingUser.setPhone(phone);
            pendingUser.setBirthDate(birthday);
            pendingUser.setGender(gender);

            // 6. Tạo ngẫu nhiên mã OTP 6 chữ số
            String otp = String.format("%06d", new Random().nextInt(999999));

            // 7. Lưu thông tin vào Session để giữ trạng thái chờ xác thực
            HttpSession session = req.getSession();
            session.setAttribute("pendingUser", pendingUser);
            session.setAttribute("REGISTER_OTP", otp);
            session.setAttribute("OTP_TIME", System.currentTimeMillis());

            // 8. Tiến hành gửi mail chứa OTP
            try {
                String subject = "Mã xác thực đăng ký tài khoản MyCosmetic";
                String content = "<h2>Xác thực tài khoản</h2>"
                        + "<p>Chào " + fullName + ",</p>"
                        + "<p>Mã OTP của bạn là: <b style='font-size: 20px; color: #d81b60;'>" + otp + "</b></p>"
                        + "<p>Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>";

                EmailUtil.sendHtml(email, subject, content);
                resp.getWriter().write("{\"status\":\"otp_required\",\"message\":\"Mã OTP đã được gửi thành công!\"}");

            } catch (Exception mailEx) {
                mailEx.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi gửi email: " + mailEx.getMessage().replace("\"", "'") + "\"}");
            }

        } catch (Exception globalEx) {
            globalEx.printStackTrace();
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi hệ thống: " + globalEx.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
