package com.webshop.app.controller.OrderController;

import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.User;
import com.webshop.app.service.OrderNotificationService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders/confirm-received")
public class ConfirmReceivedServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderNotificationService notificationService = new OrderNotificationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int orderId = parseInt(request.getParameter("orderId"), -1);
        String note = normalizeNote(request.getParameter("note"));

        if (orderId <= 0) {
            redirectWithMessage(request, response, "/orders", "error", "Mã đơn hàng không hợp lệ.");
            return;
        }

        boolean updated = orderDAO.confirmReceivedByUser(orderId, user.getId(), note);

        if (updated) {
            notificationService.notifyReceivedConfirmedSafely(orderId, user.getId(), false);
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "success",
                    "Cảm ơn bạn đã xác nhận đã nhận hàng."
            );
        } else {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Không thể xác nhận nhận hàng. Đơn hàng có thể chưa giao thành công hoặc đã được xác nhận trước đó."
            );
        }
    }

    private int parseInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String normalizeNote(String note) {
        if (note == null || note.trim().isEmpty()) {
            return "Khách hàng xác nhận đã nhận hàng.";
        }
        String value = note.trim();
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private void redirectWithMessage(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String path,
                                     String type,
                                     String message) throws IOException {
        String separator = path.contains("?") ? "&" : "?";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path + separator + type + "=" + encoded);
    }
}
