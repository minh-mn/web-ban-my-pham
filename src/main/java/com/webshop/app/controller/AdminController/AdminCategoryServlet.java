package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ===== CONSTANTS =====
    private static final String JSP_LIST = "/jsp/admin/category/category_list.jsp";
    private static final String JSP_FORM = "/jsp/admin/category/category_form.jsp";

    private static final String ACT_LIST = "list";
    private static final String ACT_NEW = "new";
    private static final String ACT_EDIT = "edit";

    private static final String ACT_CREATE = "create";
    private static final String ACT_UPDATE = "update";
    private static final String ACT_DELETE = "delete";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = ACT_LIST;

        switch (action) {

            case ACT_NEW: {
                req.setAttribute("mode", "create");
                // dropdown danh mục cha
                req.setAttribute("parentCategories", categoryDAO.findAllParents());
                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            case ACT_EDIT: {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/categories");
                    return;
                }

                Category category = categoryDAO.findById(id);
                if (category == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/categories");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("category", category);
                req.setAttribute("parentCategories", categoryDAO.findAllParents());

                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            case ACT_LIST:
            default: {
                renderList(req, resp);
                return;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = ACT_CREATE;

        try {
            switch (action) {

                case ACT_CREATE: {
                    Category c = new Category();
                    bind(req, c);
                    validate(c);
                    categoryDAO.create(c);
                    break;
                }

                case ACT_UPDATE: {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) break;

                    Category c = new Category();
                    c.setId(id);
                    bind(req, c);
                    validate(c);
                    categoryDAO.update(c);
                    break;
                }

                case ACT_DELETE: {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id > 0) categoryDAO.delete(id);
                    break;
                }

                default: {
                    // action lạ -> quay về list
                    break;
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return;

        } catch (IllegalArgumentException ex) {
            // lỗi validate -> trả về form + giữ dữ liệu
            forwardFormWithError(req, resp, action, ex.getMessage());
            return;

        } catch (Exception ex) {
            // lỗi bất ngờ -> in log + trả về form
            ex.printStackTrace();
            forwardFormWithError(req, resp, action, "Có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    /* ===================== VIEW HELPERS ===================== */

    private void renderList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Category> all = categoryDAO.findAll();

        Map<Integer, List<Category>> childrenMap = new LinkedHashMap<>();
        List<Category> parents = new ArrayList<>();

        for (Category cat : all) {
            if (cat.getParentId() == null) {
                parents.add(cat);
            } else {
                childrenMap.computeIfAbsent(cat.getParentId(), k -> new ArrayList<>()).add(cat);
            }
        }

        // sort cho đẹp (tuỳ chọn)
        parents.sort(Comparator.comparing(c -> safe(c.getName()), String.CASE_INSENSITIVE_ORDER));
        for (List<Category> chs : childrenMap.values()) {
            chs.sort(Comparator.comparing(c -> safe(c.getName()), String.CASE_INSENSITIVE_ORDER));
        }

        req.setAttribute("parents", parents);
        req.setAttribute("childrenMap", childrenMap);

        req.getRequestDispatcher(JSP_LIST).forward(req, resp);
    }

    private void forwardFormWithError(HttpServletRequest req, HttpServletResponse resp,
                                      String action, String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);

        // reload dropdown parent cho admin
        req.setAttribute("parentCategories", categoryDAO.findAllParents());

        Category c = new Category();

        if (ACT_UPDATE.equals(action)) {
            req.setAttribute("mode", "edit");
            c.setId(safeInt(req.getParameter("id"), 0));
        } else {
            req.setAttribute("mode", "create");
        }

        try { bind(req, c); } catch (Exception ignore) {}
        req.setAttribute("category", c);

        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    /* ===================== BIND / VALIDATE ===================== */

    private void bind(HttpServletRequest req, Category c) {

        String name = safe(req.getParameter("name"));
        c.setName(name);

        // slug: nếu form bỏ trống -> tự tạo
        String slug = safe(req.getParameter("slug"));
        if (slug.isBlank()) slug = toSlug(name);
        c.setSlug(slug);

        // parentId: nếu không chọn -> null (danh mục cha)
        String parentStr = req.getParameter("parentId");
        if (parentStr == null || parentStr.trim().isEmpty()) {
            c.setParentId(null);
        } else {
            int pid = safeInt(parentStr, 0);
            c.setParentId(pid > 0 ? pid : null);
        }

        // active: form có thể gửi "1" hoặc "true"
        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"));
        c.setActive(active);
    }

    private void validate(Category c) {
        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Tên danh mục không được để trống.");

        if (c.getSlug() == null || c.getSlug().isBlank())
            throw new IllegalArgumentException("Slug không hợp lệ. Vui lòng nhập tên danh mục khác.");

        // chỉ check khi update (id > 0), và so sánh Integer đúng cách
        if (c.getId() > 0 && c.getParentId() != null && c.getParentId().intValue() == c.getId())
            throw new IllegalArgumentException("Danh mục cha không hợp lệ.");
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

    private String toSlug(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase();

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        s = s.replace("đ", "d");
        s = s.replaceAll("[^a-z0-9\\s-]", "");
        s = s.replaceAll("\\s+", "-").replaceAll("-{2,}", "-");

        return s;
    }
}
