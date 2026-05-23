package com.webshop.app.filter;

import java.io.IOException;
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

        // GET, HEAD, OPTIONS: chỉ tạo token rồi cho đi tiếp
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

        String requestToken = req.getParameter(CSRF_PARAM);

        if (requestToken == null || requestToken.isBlank()) {
            requestToken = req.getHeader(CSRF_HEADER);
        }

        if (requestToken == null || requestToken.isBlank() || !sessionToken.equals(requestToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        chain.doFilter(request, response);
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

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}