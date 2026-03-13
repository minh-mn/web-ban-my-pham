package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.service.RememberMeService;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 * Servlet xử lý chức năng đăng xuất
 * URL truy cập: /logout
 *
 * Chức năng:
 * - Xóa remember-me token mới (remember_tokens)
 * - Dọn token cũ legacy (user_tokens)
 * - Xóa cookie remember-me
 * - Hủy session hiện tại
 * - Chuyển hướng về trang đăng nhập
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để dọn token remember cũ (legacy)
    private final UserDAO userDAO = new UserDAO();

    // Service xử lý remember-me mới
    private final RememberMeService rememberMeService = new RememberMeService();

    /*
     * Phương thức POST
     * Thực hiện logout người dùng
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ======================================================
        // 1) LOGOUT REMEMBER-ME MỚI (remember_tokens + cookie REMEMBER_ME)
        // ======================================================
        /*
         * Cơ chế remember-me mới:
         * - Lưu token trong bảng remember_tokens
         * - Cookie phía client: REMEMBER_ME
         *
         * logoutByRequest():
         * - Revoke token trong database
         * - Xóa cookie REMEMBER_ME
         */
        rememberMeService.logoutByRequest(req, resp);

        // ======================================================
        // 2) CLEANUP REMEMBER-ME LEGACY (user_tokens + REMEMBER_TOKEN)
        // ======================================================
        /*
         * Các phiên bản cũ của hệ thống có thể dùng:
         * - bảng user_tokens
         * - cookie REMEMBER_TOKEN
         *
         * Vì vậy cần dọn sạch để tránh xung đột cơ chế đăng nhập.
         */
        String legacyToken = getCookieValue(req, "REMEMBER_TOKEN");

        if (legacyToken != null && !legacyToken.isBlank()) {
            try {
                // Xóa token cũ trong database
                userDAO.deleteRememberToken(legacyToken);
            } catch (Exception ignored) {
                // Không cần xử lý nếu lỗi xảy ra
            }
        }

        // Xóa cookie REMEMBER_TOKEN
        clearCookie(resp, "REMEMBER_TOKEN", req.getContextPath());

        // ======================================================
        // 3) HỦY SESSION HIỆN TẠI
        // ======================================================
        /*
         * Sau khi logout cần invalidate session
         * để xóa toàn bộ dữ liệu người dùng trong session
         */
        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        // ======================================================
        // 4) REDIRECT VỀ TRANG LOGIN
        // ======================================================
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /*
     * Hỗ trợ logout bằng GET
     * (ví dụ người dùng truy cập /logout trực tiếp)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Gọi lại doPost để xử lý chung logic logout
        doPost(req, resp);
    }

    /*
     * Lấy giá trị cookie theo tên
     */
    private String getCookieValue(HttpServletRequest req, String name) {

        Cookie[] cookies = req.getCookies();

        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }

        return null;
    }

    /*
     * Xóa cookie chắc chắn
     *
     * Lý do:
     * Cookie trước đây có thể được set với path khác nhau:
     * - "/"
     * - contextPath của ứng dụng
     *
     * Vì vậy cần xóa ở cả hai path
     */
    private void clearCookie(HttpServletResponse resp, String name, String contextPath) {

        // ===== Xóa cookie với path "/" =====
        Cookie c1 = new Cookie(name, "");
        c1.setHttpOnly(true);
        c1.setMaxAge(0);
        c1.setPath("/");
        resp.addCookie(c1);

        // ===== Xóa cookie với path = contextPath =====
        Cookie c2 = new Cookie(name, "");
        c2.setHttpOnly(true);
        c2.setMaxAge(0);
        c2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(c2);
    }
}