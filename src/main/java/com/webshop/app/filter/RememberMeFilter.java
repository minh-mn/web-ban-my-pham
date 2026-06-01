package com.webshop.app.filter;

import java.io.IOException;

import com.webshop.app.model.User;
import com.webshop.app.service.RememberMeService;
import com.webshop.app.utils.CartUtil;

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

    @Override
    public void init(FilterConfig filterConfig) {
        this.rememberMeService = new RememberMeService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(ctx.length());

        // 1) Bỏ qua static assets: css, js, images, fonts...
        if (StaticResourceUtil.isStatic(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Bỏ qua request API/json nếu cần
        String accept = req.getHeader("Accept");

        if (accept != null && accept.contains("application/json")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // Nếu chưa có user trong session thì thử auto-login bằng cookie REMEMBER_ME
        if (currentUser == null) {
            String token = getCookieValue(req, RememberMeService.COOKIE_NAME);

            if (token != null && !token.isBlank()) {
                try {
                    User rememberedUser = rememberMeService.authenticateByToken(token);

                    if (rememberedUser != null) {
                        HttpSession activeSession = req.getSession(true);
                        activeSession.setAttribute("user", rememberedUser);

                        /*
                         * Issue 132:
                         * Khi auto-login bằng Remember Me thành công,
                         * khôi phục và gộp giỏ hàng đã lưu trong database vào session.
                         */
                        if (rememberedUser.getId() > 0) {
                            CartUtil.mergeDatabaseCartIntoSession(activeSession, rememberedUser.getId());
                        }
                    } else {
                        // Token sai, hết hạn hoặc đã revoked thì xóa cookie REMEMBER_ME
                        rememberMeService.clearCookie(resp);
                    }

                } catch (Exception ex) {
                    // Lỗi hệ thống tạm thời thì không xóa cookie để tránh logout nhầm user
                    System.out.println("[RememberMeFilter] token auth error: " + ex.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
