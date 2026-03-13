package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Servlet xử lý chức năng đăng ký tài khoản
 * URL truy cập: /register
 *
 * Chức năng:
 * - Hiển thị form đăng ký
 * - Kiểm tra dữ liệu người dùng nhập vào
 * - Kiểm tra trùng username / email
 * - Tạo tài khoản mới trong hệ thống
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu người dùng
    private final UserDAO userDAO = new UserDAO();

    /*
     * Phương thức GET
     * Hiển thị trang đăng ký
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Thiết lập thông tin trang
        req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");

        /*
         * Nếu base.jsp đang tự động load CSS theo pageCss
         * thì giữ thuộc tính này.
         * Nếu register.jsp đã tự import CSS rồi thì có thể bỏ.
         */
        req.setAttribute("pageCss", "register.css");

        // Chỉ định nội dung trang cần render
        req.setAttribute("pageContent", "/jsp/auth/register.jsp");

        // Forward đến layout chính
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý đăng ký tài khoản mới
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ===== LẤY DỮ LIỆU TỪ FORM =====
        String username = req.getParameter("username");
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        // Danh sách lỗi validation
        List<String> errors = new ArrayList<>();

        // ===== CHUẨN HÓA DỮ LIỆU =====
        // Trim khoảng trắng để tránh lỗi nhập "   "
        String uName = (username != null) ? username.trim() : null;
        String fName = (fullName != null) ? fullName.trim() : null;
        String mail = (email != null) ? email.trim() : null;
        String ph = (phone != null) ? phone.trim() : null;

        // ===== VALIDATION =====

        // Kiểm tra tên đăng nhập
        if (uName == null || uName.isBlank()) {
            errors.add("Tên đăng nhập không được để trống");
        }

        // Kiểm tra họ tên
        if (fName == null || fName.isBlank()) {
            errors.add("Họ tên không được để trống");
        }

        // Kiểm tra email
        if (mail == null || mail.isBlank()) {
            errors.add("Email không được để trống");
        } else if (!mail.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errors.add("Email không hợp lệ");
        }

        // Kiểm tra số điện thoại
        if (ph == null || ph.isBlank()) {
            errors.add("Số điện thoại không được để trống");
        } else if (!ph.matches("^\\d{9,11}$")) {
            errors.add("Số điện thoại không hợp lệ (9–11 chữ số)");
        }

        // Kiểm tra mật khẩu
        if (password == null || password.length() < 6) {
            errors.add("Mật khẩu phải có ít nhất 6 ký tự");
        }

        // Kiểm tra mật khẩu xác nhận
        if (confirm == null || password == null || !password.equals(confirm)) {
            errors.add("Mật khẩu xác nhận không khớp");
        }

        // ===== KIỂM TRA TRÙNG DỮ LIỆU =====
        // Chỉ kiểm tra khi input đã có giá trị tối thiểu
        if (uName != null && !uName.isBlank() && userDAO.findByUsername(uName) != null) {
            errors.add("Tên đăng nhập đã tồn tại");
        }

        if (mail != null && !mail.isBlank() && userDAO.findByEmail(mail) != null) {
            errors.add("Email đã được sử dụng");
        }

        // ===== NẾU CÓ LỖI → HIỂN THỊ LẠI FORM =====
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);

            req.setAttribute("pageTitle", "MyCosmetic | Đăng ký");
            req.setAttribute("pageCss", "register.css");
            req.setAttribute("pageContent", "/jsp/auth/register.jsp");

            /*
             * Lưu ý:
             * JSP đang dùng ${param.fullName}, ${param.email}, ...
             * nên dữ liệu người dùng nhập sẽ tự giữ lại sau khi forward.
             * Không cần set thêm old_fullName, old_email,...
             */
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // ===== TẠO NGƯỜI DÙNG MỚI =====
        User u = new User();
        u.setUsername(uName);
        u.setFullName(fName);
        u.setEmail(mail);
        u.setPhone(ph);
        u.setRole("USER");
        u.setActive(true);

        /*
         * Tạo tài khoản trong database
         * Mật khẩu sẽ được hash bên trong DAO
         * (ví dụ dùng BCrypt)
         */
        userDAO.create(u, password);

        // Chuyển về trang đăng nhập sau khi đăng ký thành công
        resp.sendRedirect(req.getContextPath() + "/login?register=success");
    }
}