package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

    private static final long serialVersionUID = 1L;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final UserRankDAO rankDAO = new UserRankDAO();

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

            case "detail": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    redirectUsersWithError(req, resp, "ID user không hợp lệ.");
                    return;
                }

                User user = userDAO.findById(id);

                if (user == null) {
                    redirectUsersWithError(req, resp, "Không tìm thấy user.");
                    return;
                }

                attachCurrentRank(user);

                req.setAttribute("user", user);
                req.setAttribute("ranks", rankDAO.findAllActive());

                req.getRequestDispatcher("/jsp/admin/user/user_detail.jsp").forward(req, resp);
                return;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    redirectUsersWithError(req, resp, "ID user không hợp lệ.");
                    return;
                }

                User user = userDAO.findById(id);

                if (user == null) {
                    redirectUsersWithError(req, resp, "Không tìm thấy user.");
                    return;
                }

                attachCurrentRank(user);

                req.setAttribute("mode", "edit");
                req.setAttribute("user", user);
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
        User currentAdmin = currentUser(req);

        switch (action) {

            /*
             * Issue 130:
             * Admin chỉ được cập nhật các trường quản trị:
             * - role
             * - active
             * - manualRankCode
             *
             * Admin không được tự ý sửa thông tin cá nhân:
             * - fullName
             * - email
             * - phone
             *
             * Nếu sau này có bảng yêu cầu từ user, phần sửa thông tin cá nhân
             * nên chuyển qua luồng duyệt request riêng.
             */
            case "updateInfo": {
                handleUpdateAdminManagedFields(req, resp, id, currentAdmin);
                return;
            }

            /*
             * Không cho admin đổi mật khẩu tài khoản khác.
             * Tạm thời chỉ cho phép đổi mật khẩu chính tài khoản đang đăng nhập.
             */
            case "changePassword": {
                handleChangePassword(req, resp, id, currentAdmin);
                return;
            }

            /*
             * Giữ tương thích form cũ.
             * Chỉ cho đổi rank user thường, không cho thao tác với ADMIN.
             */
            case "changeRank": {
                handleChangeRank(req, resp, id, currentAdmin);
                return;
            }

            /*
             * Issue 130:
             * Không xóa cứng user nữa.
             * Action delete sẽ chuyển thành khóa tài khoản user thường.
             */
            case "delete": {
                handleSoftDelete(req, resp, id, currentAdmin);
                return;
            }

            /*
             * Giữ tương thích form cũ.
             * Có kiểm tra quyền để tránh admin tự đổi role chính mình
             * hoặc chỉnh tài khoản ADMIN khác.
             */
            case "changeRole": {
                handleChangeRole(req, resp, id, currentAdmin);
                return;
            }

            /*
             * Khóa / mở khóa tài khoản user thường.
             * Không cho khóa chính mình hoặc khóa tài khoản ADMIN.
             */
            case "toggleLock": {
                handleToggleLock(req, resp, id, currentAdmin);
                return;
            }

            default:
                redirectBackWithError(req, resp, "Thao tác không hợp lệ.");
        }
    }

    /* ===================== POST HANDLERS ===================== */

    private void handleUpdateAdminManagedFields(HttpServletRequest req,
                                                HttpServletResponse resp,
                                                int id,
                                                User currentAdmin)
            throws ServletException, IOException {

        if (id <= 0) {
            redirectUsersWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectUsersWithError(req, resp, "Không tìm thấy user cần cập nhật.");
            return;
        }

        if (personalInfoChanged(req, target)) {
            setRequestError(req,
                    "Admin không được tự ý sửa họ tên, email hoặc số điện thoại của user. "
                            + "Nếu user yêu cầu chỉnh sửa, cần xử lý qua luồng yêu cầu riêng.");
            forwardEdit(req, resp, id);
            return;
        }

        String role = normalizeRole(req.getParameter("role"));
        if (role == null) {
            setRequestError(req, "Role không hợp lệ. Chỉ chấp nhận ADMIN hoặc USER.");
            forwardEdit(req, resp, id);
            return;
        }

        String manualRankCode = normalizeManualRank(req.getParameter("manualRankCode"));
        boolean active = parseActive(req.getParameter("active"));

        if (isSelf(currentAdmin, target)) {
            if (!sameText(target.getRole(), role)) {
                setRequestError(req, "Admin không được tự thay đổi role của chính mình.");
                forwardEdit(req, resp, id);
                return;
            }

            if (!active) {
                setRequestError(req, "Admin không được tự khóa tài khoản của chính mình.");
                forwardEdit(req, resp, id);
                return;
            }
        }

        if (isAdmin(target) && !isSelf(currentAdmin, target)) {
            setRequestError(req, "Không được chỉnh role, rank hoặc trạng thái của tài khoản ADMIN khác.");
            forwardEdit(req, resp, id);
            return;
        }

        User update = buildAdminUpdateUser(target, role, active, manualRankCode);
        boolean ok = userDAO.updateInfoAdmin(update);

        if (ok) {
            setRequestSuccess(req, "Đã cập nhật role, trạng thái và rank của user.");
        } else {
            setRequestError(req, "Cập nhật thất bại.");
        }

        forwardEdit(req, resp, id);
    }

    private void handleChangePassword(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      int id,
                                      User currentAdmin)
            throws ServletException, IOException {

        if (id <= 0) {
            redirectUsersWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectUsersWithError(req, resp, "Không tìm thấy user.");
            return;
        }

        if (!isSelf(currentAdmin, target)) {
            setRequestError(req,
                    "Admin không được tự ý đổi mật khẩu tài khoản khác. "
                            + "Nếu user quên mật khẩu, nên dùng luồng yêu cầu đặt lại mật khẩu riêng.");
            forwardEdit(req, resp, id);
            return;
        }

        String newPassword = safe(req.getParameter("newPassword"));
        String confirm = safe(req.getParameter("confirmPassword"));

        if (newPassword.isBlank() || newPassword.length() < 6) {
            setRequestError(req, "Mật khẩu mới phải từ 6 ký tự trở lên.");
            forwardEdit(req, resp, id);
            return;
        }

        if (!newPassword.equals(confirm)) {
            setRequestError(req, "Xác nhận mật khẩu không khớp.");
            forwardEdit(req, resp, id);
            return;
        }

        boolean ok = userDAO.updatePasswordAdmin(id, newPassword);

        if (ok) {
            setRequestSuccess(req, "Đã đổi mật khẩu tài khoản hiện tại.");
        } else {
            setRequestError(req, "Đổi mật khẩu thất bại.");
        }

        forwardEdit(req, resp, id);
    }

    private void handleChangeRank(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  int id,
                                  User currentAdmin)
            throws IOException {

        if (id <= 0) {
            redirectBackWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectBackWithError(req, resp, "Không tìm thấy user.");
            return;
        }

        if (isAdmin(target)) {
            redirectBackWithError(req, resp, "Không được đổi rank thủ công của tài khoản ADMIN.");
            return;
        }

        String manualRankCode = normalizeManualRank(req.getParameter("manualRankCode"));
        boolean ok = userDAO.updateManualRank(id, manualRankCode);

        if (ok) {
            redirectBackWithSuccess(req, resp, "Đã cập nhật rank thủ công của user.");
        } else {
            redirectBackWithError(req, resp, "Cập nhật rank thất bại.");
        }
    }

    private void handleSoftDelete(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  int id,
                                  User currentAdmin)
            throws IOException {

        if (id <= 0) {
            redirectBackWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectBackWithError(req, resp, "Không tìm thấy user cần xử lý.");
            return;
        }

        if (isSelf(currentAdmin, target)) {
            redirectBackWithError(req, resp, "Admin không được xóa hoặc khóa chính tài khoản của mình.");
            return;
        }

        if (isAdmin(target)) {
            redirectBackWithError(req, resp, "Không được xóa hoặc khóa tài khoản ADMIN.");
            return;
        }

        User update = buildAdminUpdateUser(target, ROLE_USER, false, target.getManualRankCode());
        boolean ok = userDAO.updateInfoAdmin(update);

        if (ok) {
            redirectBackWithSuccess(req, resp,
                    "Đã khóa tài khoản user. Hệ thống không xóa cứng dữ liệu để bảo toàn lịch sử đơn hàng.");
        } else {
            redirectBackWithError(req, resp, "Khóa tài khoản thất bại.");
        }
    }

    private void handleChangeRole(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  int id,
                                  User currentAdmin)
            throws IOException {

        if (id <= 0) {
            redirectBackWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectBackWithError(req, resp, "Không tìm thấy user.");
            return;
        }

        String role = normalizeRole(req.getParameter("role"));
        if (role == null) {
            redirectBackWithError(req, resp, "Role không hợp lệ. Chỉ chấp nhận ADMIN hoặc USER.");
            return;
        }

        if (isSelf(currentAdmin, target)) {
            redirectBackWithError(req, resp, "Admin không được tự thay đổi role của chính mình.");
            return;
        }

        if (isAdmin(target)) {
            redirectBackWithError(req, resp, "Không được thay đổi role của tài khoản ADMIN khác.");
            return;
        }

        boolean ok = userDAO.updateRole(id, role);

        if (ok) {
            redirectBackWithSuccess(req, resp, "Đã cập nhật role của user.");
        } else {
            redirectBackWithError(req, resp, "Cập nhật role thất bại.");
        }
    }

    private void handleToggleLock(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  int id,
                                  User currentAdmin)
            throws IOException {

        if (id <= 0) {
            redirectBackWithError(req, resp, "ID user không hợp lệ.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectBackWithError(req, resp, "Không tìm thấy user.");
            return;
        }

        if (isSelf(currentAdmin, target)) {
            redirectBackWithError(req, resp, "Admin không được tự khóa hoặc mở khóa tài khoản của chính mình.");
            return;
        }

        if (isAdmin(target)) {
            redirectBackWithError(req, resp, "Không được khóa hoặc mở khóa tài khoản ADMIN.");
            return;
        }

        boolean ok = userDAO.toggleLock(id);

        if (ok) {
            redirectBackWithSuccess(req, resp, "Đã cập nhật trạng thái khóa/mở khóa của user.");
        } else {
            redirectBackWithError(req, resp, "Cập nhật trạng thái tài khoản thất bại.");
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

        if (user.getManualRankCode() != null && !user.getManualRankCode().isBlank()) {
            rank = rankDAO.findByCode(user.getManualRankCode());
        }

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

    /* ===================== SECURITY / RULE HELPERS ===================== */

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionUser = session.getAttribute("user");
        if (sessionUser instanceof User) {
            return (User) sessionUser;
        }

        sessionUser = session.getAttribute("authUser");
        if (sessionUser instanceof User) {
            return (User) sessionUser;
        }

        sessionUser = session.getAttribute("currentUser");
        if (sessionUser instanceof User) {
            return (User) sessionUser;
        }

        return null;
    }

    private boolean isSelf(User currentAdmin, User target) {
        return currentAdmin != null
                && target != null
                && currentAdmin.getId() > 0
                && currentAdmin.getId() == target.getId();
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equalsIgnoreCase(safe(user.getRole()));
    }

    private boolean personalInfoChanged(HttpServletRequest req, User target) {
        if (target == null) {
            return false;
        }

        if (hasParameter(req, "fullName")
                && !sameText(nullify(req.getParameter("fullName")), target.getFullName())) {
            return true;
        }

        if (hasParameter(req, "email")
                && !sameText(nullify(req.getParameter("email")), target.getEmail())) {
            return true;
        }

        return hasParameter(req, "phone")
                && !sameText(nullify(req.getParameter("phone")), target.getPhone());
    }

    private User buildAdminUpdateUser(User source, String role, boolean active, String manualRankCode) {
        User update = new User();

        update.setId(source.getId());

        /*
         * Giữ nguyên thông tin cá nhân.
         * Admin không được sửa trực tiếp các trường này trong issue 130.
         */
        update.setFullName(source.getFullName());
        update.setEmail(source.getEmail());
        update.setPhone(source.getPhone());

        update.setRole(role == null || role.isBlank() ? source.getRole() : role.toUpperCase());
        update.setActive(active);
        update.setManualRankCode(normalizeManualRank(manualRankCode));

        return update;
    }

    private String normalizeRole(String value) {
        String role = safe(value).toUpperCase();

        if (ROLE_ADMIN.equals(role)) {
            return ROLE_ADMIN;
        }

        if (ROLE_USER.equals(role)) {
            return ROLE_USER;
        }

        return null;
    }

    private String normalizeManualRank(String value) {
        String rank = nullify(value);

        if (rank == null) {
            return null;
        }

        if ("AUTO".equalsIgnoreCase(rank)) {
            return null;
        }

        return rank.toUpperCase();
    }

    private boolean parseActive(String value) {
        String active = safe(value);

        return "1".equals(active)
                || "true".equalsIgnoreCase(active)
                || "on".equalsIgnoreCase(active)
                || "ACTIVE".equalsIgnoreCase(active);
    }

    /* ===================== FORWARD HELPERS ===================== */

    private void forwardEdit(HttpServletRequest req, HttpServletResponse resp, int id)
            throws ServletException, IOException {

        User fresh = userDAO.findById(id);

        if (fresh == null) {
            redirectUsersWithError(req, resp, "Không tìm thấy user.");
            return;
        }

        attachCurrentRank(fresh);

        req.setAttribute("mode", "edit");
        req.setAttribute("user", fresh);
        req.setAttribute("ranks", rankDAO.findAllActive());

        req.getRequestDispatcher("/jsp/admin/user/user_form.jsp").forward(req, resp);
    }

    /* ===================== REDIRECT / FLASH HELPERS ===================== */

    private void redirectUsersWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {

        setFlashError(req, message);
        resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/admin/users"));
    }

    private void redirectBackWithSuccess(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {

        setFlashSuccess(req, message);
        redirectBack(req, resp);
    }

    private void redirectBackWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {

        setFlashError(req, message);
        redirectBack(req, resp);
    }

    private void redirectBack(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String back = req.getHeader("Referer");

        if (back != null && back.contains("/admin/users")) {
            resp.sendRedirect(resp.encodeRedirectURL(back));
        } else {
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/admin/users"));
        }
    }

    private void setFlashSuccess(HttpServletRequest req, String message) {
        req.getSession().setAttribute("success", message);
        req.getSession().setAttribute("successMessage", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute("error", message);
        req.getSession().setAttribute("errorMessage", message);
    }

    private void moveFlashMessage(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }

        Object success = session.getAttribute("success");
        Object successMessage = session.getAttribute("successMessage");
        Object error = session.getAttribute("error");
        Object errorMessage = session.getAttribute("errorMessage");

        if (success != null) {
            req.setAttribute("success", success);
            req.setAttribute("successMessage", success);
            session.removeAttribute("success");
        } else if (successMessage != null) {
            req.setAttribute("success", successMessage);
            req.setAttribute("successMessage", successMessage);
        }

        if (successMessage != null) {
            req.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("errorMessage", error);
            session.removeAttribute("error");
        } else if (errorMessage != null) {
            req.setAttribute("error", errorMessage);
            req.setAttribute("errorMessage", errorMessage);
        }

        if (errorMessage != null) {
            req.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
    }

    private void setRequestSuccess(HttpServletRequest req, String message) {
        req.setAttribute("success", message);
        req.setAttribute("successMessage", message);
    }

    private void setRequestError(HttpServletRequest req, String message) {
        req.setAttribute("error", message);
        req.setAttribute("errorMessage", message);
    }

    /* ===================== BASIC HELPERS ===================== */

    private int safeInt(String value, int defaultValue) {
        try {
            if (value == null) {
                return defaultValue;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(trimmed);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullify(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasParameter(HttpServletRequest req, String name) {
        return req.getParameterMap().containsKey(name);
    }

    private boolean sameText(String a, String b) {
        return Objects.equals(nullify(a), nullify(b));
    }
}
