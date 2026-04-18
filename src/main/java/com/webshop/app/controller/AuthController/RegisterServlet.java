package com.webshop.app.controller.AuthController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");

        // (Tuỳ kiến trúc) Nếu base.jsp đang load pageCss theo contextPath/assets/css/${pageCss}
        // thì bật dòng dưới. Nếu register.jsp đã tự link register.css như bạn đưa thì có thể bỏ.
        req.setAttribute("pageCss", "register.css");

        req.setAttribute("pageContent", "/jsp/auth/register.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String fullName = req.getParameter("fullName");
        String email    = req.getParameter("email");
        String phone    = req.getParameter("phone");

        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");

        List<String> errors = new ArrayList<>();

        // ===== NORMALIZE (tránh lỗi "   ") =====
        String uName = (username != null) ? username.trim() : null;
        String fName = (fullName != null) ? fullName.trim() : null;
        String mail  = (email != null) ? email.trim() : null;
        String ph    = (phone != null) ? phone.trim() : null;

        // ===== VALIDATION =====
        if (uName == null || uName.isBlank()) {
            errors.add("Tên đăng nhập không được để trống");
        }

        if (fName == null || fName.isBlank()) {
            errors.add("Họ tên không được để trống");
        }

        if (mail == null || mail.isBlank()) {
            errors.add("Email không được để trống");
        } else if (!mail.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errors.add("Email không hợp lệ");
        }

        if (ph == null || ph.isBlank()) {
            errors.add("Số điện thoại không được để trống");
        } else if (!ph.matches("^\\d{9,11}$")) {
            errors.add("Số điện thoại không hợp lệ (9–11 chữ số)");
        }

        if (password == null || password.length() < 6) {
            errors.add("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (confirm == null || password == null || !password.equals(confirm)) {
            errors.add("Mật khẩu xác nhận không khớp");
        }

        // ===== DUPLICATE CHECK (chỉ check khi input hợp lệ tối thiểu) =====
        if (uName != null && !uName.isBlank() && userDAO.findByUsername(uName) != null) {
            errors.add("Tên đăng nhập đã tồn tại");
        }

        if (mail != null && !mail.isBlank() && userDAO.findByEmail(mail) != null) {
            errors.add("Email đã được sử dụng");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);

            req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");
            req.setAttribute("pageCss", "register.css");
            req.setAttribute("pageContent", "/jsp/auth/register.jsp"); // ✅ ĐÚNG đường dẫn

            // Lưu ý: JSP của bạn đang dùng ${param.fullName}... nên tự giữ dữ liệu nhập được rồi.
            // Không cần old_username/old_email/old_phone...

            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // ===== CREATE USER =====
        User u = new User();
        u.setUsername(uName);
        u.setFullName(fName);
        u.setEmail(mail);
        u.setPhone(ph);
        u.setRole("USER");
        u.setActive(true);

        userDAO.create(u, password); // BCrypt hash bên trong DAO

        resp.sendRedirect(req.getContextPath() + "/login?register=success");
    }
}
