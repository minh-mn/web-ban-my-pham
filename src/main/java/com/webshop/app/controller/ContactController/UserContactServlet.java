package com.webshop.app.controller.ContactController;

import com.webshop.app.dao.ContactDAO;
import com.webshop.app.model.Contact;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/lien-he") // Đường dẫn mới hoàn toàn độc lập
public class UserContactServlet extends HttpServlet {

    private final ContactDAO contactDAO = new ContactDAO();

    // 1. Hiển thị trang Liên Hệ khi click vào menu Header
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("pageTitle", "MyCosmetic | Liên hệ");
        req.setAttribute("pageContent", "/jsp/contact/contact.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    // 2. Xử lý khi khách điền Form chi tiết ở trang Liên Hệ và bấm Gửi
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // Lấy đầy đủ thông tin từ form chi tiết
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String message = req.getParameter("message");

        // Đổ dữ liệu vào Model
        Contact c = new Contact();
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhone(phone != null ? phone : "");
        c.setSubject("Liên hệ từ trang khách hàng");
        c.setMessage(message);

        // Lưu vào database thông qua DAO có sẵn của bạn
        contactDAO.insert(c);

        // Gửi thông báo thành công hiển thị ngay trên trang
        req.setAttribute("messageSuccess", "Cảm ơn " + fullName + "! Lời nhắn của bạn đã được gửi thành công.");

        // Tải lại giao diện trang liên hệ kèm thông báo
        req.setAttribute("pageTitle", "MyCosmetic | Liên hệ");
        req.setAttribute("pageContent", "/jsp/contact/contact.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}