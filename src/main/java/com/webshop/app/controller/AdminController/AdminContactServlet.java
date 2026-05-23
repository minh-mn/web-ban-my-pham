package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.ContactDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/contact-messages")
public class AdminContactServlet extends HttpServlet {

    private final ContactDAO dao = new ContactDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("list", dao.findAll());

        req.getRequestDispatcher("/jsp/admin/contact/contact-list.jsp")
                .forward(req, resp);
    }
}
