package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.RoleDAO;
import com.webshop.app.model.Permission;
import com.webshop.app.model.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/roles")
public class AdminRoleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        moveFlashMessage(req);

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = "list";
        }

        switch (action) {
            case "create":
                forwardForm(req, resp, new Role(), "create");
                return;

            case "edit": {
                String code = normalizeCode(req.getParameter("code"));
                Role role = roleDAO.findByCode(code);

                if (role == null) {
                    redirectWithError(req, resp, "Không tìm thấy role cần sửa.");
                    return;
                }

                forwardForm(req, resp, role, "edit");
                return;
            }

            case "list":
            default:
                req.setAttribute("roles", roleDAO.findAll());
                req.getRequestDispatcher("/jsp/admin/role/role_list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action"));

        switch (action) {
            case "create":
                handleCreate(req, resp);
                return;

            case "update":
                handleUpdate(req, resp);
                return;

            case "deactivate":
                handleDeactivate(req, resp);
                return;

            default:
                redirectWithError(req, resp, "Thao tác role không hợp lệ.");
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Role role = bindRole(req, false);

        if (role.getCode() == null || role.getName() == null) {
            req.setAttribute("error", "Vui lòng nhập mã role và tên role.");
            forwardForm(req, resp, role, "create");
            return;
        }

        if (roleDAO.findByCode(role.getCode()) != null) {
            req.setAttribute("error", "Mã role đã tồn tại.");
            forwardForm(req, resp, role, "create");
            return;
        }

        boolean ok = roleDAO.create(role);
        if (ok) {
            redirectWithSuccess(req, resp, "Đã tạo role mới.");
        } else {
            req.setAttribute("error", "Tạo role thất bại.");
            forwardForm(req, resp, role, "create");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Role role = bindRole(req, true);

        if (role.getCode() == null || role.getName() == null) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin role.");
            forwardForm(req, resp, role, "edit");
            return;
        }

        Role oldRole = roleDAO.findByCode(role.getCode());
        if (oldRole == null) {
            redirectWithError(req, resp, "Không tìm thấy role cần cập nhật.");
            return;
        }

        role.setSystemRole(oldRole.isSystemRole());
        if (oldRole.isSystemRole()) {
            role.setActive(true);
        }

        boolean ok = roleDAO.update(role);
        if (ok) {
            redirectWithSuccess(req, resp, "Đã cập nhật role và danh sách quyền.");
        } else {
            req.setAttribute("error", "Cập nhật role thất bại.");
            forwardForm(req, resp, role, "edit");
        }
    }

    private void handleDeactivate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String code = normalizeCode(req.getParameter("code"));

        if (code == null) {
            redirectWithError(req, resp, "Mã role không hợp lệ.");
            return;
        }

        boolean ok = roleDAO.deactivate(code);
        if (ok) {
            redirectWithSuccess(req, resp, "Đã tắt role. User đang dùng role này nên được chuyển sang role khác trước khi đăng nhập lại.");
        } else {
            redirectWithError(req, resp, "Không thể tắt role hệ thống hoặc role không tồn tại.");
        }
    }

    private Role bindRole(HttpServletRequest req, boolean updateMode) {
        Role role = new Role();
        role.setCode(normalizeCode(req.getParameter("code")));
        role.setName(nullify(req.getParameter("name")));
        role.setDescription(nullify(req.getParameter("description")));
        role.setActive("1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"))
                || "on".equalsIgnoreCase(req.getParameter("active"))
                || !updateMode);

        String[] permissionCodes = req.getParameterValues("permissions");
        List<String> codes = permissionCodes == null
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(permissionCodes));

        if (!codes.contains("ADMIN_ACCESS") && !codes.isEmpty()) {
            codes.add("ADMIN_ACCESS");
        }

        role.setPermissionCodes(codes);
        return role;
    }

    private void forwardForm(HttpServletRequest req, HttpServletResponse resp, Role role, String mode)
            throws ServletException, IOException {

        List<Permission> permissions = roleDAO.findAllPermissions();

        req.setAttribute("mode", mode);
        req.setAttribute("role", role);
        req.setAttribute("permissions", permissions);
        req.getRequestDispatcher("/jsp/admin/role/role_form.jsp").forward(req, resp);
    }

    private void moveFlashMessage(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }

        Object success = session.getAttribute("success");
        if (success != null) {
            req.setAttribute("success", success);
            session.removeAttribute("success");
        }

        Object error = session.getAttribute("error");
        if (error != null) {
            req.setAttribute("error", error);
            session.removeAttribute("error");
        }
    }

    private void redirectWithSuccess(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        req.getSession(true).setAttribute("success", message);
        resp.sendRedirect(req.getContextPath() + "/admin/roles");
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        req.getSession(true).setAttribute("error", message);
        resp.sendRedirect(req.getContextPath() + "/admin/roles");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullify(String value) {
        String safe = safe(value);
        return safe.isBlank() ? null : safe;
    }

    private String normalizeCode(String value) {
        String safe = safe(value).toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        return safe.isBlank() ? null : safe;
    }
}
