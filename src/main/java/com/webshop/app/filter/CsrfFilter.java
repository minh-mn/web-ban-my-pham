package com.webshop.app.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

public class CsrfFilter implements Filter {

    public static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    public static final String CSRF_PARAM = "csrf_token";
    public static final String CSRF_HEADER = "X-CSRF-TOKEN";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final SecureRandom random = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_KEY);

        if (sessionToken == null || sessionToken.isBlank()) {
            sessionToken = generateToken();
            session.setAttribute(CSRF_SESSION_KEY, sessionToken);
        }

        String method = req.getMethod().toUpperCase();

        /*
         * GET, HEAD, OPTIONS:
         * Chỉ tạo token rồi cho request đi tiếp.
         */
        if (SAFE_METHODS.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * Chỉ kiểm tra CSRF cho admin và orders.
         *
         * Tạm thời KHÔNG kiểm tra:
         * - /cart/add
         * - /cart/select-checkout
         * - /checkout
         *
         * Vì các form cart/checkout hiện chưa đồng bộ CSRF hoàn toàn.
         */
        if (!requiresCsrf(req)) {
            chain.doFilter(request, response);
            return;
        }

        String requestToken = getRequestToken(req);

        if (requestToken == null
                || requestToken.isBlank()
                || !isSameToken(sessionToken, requestToken)) {

            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        chain.doFilter(request, response);
    }

    /*
     * Lấy CSRF token theo thứ tự:
     * 1. request parameter csrf_token
     * 2. request header X-CSRF-TOKEN
     * 3. multipart part csrf_token
     *
     * Lý do cần multipart part:
     * Form upload ảnh brand/banner dùng enctype="multipart/form-data".
     * Một số trường hợp getParameter("csrf_token") không đọc được token multipart,
     * dẫn tới lỗi 403 CSRF invalid.
     */
    private String getRequestToken(HttpServletRequest req) {
        String token = req.getParameter(CSRF_PARAM);

        if (token != null && !token.isBlank()) {
            return token.trim();
        }

        token = req.getHeader(CSRF_HEADER);

        if (token != null && !token.isBlank()) {
            return token.trim();
        }

        if (isMultipart(req)) {
            token = readTokenFromMultipart(req);
        }

        return token == null ? null : token.trim();
    }

    private String readTokenFromMultipart(HttpServletRequest req) {
        try {
            for (Part part : req.getParts()) {
                if (!CSRF_PARAM.equals(part.getName())) {
                    continue;
                }

                /*
                 * Token CSRF rất ngắn.
                 * Nếu part quá lớn thì bỏ qua để tránh đọc dữ liệu không cần thiết.
                 */
                if (part.getSize() <= 0 || part.getSize() > 2048) {
                    return null;
                }

                byte[] bytes = part.getInputStream().readAllBytes();

                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();

        return contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    private boolean requiresCsrf(HttpServletRequest req) {
        String path = req.getServletPath();

        if (path == null || path.isBlank()) {
            return false;
        }

        return path.startsWith("/admin/")
                || path.equals("/orders")
                || path.startsWith("/orders/");
    }

    private boolean isSameToken(String sessionToken, String requestToken) {
        if (sessionToken == null || requestToken == null) {
            return false;
        }

        byte[] sessionBytes = sessionToken.getBytes(StandardCharsets.UTF_8);
        byte[] requestBytes = requestToken.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(sessionBytes, requestBytes);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}