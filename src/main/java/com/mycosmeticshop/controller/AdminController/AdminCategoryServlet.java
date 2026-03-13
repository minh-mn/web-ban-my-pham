package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.CategoryDAO;
import com.mycosmeticshop.model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    // DAO thao tác với dữ liệu danh mục trong database
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /* ======================================================
       CONSTANTS
       Khai báo hằng số để tránh hard-code nhiều nơi
       ====================================================== */

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

        // Thiết lập UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy action từ URL
        String action = req.getParameter("action");
        if (action == null) {
            action = ACT_LIST;
        }

        switch (action) {

            // =========================
            // HIỂN THỊ FORM TẠO MỚI DANH MỤC
            // =========================
            case ACT_NEW: {
                req.setAttribute("mode", "create");

                // Danh sách danh mục cha để đổ vào dropdown
                req.setAttribute("parentCategories", categoryDAO.findAllParents());

                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            // =========================
            // HIỂN THỊ FORM CHỈNH SỬA DANH MỤC
            // =========================
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

                // Đổ danh mục cha vào form
                req.setAttribute("parentCategories", categoryDAO.findAllParents());

                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH DANH MỤC
            // =========================
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

        // Lấy action từ form submit
        String action = req.getParameter("action");
        if (action == null) {
            action = ACT_CREATE;
        }

        try {
            switch (action) {

                // =========================
                // TẠO MỚI DANH MỤC
                // =========================
                case ACT_CREATE: {
                    Category category = new Category();

                    bind(req, category);
                    validate(category);

                    categoryDAO.create(category);
                    break;
                }

                // =========================
                // CẬP NHẬT DANH MỤC
                // =========================
                case ACT_UPDATE: {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) {
                        break;
                    }

                    Category category = new Category();
                    category.setId(id);

                    bind(req, category);
                    validate(category);

                    categoryDAO.update(category);
                    break;
                }

                // =========================
                // XÓA DANH MỤC
                // =========================
                case ACT_DELETE: {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        categoryDAO.delete(id);
                    }
                    break;
                }

                default: {
                    // action lạ thì bỏ qua và quay lại list
                    break;
                }
            }

            // Sau khi xử lý xong thì quay lại trang danh sách
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return;

        } catch (IllegalArgumentException ex) {
            // Lỗi validate -> quay lại form và giữ dữ liệu đã nhập
            forwardFormWithError(req, resp, action, ex.getMessage());
            return;

        } catch (Exception ex) {
            // Lỗi bất ngờ -> log ra console và báo lỗi chung
            ex.printStackTrace();
            forwardFormWithError(req, resp, action, "Có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    /* ======================================================
       VIEW HELPERS
       Các hàm hỗ trợ hiển thị dữ liệu lên JSP
       ====================================================== */

    private void renderList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lấy toàn bộ danh mục
        List<Category> all = categoryDAO.findAll();

        // childrenMap: map từ parentId -> danh sách con
        Map<Integer, List<Category>> childrenMap = new LinkedHashMap<>();

        // parents: danh sách danh mục cha
        List<Category> parents = new ArrayList<>();

        for (Category category : all) {
            if (category.getParentId() == null) {
                parents.add(category);
            } else {
                childrenMap.computeIfAbsent(category.getParentId(), k -> new ArrayList<>())
                        .add(category);
            }
        }

        // Sắp xếp theo tên cho đẹp
        parents.sort(Comparator.comparing(c -> safe(c.getName()), String.CASE_INSENSITIVE_ORDER));

        for (List<Category> children : childrenMap.values()) {
            children.sort(Comparator.comparing(c -> safe(c.getName()), String.CASE_INSENSITIVE_ORDER));
        }

        req.setAttribute("parents", parents);
        req.setAttribute("childrenMap", childrenMap);

        req.getRequestDispatcher(JSP_LIST).forward(req, resp);
    }

    private void forwardFormWithError(HttpServletRequest req, HttpServletResponse resp,
                                      String action, String message)
            throws ServletException, IOException {

        // Gửi message lỗi sang JSP
        req.setAttribute("error", message);

        // Load lại danh mục cha để form không bị trống dropdown
        req.setAttribute("parentCategories", categoryDAO.findAllParents());

        Category category = new Category();

        if (ACT_UPDATE.equals(action)) {
            req.setAttribute("mode", "edit");
            category.setId(safeInt(req.getParameter("id"), 0));
        } else {
            req.setAttribute("mode", "create");
        }

        // Bind lại dữ liệu cũ để người dùng không phải nhập lại
        try {
            bind(req, category);
        } catch (Exception ignore) {
        }

        req.setAttribute("category", category);

        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    /* ======================================================
       BIND / VALIDATE
       ====================================================== */

    private void bind(HttpServletRequest req, Category category) {

        // Tên danh mục
        String name = safe(req.getParameter("name"));
        category.setName(name);

        // Slug: nếu bỏ trống thì tự sinh từ tên
        String slug = safe(req.getParameter("slug"));
        if (slug.isBlank()) {
            slug = toSlug(name);
        }
        category.setSlug(slug);

        // parentId: nếu không chọn thì là danh mục cha
        String parentStr = req.getParameter("parentId");
        if (parentStr == null || parentStr.trim().isEmpty()) {
            category.setParentId(null);
        } else {
            int parentId = safeInt(parentStr, 0);
            category.setParentId(parentId > 0 ? parentId : null);
        }

        // Active: form có thể gửi "1" hoặc "true"
        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"));

        category.setActive(active);
    }

    private void validate(Category category) {

        // Kiểm tra tên danh mục
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }

        // Kiểm tra slug
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            throw new IllegalArgumentException("Slug không hợp lệ. Vui lòng nhập tên danh mục khác.");
        }

        // Không cho chọn chính nó làm danh mục cha
        if (category.getId() > 0
                && category.getParentId() != null
                && category.getParentId().intValue() == category.getId()) {
            throw new IllegalArgumentException("Danh mục cha không hợp lệ.");
        }
    }

    /* ======================================================
       HELPERS
       Các hàm xử lý dữ liệu an toàn
       ====================================================== */

    // Parse int an toàn
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

    // Chuyển chuỗi tiếng Việt có dấu thành slug không dấu
    private String toSlug(String input) {
        if (input == null) {
            return "";
        }

        String s = input.trim().toLowerCase();

        // Bỏ dấu tiếng Việt
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Chuyển đ -> d
        s = s.replace("đ", "d");

        // Bỏ ký tự đặc biệt
        s = s.replaceAll("[^a-z0-9\\s-]", "");

        // Đổi khoảng trắng thành dấu gạch ngang
        s = s.replaceAll("\\s+", "-").replaceAll("-{2,}", "-");

        return s;
    }
}