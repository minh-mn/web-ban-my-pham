package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.PageDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/pages")
public class PageAdminController extends HttpServlet {

    private PageDAO dao = new PageDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if (action == null) {
            req.setAttribute("pages", dao.findAll());
            req.getRequestDispatcher("/jsp/admin/pages/page-list.jsp").forward(req, resp);
            return;
        }

        if ("new".equals(action)) {
            req.getRequestDispatcher("/jsp/admin/pages/page-form.jsp").forward(req, resp);
            return;
        }

        if ("edit".equals(action)) {

            int id = Integer.parseInt(req.getParameter("id"));
            req.setAttribute("page", dao.findById(id));

            req.getRequestDispatcher("/jsp/admin/pages/page-form.jsp").forward(req, resp);
        }
    }
}
