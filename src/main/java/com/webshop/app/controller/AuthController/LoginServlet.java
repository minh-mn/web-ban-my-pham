package com.webshop.app.controller.AuthController;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.service.RememberMeService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();
    private final RememberMeService rememberMeService = new RememberMeService();

    // Cấu hình tính năng khóa tài khoản
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15 phút (tính bằng mili-giây)
    private static final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lockTime = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/account");
            return;
        }

        req.setAttribute("pageTitle", "Đăng nhập");
        req.setAttribute("pageContent", "/jsp/auth/login.jsp");
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String remember = req.getParameter("remember");
        String redirect = req.getParameter("redirect");

        String lowerUsername = (username != null) ? username.trim().toLowerCase() : "";

        // 1. Kiểm tra tài khoản có đang bị khóa hay không
        if (!lowerUsername.isEmpty() && lockTime.containsKey(lowerUsername)) {
            long lockExpired = lockTime.get(lowerUsername) + LOCK_TIME_DURATION;
            if (System.currentTimeMillis() < lockExpired) {
                long secondsLeft = (lockExpired - System.currentTimeMillis()) / 1000;
                long minutesLeft = secondsLeft / 60;
                String timeStr = minutesLeft > 0 ? minutesLeft + " phút" : secondsLeft + " giây";

                sendJsonResponse(resp, "error", "Tài khoản bị khóa do đăng nhập sai quá 5 lần. Thử lại sau " + timeStr + ".", null);
                return;
            } else {
                // Quá thời gian khóa -> Mở khóa tự động
                lockTime.remove(lowerUsername);
                failedAttempts.remove(lowerUsername);
            }
        }

        // 2. Kiểm tra định dạng đầu vào cơ bản
        if (username != null && !username.equals(username.toLowerCase())) {
            sendJsonResponse(resp, "error", "Tên đăng nhập không được phép chứa chữ in hoa.", null);
            return;
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            sendJsonResponse(resp, "error", "Vui lòng nhập đầy đủ thông tin.", null);
            return;
        }

        // 3. Tiến hành xác thực qua DB
        User user = userDAO.login(username.trim(), password);

        if (user == null) {
            // Xử lý tăng số lần đăng nhập sai
            int attempts = failedAttempts.getOrDefault(lowerUsername, 0) + 1;
            failedAttempts.put(lowerUsername, attempts);

            if (attempts >= MAX_ATTEMPTS) {
                lockTime.put(lowerUsername, System.currentTimeMillis());
                sendJsonResponse(resp, "error", "Bạn đã nhập sai quá 5 lần! Tài khoản bị khóa trong 15 phút.", null);
            } else {
                sendJsonResponse(resp, "error", "Sai tên đăng nhập hoặc mật khẩu. Bạn còn " + (MAX_ATTEMPTS - attempts) + " lần thử.", null);
            }
            return;
        }

        if (user.getId() <= 0) {
            sendJsonResponse(resp, "error", "Tài khoản không hợp lệ, không tìm thấy ID người dùng.", null);
            return;
        }

        // Đăng nhập thành công -> Reset bộ đếm sai
        failedAttempts.remove(lowerUsername);
        lockTime.remove(lowerUsername);

        // Khởi tạo session lưu thông tin đăng nhập
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        /*
         * Issue 132:
         * Sau khi đăng nhập thành công, gộp giỏ hàng đã lưu trong database
         * với giỏ hàng hiện có trong session.
         */
        CartUtil.mergeDatabaseCartIntoSession(session, user.getId());

        boolean rememberMe = "on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember);
        if (rememberMe) {
            rememberMeService.rememberLogin(resp, user.getId());
        } else {
            rememberMeService.clearCookie(resp);
        }

        clearLegacyRememberToken(resp, req.getContextPath());

        // Điều hướng sau đăng nhập thành công
        String ctx = req.getContextPath();
        String redirectUrl = ctx + "/";

        if (redirect != null && !redirect.isBlank()) {
            if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                redirectUrl = ctx + redirect;
            }
        }

        sendJsonResponse(resp, "success", "Đăng nhập thành công!", redirectUrl);
    }

    // Hàm tiện ích hỗ trợ gửi phản hồi JSON nhanh chóng, không cần thư viện bên ngoài (Jackson/Gson)
    private void sendJsonResponse(HttpServletResponse resp, String status, String message, String redirectUrl) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":\"").append(status).append("\"");
        if (message != null) {
            json.append(",\"message\":\"").append(message.replace("\"", "\\\"")).append("\"");
        }
        if (redirectUrl != null) {
            json.append(",\"redirectUrl\":\"").append(redirectUrl.replace("\"", "\\\"")).append("\"");
        }
        json.append("}");

        resp.getWriter().write(json.toString());
    }

    private void clearLegacyRememberToken(HttpServletResponse resp, String contextPath) {
        Cookie ck = new Cookie("REMEMBER_TOKEN", "");
        ck.setHttpOnly(true);
        ck.setMaxAge(0);
        ck.setPath("/");
        resp.addCookie(ck);

        Cookie ck2 = new Cookie("REMEMBER_TOKEN", "");
        ck2.setHttpOnly(true);
        ck2.setMaxAge(0);
        ck2.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        resp.addCookie(ck2);
    }
}
