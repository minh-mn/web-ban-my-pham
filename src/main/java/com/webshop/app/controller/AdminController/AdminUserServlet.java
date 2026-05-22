package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.AdminUserDAO;
import com.webshop.app.dao.UserRankDAO;
import com.webshop.app.model.User;
import com.webshop.app.model.UserRank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final UserRankDAO rankDAO = new UserRankDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = "list";
        }

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

                attachCurrentRank(u);

                req.setAttribute("user", u);
                req.setAttribute("ranks", rankDAO.findAllActive());

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

                attachCurrentRank(u);

                req.setAttribute("mode", "edit");
                req.setAttribute("user", u);
                req.setAttribute("ranks", rankDAO.findAllActive());

                req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
                return;
            }

            case "list":
            default: {
                String q = safe(req.getParameter("q"));
                String role = safe(req.getParameter("role"));
                String rank = safe(req.getParameter("rank"));

                String activeParam = safe(req.getParameter("active"));
                Integer active = null;

                if ("1".equals(activeParam)) {
                    active = 1;
                } else if ("0".equals(activeParam)) {
                    active = 0;
                }

                List<User> users = userDAO.search(q, role, rank, active);
                attachCurrentRanks(users);

                req.setAttribute("users", users);
                req.setAttribute("ranks", rankDAO.findAllActive());

                req.setAttribute("f_q", q);
                req.setAttribute("f_role", role);
                req.setAttribute("f_rank", rank);
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

        String action = safe(req.getParameter("action"));
        int id = safeInt(req.getParameter("id"), -1);

        switch (action) {

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

                String manualRankCode = nullify(req.getParameter("manualRankCode"));
                if ("AUTO".equalsIgnoreCase(manualRankCode)) {
                    manualRankCode = null;
                }

                boolean active = "1".equals(activeParam)
                        || "true".equalsIgnoreCase(activeParam)
                        || "on".equalsIgnoreCase(activeParam);

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
                u.setManualRankCode(manualRankCode);

                boolean ok = userDAO.updateInfoAdmin(u);

                if (ok) {
                    req.setAttribute("success", "Đã cập nhật thông tin user.");
                } else {
                    req.setAttribute("error", "Cập nhật thất bại.");
                }

                forwardEdit(req, resp, id);
                return;
            }

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

            /*
             * Giữ lại để tương thích nếu form cũ còn gọi.
             * Nếu user_list.jsp đã bỏ đổi rank nhanh thì case này không ảnh hưởng.
             */
            case "changeRank": {
                int uid = safeInt(req.getParameter("id"), -1);
                String manualRankCode = safe(req.getParameter("manualRankCode"));

                if (uid > 0) {
                    userDAO.updateManualRank(uid, manualRankCode);
                }

                redirectBack(req, resp);
                return;
            }

            case "delete": {
                int uid = safeInt(req.getParameter("id"), -1);

                if (uid <= 0) {
                    redirectBack(req, resp);
                    return;
                }

                HttpSession session = req.getSession(false);
                User current = null;

                if (session != null) {
                    Object sessionUser = session.getAttribute("user");

                    if (sessionUser instanceof User) {
                        current = (User) sessionUser;
                    }
                }

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

            /*
             * Giữ lại để tương thích nếu form cũ còn gọi.
             * Nếu user_list.jsp đã bỏ đổi role nhanh thì case này không ảnh hưởng.
             */
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

    /* ===================== RANK HELPERS ===================== */

    private void attachCurrentRanks(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        for (User user : users) {
            attachCurrentRank(user);
        }
    }

    private void attachCurrentRank(User user) {
        if (user == null) {
            return;
        }

        UserRank rank = null;

        /*
         * Nếu admin đã chỉ định rank trực tiếp:
         * - Dùng rank đó làm rank hiện tại.
         */
        if (user.getManualRankCode() != null && !user.getManualRankCode().isBlank()) {
            rank = rankDAO.findByCode(user.getManualRankCode());
        }

        /*
         * Nếu không có rank thủ công:
         * - Tính rank theo tổng chi tiêu đã thanh toán.
         */
        if (rank == null) {
            rank = rankDAO.findCurrentRankByUserId(user.getId());
        }

        if (rank != null) {
            user.setCurrentRankCode(rank.getCode());
            user.setCurrentRankName(rank.getName());
        } else {
            user.setCurrentRankCode("MEMBER");
            user.setCurrentRankName("Thành viên");
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

        attachCurrentRank(fresh);

        req.setAttribute("mode", "edit");
        req.setAttribute("user", fresh);
        req.setAttribute("ranks", rankDAO.findAllActive());

        req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
    }

    private void redirectBack(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String back = req.getHeader("Referer");

        if (back != null && back.contains("/admin/users")) {
            resp.sendRedirect(back);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

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

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String nullify(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();

        return s.isEmpty() ? null : s;
    }
}