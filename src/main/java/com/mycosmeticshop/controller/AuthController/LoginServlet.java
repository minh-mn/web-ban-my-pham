package com.mycosmeticshop.controller.AuthController;

import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.User;
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
 * Servlet xử lý chức năng đăng nhập
 * URL truy cập: /login
 *
 * Chức năng:
 * - Hiển thị trang đăng nhập
 * - Kiểm tra thông tin username/password
 * - Tạo session sau khi đăng nhập thành công
 * - Hỗ trợ remember-me bằng cookie
 * - Hỗ trợ redirect về trang trước đó sau khi login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Tránh warning khi servlet được serialize
    private static final long serialVersionUID = 1L;

    // DAO thao tác với dữ liệu người dùng
    private final UserDAO userDAO = new UserDAO();

    // Service xử lý chức năng remember-me
    private final RememberMeService rememberMeService = new RememberMeService();

    /*
     * Phương thức GET
     * Hiển thị trang đăng nhập
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy session hiện tại, không tạo mới nếu chưa có
        HttpSession session = req.getSession(false);

        // Nếu người dùng đã đăng nhập thì chuyển về trang tài khoản
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/account");
            return;
        }

        // Thiết lập thông tin trang để render giao diện
        req.setAttribute("pageTitle", "Đăng nhập");
        req.setAttribute("pageContent", "/jsp/auth/login.jsp");

        // Forward đến layout chính
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /*
     * Phương thức POST
     * Xử lý đăng nhập người dùng
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Lấy dữ liệu từ form đăng nhập
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // Checkbox remember-me (name="remember")
        String remember = req.getParameter("remember");

        // URL nội bộ cần quay lại sau khi đăng nhập thành công
        // Ví dụ: /checkout, /cart, /account
        String redirect = req.getParameter("redirect");

        // ===== KIỂM TRA DỮ LIỆU ĐẦU VÀO =====
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin");
            req.setAttribute("pageTitle", "Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // Thực hiện kiểm tra đăng nhập với database
        User user = userDAO.login(username.trim(), password);

        // Nếu đăng nhập thất bại
        if (user == null) {
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
            req.setAttribute("pageTitle", "MyCosmetic | Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        /*
         * QUAN TRỌNG:
         * user.id phải là khóa chính thực của bảng dbo.users
         * vì các bảng khác có thể dùng users.id làm khóa ngoại
         * Ví dụ: store_order.user_id -> dbo.users.id
         */
        if (user.getId() <= 0) {
            req.setAttribute("error", "Tài khoản không hợp lệ (không tìm thấy users.id).");
            req.setAttribute("pageTitle", "MyCosmetic | Đăng nhập");
            req.setAttribute("pageContent", "/jsp/auth/login.jsp");
            req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
            return;
        }

        // ===== TẠO SESSION SAU KHI ĐĂNG NHẬP THÀNH CÔNG =====
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        // ===== XỬ LÝ REMEMBER-ME =====
        // Checkbox có thể trả về "on" hoặc "true" tùy form
        boolean rememberMe = "on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember);

        if (rememberMe) {
            // Lưu cookie ghi nhớ đăng nhập theo users.id
            rememberMeService.rememberLogin(resp, user.getId());
        } else {
            // Nếu không chọn remember-me thì xóa cookie cũ
            rememberMeService.clearCookie(resp);
        }

        // ===== XÓA COOKIE REMEMBER CŨ (PHIÊN BẢN LEGACY) =====
        // Nếu project trước đây dùng cookie "REMEMBER_TOKEN"
        // thì xóa để tránh xung đột hoặc gây nhiễu logic đăng nhập
        clearLegacyRememberToken(resp, req.getContextPath());

        // ===== XỬ LÝ REDIRECT SAU KHI ĐĂNG NHẬP =====
        String ctx = req.getContextPath();

        if (redirect != null && !redirect.isBlank()) {

            /*
             * Chặn open redirect:
             * chỉ cho phép redirect nội bộ trong website
             * - hợp lệ: /checkout
             * - không hợp lệ: //abc.com hoặc https://abc.com
             */
            if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                resp.sendRedirect(ctx + redirect);
                return;
            }
        }

        // Nếu không có redirect hợp lệ thì quay về trang chủ
        resp.sendRedirect(ctx + "/");
    }

    /*
     * Hàm xóa cookie remember cũ tên REMEMBER_TOKEN
     *
     * Lý do:
     * Cookie trước đây có thể được set với path khác nhau:
     * - "/"
     * - contextPath của ứng dụng
     *
     * Vì vậy cần xóa ở cả 2 path để chắc chắn cookie biến mất hoàn toàn
     */
    private void clearLegacyRememberToken(HttpServletResponse resp, String contextPath) {

        // Xóa cookie với path "/"
        Cookie ck = new Cookie("REMEMBER_TOKEN", "");
        ck.setHttpOnly(true);
        ck.setMaxAge(0);
        ck.setPath("/");
        resp.addCookie(ck);

        // Xóa cookie với path = contextPath
        Cookie ck2 = new Cookie("REMEMBER_TOKEN", "");
        ck2.setHttpOnly(true);
        ck2.setMaxAge(0);
        ck2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(ck2);
    }
}