package com.webshop.app.filter;

import java.io.IOException;

import com.webshop.app.model.User;
import com.webshop.app.service.RememberMeService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class RememberMeFilter implements Filter {

    private RememberMeService rememberMeService;

    
    public void init(FilterConfig filterConfig) {
        this.rememberMeService = new RememberMeService();
    }

    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        // 1) Static assets: bỏ qua
        if (StaticResourceUtil.isStatic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Skip API/json (nếu cần)
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            String token = getCookieValue(req, RememberMeService.COOKIE_NAME);
            if (token != null && !token.isBlank()) {
                try {
                    User u = rememberMeService.authenticateByToken(token);
                    if (u != null) {
                        req.getSession(true).setAttribute("user", u);
                    } else {
                        // token hết hạn/sai -> clear cookie
                        rememberMeService.clearCookie(resp);
                    }
                } catch (Exception ex) {
                    // lỗi hệ thống tạm thời -> log, không clear cookie
                    System.out.println("[RememberMeFilter] token auth error: " + ex.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

	public boolean test(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
