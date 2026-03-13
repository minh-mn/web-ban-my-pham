package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;

// ===== Jakarta Servlet API (Tomcat 10+ sử dụng jakarta thay vì javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 * Servlet xử lý chức năng đổi mật khẩu của người dùng
 * URL truy cập: /account/change-password
 */
@WebServlet("/account/change-password")
public class ChangePasswordServlet extends HttpServlet {

    // Khai báo serialVersionUID để tránh warning khi serialize servlet
    private static final long serialVersionUID = 1L;

    // DAO dùng để thao tác dữ liệu người dùng trong database
    private final UserDAO userDAO = new UserDAO();

    /*
     * Phương thức GET
     * Hiển thị trang đổi mật khẩu
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Lấy session hiện tại (false = không tạo session mới nếu chưa tồn tại)
        HttpSession session = req.getSession(false);

        // Lấy thông tin user đã đăng nhập từ session
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập → chuyển hướng về trang login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Thiết lập các attribute để render giao diện
        req.setAttribute("pageTitle", "MyCosmetic | Đổi mật khẩu");
        req.setAttribute("pageCss", "login.css");
        req.setAttribute("pageContent", "/jsp/auth/change_password.jsp");

        // Forward đến layout chính
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý logic đổi mật khẩu
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Lấy session
        HttpSession session = req.getSession(false);

        // Lấy user từ session
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập → chuyển hướng về login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Lấy dữ liệu từ form
        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        // Biến lưu thông báo lỗi
        String error = null;

        // ===== Kiểm tra dữ liệu nhập =====

        // Kiểm tra mật khẩu hiện tại
        if (oldPass == null || oldPass.isBlank()) {
            error = "Vui lòng nhập mật khẩu hiện tại";
        }
        // Kiểm tra độ dài mật khẩu mới
        else if (newPass == null || newPass.length() < 6) {
            error = "Mật khẩu mới phải ≥ 6 ký tự";
        }
        // Kiểm tra mật khẩu xác nhận
        else if (!newPass.equals(confirm)) {
            error = "Mật khẩu xác nhận không khớp";
        }
        // Kiểm tra mật khẩu cũ có đúng không
        else if (!userDAO.checkPassword(user.getId(), oldPass)) {
            error = "Mật khẩu hiện tại không đúng";
        }

        // ===== Nếu không có lỗi → cập nhật mật khẩu =====
        if (error == null) {

            // Cập nhật mật khẩu mới vào database
            userDAO.updatePassword(user.getId(), newPass);

            // Thông báo thành công
            req.setAttribute("success", "Đổi mật khẩu thành công");

        } else {

            // Gửi thông báo lỗi ra giao diện
            req.setAttribute("error", error);
        }

        // Thiết lập lại dữ liệu trang
        req.setAttribute("pageTitle", "MyCosmetic | Đổi mật khẩu");
        req.setAttribute("pageCss", "login.css");
        req.setAttribute("pageContent", "/jsp/auth/change_password.jsp");

        // Forward lại trang đổi mật khẩu
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}