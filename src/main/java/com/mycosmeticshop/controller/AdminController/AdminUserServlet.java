package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.AdminUserDAO;
import com.mycosmeticshop.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    // DAO dùng để thao tác dữ liệu user phía admin
    private final AdminUserDAO userDAO = new AdminUserDAO();

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
            // XEM CHI TIẾT USER
            // =========================
            case "detail": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                User user = userDAO.findById(id);

                if (user == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                req.setAttribute("user", user);
                req.getRequestDispatcher("/jsp/admin/user/user_detail.jsp").forward(req, resp);
                return;
            }

            // =========================
            // HIỂN THỊ FORM SỬA USER
            // =========================
            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                User user = userDAO.findById(id);

                if (user == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("user", user);

                req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
                return;
            }

            // =========================
            // HIỂN THỊ DANH SÁCH USER
            // Hỗ trợ filter theo q / role / active
            // =========================
            case "list":
            default: {
                String q = safe(req.getParameter("q"));
                String role = safe(req.getParameter("role"));

                // Optional filter active: ?active=1 / 0
                String activeParam = safe(req.getParameter("active"));
                Integer active = null;

                if ("1".equals(activeParam)) {
                    active = 1;
                } else if ("0".equals(activeParam)) {
                    active = 0;
                }

                // search(q, role, phone, active)
                // hiện tại truyền "" cho phone để giữ tương thích với DAO cũ
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }

        // Nếu post từ form edit thì ưu tiên quay lại trang edit để thấy thông báo
        int id = safeInt(req.getParameter("id"), -1);

        switch (action) {

            /* ======================================================
               UPDATE THÔNG TIN USER
               FULL_NAME / EMAIL / PHONE / ROLE / ACTIVE
               ====================================================== */
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

                // Validate role
                if (!("ADMIN".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role))) {
                    req.setAttribute("error", "Role không hợp lệ. Chỉ chấp nhận ADMIN | USER.");
                    forwardEdit(req, resp, id);
                    return;
                }

                User user = new User();
                user.setId(id);
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPhone(phone);
                user.setRole(role.toUpperCase());
                user.setActive(active);

                boolean ok = userDAO.updateInfoAdmin(user);

                if (ok) {
                    req.setAttribute("success", "Đã cập nhật thông tin user.");
                } else {
                    req.setAttribute("error", "Cập nhật thất bại.");
                }

                forwardEdit(req, resp, id);
                return;
            }

            /* ======================================================
               ADMIN ĐỔI / RESET MẬT KHẨU CHO USER
               ====================================================== */
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

                if (ok) {
                    req.setAttribute("success", "Đã đổi mật khẩu.");
                } else {
                    req.setAttribute("error", "Đổi mật khẩu thất bại.");
                }

                forwardEdit(req, resp, id);
                return;
            }

            /* ======================================================
               XÓA USER
               Không cho admin tự xóa chính mình
               ====================================================== */
            case "delete": {
                int uid = safeInt(req.getParameter("id"), -1);

                if (uid <= 0) {
                    redirectBack(req, resp);
                    return;
                }

                HttpSession session = req.getSession(false);
                User current = (session != null) ? (User) session.getAttribute("user") : null;

                // Không cho tự xóa chính mình
                if (current != null && current.getId() == uid) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users?err=cannot_delete_self");
                    return;
                }

                try {
                    boolean ok = userDAO.deleteById(uid);

                    if (ok) {
                        resp.sendRedirect(req.getContextPath() + "/admin/users?msg=deleted");
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/admin/users?err=delete_failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendRedirect(req.getContextPath() + "/admin/users?err=delete_failed");
                }
                return;
            }

            /* ======================================================
               BACKWARD COMPAT
               HỖ TRỢ CÁC ACTION CŨ
               ====================================================== */

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

    /* ======================================================
       HELPER METHODS
       ====================================================== */

    // Forward lại trang edit sau khi update / change password
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

    // Redirect về trang trước nếu là trang admin/users, không thì về list
    private void redirectBack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String back = req.getHeader("Referer");

        if (back != null && back.contains("/admin/users")) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

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

    // Nếu chuỗi rỗng thì trả về null
    private String nullify(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}