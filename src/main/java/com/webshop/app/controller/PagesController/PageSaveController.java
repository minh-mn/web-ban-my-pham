package com.webshop.app.controller.PagesController;

import com.webshop.app.dao.PageDAO;
import com.webshop.app.model.Page;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/pages/save")
public class PageSaveController extends HttpServlet {

    private PageDAO dao = new PageDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Page p = new Page();

        String id = req.getParameter("id");
        if (id != null && !id.isEmpty()) {
            p.setId(Integer.parseInt(id));
        }

        p.setTitle(req.getParameter("title"));
        p.setSlug(req.getParameter("slug"));
        p.setContent(req.getParameter("content"));
        p.setType(req.getParameter("type"));

        dao.save(p);

        resp.sendRedirect(req.getContextPath() + "/admin/pages");
    }
}