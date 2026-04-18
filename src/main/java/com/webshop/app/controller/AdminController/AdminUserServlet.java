package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.AdminUserDAO;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final AdminUserDAO userDAO = new AdminUserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {

            case "detail": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                User u = userDAO.findById(id);
                if (u == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                req.setAttribute("user", u);
                req.getRequestDispatcher("/jsp/admin/user/user_detail.jsp").forward(req, resp);
                return;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                User u = userDAO.findById(id);
                if (u == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("user", u);

                req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
                return;
            }

            case "list":
            default: {
                String q = safe(req.getParameter("q"));
                String role = safe(req.getParameter("role"));

                // Optional filter active: ?active=1/0
                String activeParam = safe(req.getParameter("active"));
                Integer active = null;

                if ("1".equals(activeParam)) active = 1;
                else if ("0".equals(activeParam)) active = 0;

                // search(q, role, phone?, active) - bạn đang truyền "" cho tham số thứ 3
                List<User> users = userDAO.search(q, role, "", active);

                req.setAttribute("users", users);
                req.setAttribute("f_q", q);
                req.setAttribute("f_role", role);
                req.setAttribute("f_active", activeParam);

                req.getRequestDispatcher("/jsp/admin/user/user_list.jsp").forward(req, resp);
                return;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "";

        // Nếu post từ form edit, ưu tiên quay lại trang edit để thấy thông báo
        int id = safeInt(req.getParameter("id"), -1);

        switch (action) {

            /* ================= UPDATE INFO (FULL_NAME/EMAIL/PHONE/ROLE/ACTIVE) ================= */
            case "updateInfo": {
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                String fullName = nullify(req.getParameter("fullName"));
                String email = nullify(req.getParameter("email"));
                String phone = nullify(req.getParameter("phone"));

                String role = safe(req.getParameter("role"));
                String activeParam = safe(req.getParameter("active"));
                boolean active = "1".equals(activeParam) || "true".equalsIgnoreCase(activeParam);

                // validate role
                if (!("ADMIN".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role))) {
                    req.setAttribute("error", "Role không hợp lệ. Chỉ chấp nhận ADMIN | USER.");
                    forwardEdit(req, resp, id);
                    return;
                }

                User u = new User();
                u.setId(id);
                u.setFullName(fullName);
                u.setEmail(email);
                u.setPhone(phone);
                u.setRole(role.toUpperCase());
                u.setActive(active);

                boolean ok = userDAO.updateInfoAdmin(u);
                if (ok) req.setAttribute("success", "Đã cập nhật thông tin user.");
                else req.setAttribute("error", "Cập nhật thất bại.");

                forwardEdit(req, resp, id);
                return;
            }

            /* ================= CHANGE PASSWORD (ADMIN RESET) ================= */
            case "changePassword": {
                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                String newPassword = safe(req.getParameter("newPassword"));
                String confirm = safe(req.getParameter("confirmPassword"));

                if (newPassword.isBlank() || newPassword.length() < 6) {
                    req.setAttribute("error", "Mật khẩu mới phải >= 6 ký tự.");
                    forwardEdit(req, resp, id);
                    return;
                }
                if (!newPassword.equals(confirm)) {
                    req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
                    forwardEdit(req, resp, id);
                    return;
                }

                boolean ok = userDAO.updatePasswordAdmin(id, newPassword);
                if (ok) req.setAttribute("success", "Đã đổi mật khẩu.");
                else req.setAttribute("error", "Đổi mật khẩu thất bại.");

                forwardEdit(req, resp, id);
                return;
            }

            /* ================= DELETE USER (NEW) ================= */
            case "delete": {
                int uid = safeInt(req.getParameter("id"), -1);
                if (uid <= 0) {
                    redirectBack(req, resp);
                    return;
                }

                // ✅ Không cho tự xóa chính mình
                HttpSession session = req.getSession(false);
                User current = (session != null) ? (User) session.getAttribute("user") : null;
                if (current != null && current.getId() == uid) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users?err=cannot_delete_self");
                    return;
                }

                try {
                    boolean ok = userDAO.deleteById(uid); // ✅ bạn phải implement trong AdminUserDAO
                    if (ok) resp.sendRedirect(req.getContextPath() + "/admin/users?msg=deleted");
                    else resp.sendRedirect(req.getContextPath() + "/admin/users?err=delete_failed");
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendRedirect(req.getContextPath() + "/admin/users?err=delete_failed");
                }
                return;
            }

            /* ================= BACKWARD COMPAT (OLD ACTION) ================= */
            case "changeRole": {
                int uid = safeInt(req.getParameter("id"), -1);
                String role = safe(req.getParameter("role"));
                if (uid > 0 && !role.isBlank()) {
                    userDAO.updateRole(uid, role);
                }
                redirectBack(req, resp);
                return;
            }

            case "toggleLock": {
                int uid = safeInt(req.getParameter("id"), -1);
                if (uid > 0) {
                    userDAO.toggleLock(uid);
                }
                redirectBack(req, resp);
                return;
            }

            default:
                redirectBack(req, resp);
        }
    }

    /* ===================== HELPERS ===================== */

    private void forwardEdit(HttpServletRequest req, HttpServletResponse resp, int id)
            throws ServletException, IOException {

        User fresh = userDAO.findById(id);
        if (fresh == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        req.setAttribute("mode", "edit");
        req.setAttribute("user", fresh);
        req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
    }

    private void redirectBack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String back = req.getHeader("Referer");
        if (back != null && back.contains("/admin/users")) resp.sendRedirect(back);
        else resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

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

    private String nullify(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
