package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.OrderStatus;
import com.webshop.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/order/update-status")
public class AdminOrderUpdateStatusServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();

    // Đồng bộ với CsrfFilter/JSP
    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1) Auth + Admin check
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return;
        }

        // 1.5) CSRF check (nếu CSRF filter đã làm rồi thì vẫn OK — check trùng cũng không hại)
        if (!isValidCsrf(req, session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        // 2) Read params
        int orderId = safeInt(req.getParameter("orderId"), -1);
        String statusRaw = req.getParameter("status");

        if (orderId <= 0 || statusRaw == null || statusRaw.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid params");
            return;
        }

        // 3) Validate status
        String normalized = statusRaw.trim();
        if (!OrderStatus.isValidKey(normalized)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
            return;
        }

        // 4) Update DB
        try {
            orderDAO.updateStatus(orderId, normalized);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
            return;
        }

        // 5) Redirect back
        String returnUrl = req.getParameter("returnUrl");
        String target = safeReturnUrl(returnUrl, req.getContextPath());
        resp.sendRedirect(req.getContextPath() + target);
    }

    private boolean isValidCsrf(HttpServletRequest req, HttpSession session) {
        if (session == null) return false;
        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        String sent = req.getParameter(CSRF_PARAM);
        return token != null && sent != null && token.equals(sent);
    }

    private int safeInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String safeReturnUrl(String returnUrl, String ctx) {
        if (returnUrl == null || returnUrl.isBlank() || "null".equalsIgnoreCase(returnUrl)) {
            return "/orders";
        }

        String u = returnUrl.trim();

        if (u.startsWith("http://") || u.startsWith("https://")) {
            return "/orders";
        }

        if (ctx != null && !ctx.isBlank() && u.startsWith(ctx + "/")) {
            u = u.substring(ctx.length());
        }

        if (!u.startsWith("/")) {
            return "/orders";
        }

        return u;
    }
}
