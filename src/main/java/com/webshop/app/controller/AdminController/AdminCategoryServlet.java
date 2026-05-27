package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.model.Category;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ===== JSP PATH =====
    private static final String JSP_LIST = "/jsp/admin/category/category_list.jsp";
    private static final String JSP_FORM = "/jsp/admin/category/category_form.jsp";

    // ===== GET ACTION =====
    private static final String ACT_LIST = "list";
    private static final String ACT_NEW = "new";
    private static final String ACT_EDIT = "edit";

    // ===== POST ACTION =====
    private static final String ACT_CREATE = "create";
    private static final String ACT_UPDATE = "update";
    private static final String ACT_DELETE = "delete";

    // ===== FLASH KEY =====
    private static final String FLASH_SUCCESS = "categorySuccess";
    private static final String FLASH_ERROR = "categoryError";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupEncoding(req, resp);

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = ACT_LIST;
        }

        switch (action) {

            case ACT_NEW:
                showCreateForm(req, resp);
                break;

            case ACT_EDIT:
                showEditForm(req, resp);
                break;

            case ACT_LIST:
            default:
                renderList(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupEncoding(req, resp);

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = ACT_CREATE;
        }

        try {
            switch (action) {

                case ACT_CREATE:
                    handleCreate(req);
                    setFlash(req, FLASH_SUCCESS, "Thêm danh mục thành công.");
                    redirectToList(req, resp);
                    break;

                case ACT_UPDATE:
                    handleUpdate(req);
                    setFlash(req, FLASH_SUCCESS, "Cập nhật danh mục thành công.");
                    redirectToList(req, resp);
                    break;

                case ACT_DELETE:
                    handleDelete(req);
                    setFlash(req, FLASH_SUCCESS, "Xóa danh mục thành công.");
                    redirectToList(req, resp);
                    break;

                default:
                    setFlash(req, FLASH_ERROR, "Thao tác không hợp lệ.");
                    redirectToList(req, resp);
                    break;
            }

        } catch (IllegalArgumentException ex) {
            forwardFormWithError(req, resp, action, ex.getMessage());

        } catch (RuntimeException ex) {
            handleRuntimeError(req, resp, action, ex);

        } catch (Exception ex) {
            ex.printStackTrace();
            forwardFormWithError(req, resp, action, "Có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    /* =====================================================
       GET VIEW
    ===================================================== */

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("mode", "create");
        req.setAttribute("category", new Category());
        req.setAttribute("parentCategories", categoryDAO.findAllParents());

        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = safeInt(req.getParameter("id"), -1);

        if (id <= 0) {
            setFlash(req, FLASH_ERROR, "ID danh mục không hợp lệ.");
            redirectToList(req, resp);
            return;
        }

        Category category = categoryDAO.findById(id);

        if (category == null) {
            setFlash(req, FLASH_ERROR, "Danh mục không tồn tại hoặc đã bị xóa.");
            redirectToList(req, resp);
            return;
        }

        req.setAttribute("mode", "edit");
        req.setAttribute("category", category);
        req.setAttribute("parentCategories", getAvailableParentCategories(id));

        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    private void renderList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        pullFlash(req);

        List<Category> allCategories = categoryDAO.findAll();

        Map<Integer, List<Category>> childrenMap = new LinkedHashMap<>();
        List<Category> parents = new ArrayList<>();

        for (Category category : allCategories) {

            if (category.getParentId() == null) {
                parents.add(category);
            } else {
                childrenMap
                        .computeIfAbsent(category.getParentId(), key -> new ArrayList<>())
                        .add(category);
            }
        }

        Comparator<Category> byName = Comparator.comparing(
                category -> safe(category.getName()),
                String.CASE_INSENSITIVE_ORDER
        );

        parents.sort(byName);

        for (List<Category> children : childrenMap.values()) {
            children.sort(byName);
        }

        req.setAttribute("categories", allCategories);
        req.setAttribute("parents", parents);
        req.setAttribute("childrenMap", childrenMap);

        req.getRequestDispatcher(JSP_LIST).forward(req, resp);
    }

    /* =====================================================
       POST HANDLER
    ===================================================== */

    private void handleCreate(HttpServletRequest req) {

        Category category = new Category();

        bind(req, category);
        validate(category, false);

        categoryDAO.create(category);
    }

    private void handleUpdate(HttpServletRequest req) {

        int id = safeInt(req.getParameter("id"), -1);

        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }

        Category category = new Category();

        category.setId(id);
        bind(req, category);
        validate(category, true);

        categoryDAO.update(category);
    }

    private void handleDelete(HttpServletRequest req) {

        int id = safeInt(req.getParameter("id"), -1);

        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }

        categoryDAO.delete(id);
    }

    /* =====================================================
       FORM ERROR
    ===================================================== */

    private void forwardFormWithError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String action,
            String message
    ) throws ServletException, IOException {

        req.setAttribute("error", message);

        Category category = new Category();

        if (ACT_UPDATE.equals(action)) {
            category.setId(safeInt(req.getParameter("id"), 0));
            req.setAttribute("mode", "edit");
            req.setAttribute("parentCategories", getAvailableParentCategories(category.getId()));
        } else {
            req.setAttribute("mode", "create");
            req.setAttribute("parentCategories", categoryDAO.findAllParents());
        }

        try {
            bind(req, category);
        } catch (Exception ignored) {
            // Nếu dữ liệu form lỗi nặng thì vẫn forward form với message phía trên.
        }

        req.setAttribute("category", category);

        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    private void handleRuntimeError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String action,
            RuntimeException ex
    ) throws ServletException, IOException {

        ex.printStackTrace();

        String message = safe(ex.getMessage());
        if (message.isBlank()) {
            message = "Có lỗi xảy ra. Vui lòng thử lại.";
        }

        if (ACT_DELETE.equals(action)) {
            setFlash(req, FLASH_ERROR, message);
            redirectToList(req, resp);
            return;
        }

        forwardFormWithError(req, resp, action, message);
    }

    /* =====================================================
       BIND / VALIDATE
    ===================================================== */

    private void bind(HttpServletRequest req, Category category) {

        String name = safe(req.getParameter("name"));
        String slug = safe(req.getParameter("slug"));

        if (slug.isBlank()) {
            slug = toSlug(name);
        } else {
            slug = toSlug(slug);
        }

        Integer parentId = parseParentId(req.getParameter("parentId"));

        boolean active = isChecked(req.getParameter("active"));

        category.setName(name);
        category.setSlug(slug);
        category.setParentId(parentId);
        category.setActive(active);
    }

    private void validate(Category category, boolean updating) {

        if (category == null) {
            throw new IllegalArgumentException("Dữ liệu danh mục không hợp lệ.");
        }

        if (updating && category.getId() <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }

        if (isBlank(category.getName())) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }

        if (category.getName().length() > 100) {
            throw new IllegalArgumentException("Tên danh mục không được vượt quá 100 ký tự.");
        }

        if (isBlank(category.getSlug())) {
            throw new IllegalArgumentException("Slug không hợp lệ. Vui lòng nhập tên danh mục khác.");
        }

        if (category.getSlug().length() > 150) {
            throw new IllegalArgumentException("Slug không được vượt quá 150 ký tự.");
        }

        if (!category.getSlug().matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("Slug chỉ được chứa chữ thường, số và dấu gạch ngang.");
        }

        if (updating
                && category.getParentId() != null
                && category.getParentId().intValue() == category.getId()) {

            throw new IllegalArgumentException("Không thể chọn chính danh mục này làm danh mục cha.");
        }
    }

    /* =====================================================
       CATEGORY HELPER
    ===================================================== */

    private List<Category> getAvailableParentCategories(int currentCategoryId) {

        List<Category> parentCategories = categoryDAO.findAllParents();
        List<Category> availableParents = new ArrayList<>();

        for (Category parent : parentCategories) {

            if (parent.getId() != currentCategoryId) {
                availableParents.add(parent);
            }
        }

        return availableParents;
    }

    private Integer parseParentId(String value) {

        if (isBlank(value)) {
            return null;
        }

        int parentId = safeInt(value, 0);

        return parentId > 0 ? parentId : null;
    }

    /* =====================================================
       FLASH / REDIRECT
    ===================================================== */

    private void setFlash(HttpServletRequest req, String key, String message) {

        HttpSession session = req.getSession();

        session.setAttribute(key, message);
    }

    private void pullFlash(HttpServletRequest req) {

        HttpSession session = req.getSession(false);

        if (session == null) {
            return;
        }

        Object success = session.getAttribute(FLASH_SUCCESS);
        Object error = session.getAttribute(FLASH_ERROR);

        if (success != null) {
            req.setAttribute("success", success.toString());
            session.removeAttribute(FLASH_SUCCESS);
        }

        if (error != null) {
            req.setAttribute("error", error.toString());
            session.removeAttribute(FLASH_ERROR);
        }
    }

    private void redirectToList(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    /* =====================================================
       COMMON HELPER
    ===================================================== */

    private void setupEncoding(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
    }

    private int safeInt(String value, int defaultValue) {

        try {
            if (value == null) {
                return defaultValue;
            }

            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isChecked(String value) {

        if (value == null) {
            return false;
        }

        String normalized = value.trim();

        return "1".equals(normalized)
                || "true".equalsIgnoreCase(normalized)
                || "on".equalsIgnoreCase(normalized)
                || "yes".equalsIgnoreCase(normalized);
    }

    private String toSlug(String input) {

        if (input == null) {
            return "";
        }

        String slug = input.trim().toLowerCase();

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        slug = slug.replace("đ", "d");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-+", "");
        slug = slug.replaceAll("-+$", "");

        return slug;
    }
}