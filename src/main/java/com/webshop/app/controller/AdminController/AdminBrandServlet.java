package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.model.Brand;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/brands")
public class AdminBrandServlet extends HttpServlet {

    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {

            case "new": {
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                   .forward(req, resp);
                break;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brands");
                    return;
                }

                Brand brand = brandDAO.findById(id);
                if (brand == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brands");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("brand", brand);

                req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                   .forward(req, resp);
                break;
            }

            case "list":
            default: {
                req.setAttribute("brands", brandDAO.findAll());
                req.getRequestDispatcher("/jsp/admin/brand/brand_list.jsp")
                   .forward(req, resp);
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "create";

        try {
            switch (action) {

                case "create": {
                    String name = safe(req.getParameter("name"));
                    validateName(name);

                    brandDAO.create(name);
                    break;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) break;

                    String name = safe(req.getParameter("name"));
                    validateName(name);

                    brandDAO.update(id, name);
                    break;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) brandDAO.delete(id);
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/brands");

        } catch (IllegalArgumentException ex) {
            // validate fail -> quay lại form
            req.setAttribute("error", ex.getMessage());

            if ("update".equals(action)) {
                req.setAttribute("mode", "edit");

                Brand b = new Brand();
                b.setId(safeInt(req.getParameter("id"), 0));
                b.setName(safe(req.getParameter("name")));
                req.setAttribute("brand", b);

            } else {
                req.setAttribute("mode", "create");

                Brand b = new Brand();
                b.setName(safe(req.getParameter("name")));
                req.setAttribute("brand", b);
            }

            req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
               .forward(req, resp);
        }
    }

    /* ===================== HELPERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống.");
        }
    }
}
