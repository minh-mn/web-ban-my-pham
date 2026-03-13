package com.mycosmeticshop.filter;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/*
 * Filter chống tấn công CSRF (Cross-Site Request Forgery)
 *
 * Chức năng:
 * - Tạo CSRF token và lưu trong session
 * - Cho phép các request an toàn như GET / HEAD / OPTIONS đi qua
 * - Kiểm tra CSRF token với các request thay đổi dữ liệu như POST / PUT / DELETE
 *
 * Cách hoạt động:
 * - Mỗi session có 1 token CSRF
 * - Form gửi lên phải kèm tham số csrf_token
 * - Nếu token không hợp lệ -> trả về HTTP 403
 */
public class CsrfFilter implements Filter {

    // Tên key lưu token trong session
    public static final String CSRF_SESSION_KEY = "CSRF_TOKEN";

    // Tên parameter gửi từ form
    public static final String CSRF_PARAM = "csrf_token";

    // Các HTTP method được xem là "safe", không cần kiểm tra CSRF
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    // Bộ sinh số ngẫu nhiên an toàn để tạo token
    private final SecureRandom random = new SecureRandom();

    /*
     * Kiểm tra CSRF token cho request hiện tại
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // =====================================================
        // 1) LẤY / TẠO SESSION
        // =====================================================
        HttpSession session = req.getSession(true);

        // =====================================================
        // 2) LẤY / TẠO CSRF TOKEN TRONG SESSION
        // =====================================================
        String token = (String) session.getAttribute(CSRF_SESSION_KEY);

        if (token == null || token.isBlank()) {
            token = generate();
            session.setAttribute(CSRF_SESSION_KEY, token);
        }

        // =====================================================
        // 3) BỎ QUA KIỂM TRA VỚI METHOD AN TOÀN
        // =====================================================
        String method = req.getMethod().toUpperCase();

        if (SAFE.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        // =====================================================
        // 4) KIỂM TRA CSRF TOKEN TỪ REQUEST
        // =====================================================
        String reqToken = req.getParameter(CSRF_PARAM);

        if (reqToken == null || !token.equals(reqToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        // =====================================================
        // 5) TOKEN HỢP LỆ -> CHO REQUEST ĐI TIẾP
        // =====================================================
        chain.doFilter(request, response);
    }

    /*
     * Tạo token ngẫu nhiên dạng URL-safe
     *
     * Quy trình:
     * - sinh 32 byte ngẫu nhiên
     * - encode Base64 URL-safe
     * - bỏ padding để token gọn hơn
     */
    private String generate() {
        byte[] b = new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}