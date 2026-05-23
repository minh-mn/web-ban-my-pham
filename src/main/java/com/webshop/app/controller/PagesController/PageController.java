package com.webshop.app.controller.PagesController;

import com.webshop.app.dao.PageDAO;
import com.webshop.app.model.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/page/*")
public class PageController extends HttpServlet {

    private PageDAO dao = new PageDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String slug = req.getPathInfo().substring(1);

        Page page = dao.findBySlug(slug);

        req.setAttribute("page", page);

        req.getRequestDispatcher("/jsp/pages/page-detail.jsp").forward(req, resp);
    }
}
