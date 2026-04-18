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
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    private final SecureRandom random = new SecureRandom();

    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);

        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (token == null || token.isBlank()) {
            token = generate();
            session.setAttribute(CSRF_SESSION_KEY, token);
        }

        String method = req.getMethod().toUpperCase();
        if (SAFE.contains(method)) {
            chain.doFilter(request, response);
            return;
        }

        String reqToken = req.getParameter(CSRF_PARAM);
        if (reqToken == null || !token.equals(reqToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        chain.doFilter(request, response);
    }

    private String generate() {
        byte[] b = new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

	public boolean test(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
