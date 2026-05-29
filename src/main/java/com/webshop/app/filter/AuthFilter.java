package com.webshop.app.filter;

import java.io.IOException;

import com.webshop.app.model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        if (uri.equals("/login") ||
            uri.equals("/register") ||
                uri.equals("/send-otp") ||
                uri.equals("/verify-otp") ||
				uri.equals("/forgot-password") || 
                uri.equals("/reset-password") ||
                uri.equals("/css") ||
                uri.equals("/js") ||
                uri.equals("/images")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            String ctx = req.getContextPath();
            String returnUrl = req.getRequestURI();
            String qs = req.getQueryString();
            if (qs != null && !qs.isBlank()) returnUrl += "?" + qs;

            resp.sendRedirect(ctx + "/login?returnUrl=" + urlEncode(returnUrl));
            return;
        }

        chain.doFilter(request, response);
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

	public boolean test(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
