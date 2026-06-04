package com.webshop.app.controller.ContactController;

import com.webshop.app.dao.ContactDAO;
import com.webshop.app.dao.NotificationDAO;
import com.webshop.app.model.Contact;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/lien-he")
public class UserContactServlet extends HttpServlet {

    private final ContactDAO contactDAO = new ContactDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", "MyCosmetic | Liên hệ");
        req.setAttribute("pageContent", "/jsp/contact/contact.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String message = req.getParameter("message");

        Contact c = new Contact();
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhone(phone != null ? phone : "");
        c.setSubject("Liên hệ từ trang khách hàng");
        c.setMessage(message);

        try {
            contactDAO.insert(c);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String title = "Có tin nhắn liên hệ mới từ " + fullName;
        String briefMessage = "Khách hàng " + fullName + " (" + email + ") vừa gửi lời nhắn...";
        String targetUrl = "/admin/notifications";

        try {
            notificationDAO.createAdminNotification(
                    "CONTACT_CREATED",
                    title,
                    briefMessage,
                    targetUrl,
                    "SYSTEM",
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute("messageSuccess", "Cảm ơn " + fullName + "! Lời nhắn của bạn đã được gửi thành công.");
        req.setAttribute("pageTitle", "MyCosmetic | Liên hệ");
        req.setAttribute("pageContent", "/jsp/contact/contact.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
