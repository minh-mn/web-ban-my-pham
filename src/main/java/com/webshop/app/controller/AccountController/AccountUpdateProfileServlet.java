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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        email = (email != null) ? email.trim() : null;
        phone = (phone != null) ? phone.trim() : null;

        // ===== VALIDATE =====
        if (email == null || email.isEmpty()
                || !email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            resp.sendRedirect(req.getContextPath() + "/account?update=invalid_email");
            return;
        }

        if (phone == null || phone.isEmpty()
                || !phone.matches("^\\d{9,11}$")) {
            resp.sendRedirect(req.getContextPath() + "/account?update=invalid_phone");
            return;
        }

        // ===== CHECK EMAIL DUPLICATE (KHUYẾN NGHỊ) =====
        // Lưu ý: UserDAO.findByEmail(email) nên KHÔNG chặn active để check trùng chính xác.
        User existed = userDAO.findByEmail(email);
        if (existed != null && existed.getId() != user.getId()) {
            resp.sendRedirect(req.getContextPath() + "/account?update=email_used");
            return;
        }

        // ===== UPDATE =====
        userDAO.updateContact(user.getId(), email, phone);

        // reload session (để header/account hiển thị đúng ngay)
        User fresh = userDAO.findById(user.getId());
        if (fresh != null) session.setAttribute("user", fresh);

        resp.sendRedirect(req.getContextPath() + "/account?update=success");
    }
}
