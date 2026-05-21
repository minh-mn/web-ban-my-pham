package com.webshop.app.controller.AdminController;

import java.io.IOException;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.Order;
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

    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final String CSRF_PARAM = "csrf_token";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

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

        if (!isValidCsrf(req, session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF invalid");
            return;
        }

        int orderId = safeInt(req.getParameter("orderId"), -1);
        String status = normalizeStatus(req.getParameter("status"));

        if (orderId <= 0 || status.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid params");
            return;
        }

        if (!OrderStatus.isValidKey(status)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
            return;
        }

        try {
            Order order = orderDAO.findById(orderId);

            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }

            String paymentStatus = resolvePaymentStatusByOrderStatus(
                    status,
                    order.getPaymentStatus()
            );

            orderDAO.updateStatusAndPaymentStatus(orderId, status, paymentStatus);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
            return;
        }

        String returnUrl = req.getParameter("returnUrl");
        String target = safeReturnUrl(returnUrl, req.getContextPath());

        resp.sendRedirect(req.getContextPath() + target);
    }

    private String resolvePaymentStatusByOrderStatus(String status, String currentPaymentStatus) {

        String normalizedStatus = normalizeStatus(status);
        String safeCurrentPaymentStatus = normalizePaymentStatus(currentPaymentStatus);

        return switch (normalizedStatus) {
            case "completed" -> "PAID";
            case "cancelled", "canceled" -> "CANCELED";
            default -> safeCurrentPaymentStatus;
        };
    }

    private String normalizeStatus(String status) {

        if (status == null) {
            return "";
        }

        return status.trim().toLowerCase();
    }

    private String normalizePaymentStatus(String paymentStatus) {

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return "PENDING";
        }

        return paymentStatus.trim().toUpperCase();
    }

    private boolean isValidCsrf(HttpServletRequest req, HttpSession session) {

        if (session == null) {
            return false;
        }

        String token = (String) session.getAttribute(CSRF_SESSION_KEY);
        String sent = req.getParameter(CSRF_PARAM);

        return token != null && sent != null && token.equals(sent);
    }

    private int safeInt(String value, int fallback) {

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String safeReturnUrl(String returnUrl, String contextPath) {

        if (returnUrl == null || returnUrl.isBlank() || "null".equalsIgnoreCase(returnUrl)) {
            return "/orders";
        }

        String url = returnUrl.trim();

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return "/orders";
        }

        if (contextPath != null && !contextPath.isBlank() && url.startsWith(contextPath + "/")) {
            url = url.substring(contextPath.length());
        }

        if (!url.startsWith("/")) {
            return "/orders";
        }

        return url;
    }
}