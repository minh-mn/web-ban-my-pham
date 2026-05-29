package com.webshop.app.controller.OrderController;

import com.webshop.app.dao.CancelRequestDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.model.CancelRequest;
import com.webshop.app.model.Order;
import com.webshop.app.model.User;
import com.webshop.app.service.OrderNotificationService;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/orders/cancel")
public class CancelOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final OrderDAO orderDAO = new OrderDAO();
    private final CancelRequestDAO cancelRequestDAO = new CancelRequestDAO();
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
        String reason = normalizeReason(request.getParameter("reason"));

        if (orderId <= 0) {
            redirectWithMessage(request, response, "/orders", "error", "Mã đơn hàng không hợp lệ.");
            return;
        }

        if (reason == null || reason.length() < 5) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Vui lòng nhập lý do hủy đơn ít nhất 5 ký tự."
            );
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Order order = orderDAO.findById(connection, orderId);
                if (order == null || order.getUserId() != user.getId()) {
                    connection.rollback();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                if (!order.isCancelable()) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này không còn đủ điều kiện hủy. Chỉ có thể gửi yêu cầu trước khi đơn bắt đầu giao hàng."
                    );
                    return;
                }

                if (cancelRequestDAO.existsActiveByOrderId(connection, orderId)) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này đã có yêu cầu hủy đang chờ ADMIN xử lý."
                    );
                    return;
                }

                boolean paidVnpay = "VNPAY".equalsIgnoreCase(order.getPaymentMethod())
                        && "PAID".equalsIgnoreCase(order.getPaymentStatus());

                CancelRequest cancelRequest = new CancelRequest();
                cancelRequest.setOrderId(orderId);
                cancelRequest.setUserId(user.getId());
                cancelRequest.setReason(reason);
                cancelRequest.setStatus("REQUESTED");
                cancelRequest.setRefundAmount(paidVnpay ? order.getTotal() : BigDecimal.ZERO);
                cancelRequest.setRefundMethod(paidVnpay ? "VNPAY" : null);

                cancelRequestDAO.create(connection, cancelRequest);
                notificationService.notifyCancelRequestedSafely(connection, orderId, user, reason);

                connection.commit();

                redirectWithMessage(
                        request,
                        response,
                        "/orders/detail?id=" + orderId,
                        "success",
                        "Yêu cầu hủy đơn đã được gửi. ADMIN sẽ xác nhận và phản hồi cho bạn."
                );
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServletException("CancelOrderServlet error", e);
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

    private String normalizeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return null;
        }
        String value = reason.trim();
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
