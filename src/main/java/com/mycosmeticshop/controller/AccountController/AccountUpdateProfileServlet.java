package com.mycosmeticshop.controller.AccountController;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;

@WebServlet("/account/update-profile")
public class AccountUpdateProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy user từ session
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập thì chuyển về trang login
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Lấy dữ liệu từ form
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        email = (email != null) ? email.trim() : null;
        phone = (phone != null) ? phone.trim() : null;

        // =========================
        // VALIDATE EMAIL
        // =========================
        if (email == null || email.isEmpty()
                || !email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            resp.sendRedirect(req.getContextPath() + "/account?update=invalid_email");
            return;
        }

        // =========================
        // VALIDATE SỐ ĐIỆN THOẠI
        // Chỉ cho phép 10 chữ số
        // =========================
        if (phone == null || phone.isEmpty()
                || !phone.matches("^\\d{10}$")) {
            resp.sendRedirect(req.getContextPath() + "/account?update=invalid_phone");
            return;
        }

        // =========================
        // KIỂM TRA EMAIL ĐÃ TỒN TẠI HAY CHƯA
        // Nếu email thuộc user khác thì không cho cập nhật
        // =========================
        User existed = userDAO.findByEmail(email);
        if (existed != null && existed.getId() != user.getId()) {
            resp.sendRedirect(req.getContextPath() + "/account?update=email_used");
            return;
        }

        // =========================
        // CẬP NHẬT EMAIL + PHONE
        // =========================
        userDAO.updateContact(user.getId(), email, phone);

        // Reload lại user mới nhất từ DB để session cập nhật ngay
        User fresh = userDAO.findById(user.getId());
        if (fresh != null) {
            session.setAttribute("user", fresh);
        }

        // Quay lại trang tài khoản với trạng thái thành công
        resp.sendRedirect(req.getContextPath() + "/account?update=success");
    }
}