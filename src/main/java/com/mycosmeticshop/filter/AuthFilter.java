package com.mycosmeticshop.filter;

import com.mycosmeticshop.model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

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
}
