package com.webshop.app.controller.ContactController;

import com.webshop.app.dao.ContactDAO;
import com.webshop.app.model.Contact;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/contact")
public class ContactServlet extends HttpServlet {

    private final ContactDAO contactDAO = new ContactDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        Contact c = new Contact();

        c.setEmail(req.getParameter("email"));

        // nếu footer chỉ có email → set default
        c.setFullName("Guest");
        c.setPhone("");
        c.setSubject("Footer Contact");
        c.setMessage("User subscribed via footer email");

        contactDAO.insert(c);

        // redirect về trang trước
        resp.sendRedirect(req.getContextPath() + "/");
    }
}