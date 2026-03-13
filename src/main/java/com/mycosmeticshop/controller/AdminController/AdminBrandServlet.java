package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.BrandDAO;
import com.mycosmeticshop.model.Brand;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/brands")
public class AdminBrandServlet extends HttpServlet {

    // DAO dùng để thao tác với bảng Brand trong database
    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy action từ URL
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {

            // =========================
            // HIỂN THỊ FORM TẠO BRAND
            // =========================
            case "new": {
                req.setAttribute("mode", "create");

                req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                        .forward(req, resp);
                break;
            }

            // =========================
            // HIỂN THỊ FORM EDIT BRAND
            // =========================
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

            // =========================
            // HIỂN THỊ DANH SÁCH BRAND
            // =========================
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
        if (action == null) {
            action = "create";
        }

        try {

            switch (action) {

                // =========================
                // TẠO BRAND MỚI
                // =========================
                case "create": {

                    String name = safe(req.getParameter("name"));

                    validateName(name);

                    brandDAO.create(name);

                    break;
                }

                // =========================
                // CẬP NHẬT BRAND
                // =========================
                case "update": {

                    int id = safeInt(req.getParameter("id"), -1);

                    if (id <= 0) {
                        break;
                    }

                    String name = safe(req.getParameter("name"));

                    validateName(name);

                    brandDAO.update(id, name);

                    break;
                }

                // =========================
                // XÓA BRAND
                // =========================
                case "delete": {

                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        brandDAO.delete(id);
                    }

                    break;
                }

                default:
                    break;
            }

            // Sau khi thao tác xong quay lại danh sách
            resp.sendRedirect(req.getContextPath() + "/admin/brands");

        } catch (IllegalArgumentException ex) {

            // Nếu validate lỗi thì quay lại form và hiển thị lỗi
            req.setAttribute("error", ex.getMessage());

            if ("update".equals(action)) {

                req.setAttribute("mode", "edit");

                Brand brand = new Brand();
                brand.setId(safeInt(req.getParameter("id"), 0));
                brand.setName(safe(req.getParameter("name")));

                req.setAttribute("brand", brand);

            } else {

                req.setAttribute("mode", "create");

                Brand brand = new Brand();
                brand.setName(safe(req.getParameter("name")));

                req.setAttribute("brand", brand);
            }

            req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                    .forward(req, resp);
        }
    }

    /* ======================================================
       HELPER METHODS
       Các hàm hỗ trợ xử lý dữ liệu đầu vào
       ====================================================== */

    // Parse String -> int an toàn
    private int safeInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    // Trim chuỗi an toàn
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // Validate tên brand
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống.");
        }
    }
}