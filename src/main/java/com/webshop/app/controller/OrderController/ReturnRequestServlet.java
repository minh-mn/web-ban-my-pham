package com.webshop.app.controller.OrderController;

import com.webshop.app.dao.ReturnRequestDAO;
import com.webshop.app.model.Order;
import com.webshop.app.model.ReturnRequest;
import com.webshop.app.model.User;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Issue 114 - Return request notification
 *
 * File này thay thế cho ReturnOrderServlet nếu bạn muốn dùng đúng tên:
 * src/main/java/com/webshop/app/controller/OrderController/ReturnRequestServlet.java
 *
 * Lưu ý:
 * - Không để đồng thời ReturnOrderServlet và ReturnRequestServlet cùng mapping /orders/return.
 * - Nếu dùng file này thì xóa hoặc đổi mapping file ReturnOrderServlet cũ.
 */
@WebServlet("/orders/return")
public class ReturnRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReturnRequestDAO returnRequestDAO = new ReturnRequestDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null || user.getId() <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int orderId = parseInt(request.getParameter("orderId"), -1);
        String reason = normalizeReason(request.getParameter("reason"));
        String refundMethod = normalizeRefundMethod(request.getParameter("refundMethod"));

        if (orderId <= 0) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders",
                    "error",
                    "Mã đơn hàng không hợp lệ."
            );
            return;
        }

        if (reason == null || reason.length() < 10) {
            redirectWithMessage(
                    request,
                    response,
                    "/orders/detail?id=" + orderId,
                    "error",
                    "Vui lòng nhập lý do hoàn hàng ít nhất 10 ký tự."
            );
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                OrderSnapshot order = findOrderForUpdate(connection, orderId);

                if (order == null || order.userId() != user.getId()) {
                    connection.rollback();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                if (!isReturnable(order)) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này không đủ điều kiện hoàn hàng. Chỉ hỗ trợ hoàn trong 7 ngày sau khi giao thành công và chưa có yêu cầu trước đó."
                    );
                    return;
                }

                if (returnRequestDAO.existsActiveByOrderId(connection, orderId)) {
                    connection.rollback();
                    redirectWithMessage(
                            request,
                            response,
                            "/orders/detail?id=" + orderId,
                            "error",
                            "Đơn hàng này đã có yêu cầu hoàn hàng."
                    );
                    return;
                }

                BigDecimal refundAmount = order.total();

                ReturnRequest returnRequest = new ReturnRequest();
                returnRequest.setOrderId(orderId);
                returnRequest.setUserId(user.getId());
                returnRequest.setReason(reason);
                returnRequest.setStatus("REQUESTED");
                returnRequest.setRefundAmount(refundAmount);
                returnRequest.setRefundMethod(refundMethod);

                /*
                 * Issue 114:
                 * ReturnRequestDAO.create(...) là nơi tạo notification cho:
                 * - Khách: đã gửi yêu cầu hoàn hàng
                 * - Admin: có yêu cầu hoàn hàng mới
                 */
                returnRequestDAO.create(connection, returnRequest);

                /*
                 * Chỉ cập nhật trạng thái refund của đơn hàng tại đây.
                 * Không gọi OrderDAO.markReturnRequested(...) để tránh bị trùng notification
                 * nếu OrderDAO đã được tích hợp notification trước đó.
                 */
                markOrderReturnRequested(
                        connection,
                        orderId,
                        refundAmount,
                        refundMethod
                );

                connection.commit();

                redirectWithMessage(
                        request,
                        response,
                        "/orders/detail?id=" + orderId,
                        "success",
                        "Yêu cầu hoàn hàng đã được gửi. Shop sẽ kiểm tra và phản hồi trong thời gian sớm nhất."
                );

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new ServletException("ReturnRequestServlet error", e);
        }
    }

    /* =========================================================
       ORDER QUERY / UPDATE
    ========================================================= */

    private OrderSnapshot findOrderForUpdate(Connection connection, int orderId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    user_id,
                    total,
                    status,
                    payment_status,
                    shipping_status,
                    delivered_at
                FROM store_order
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new OrderSnapshot(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getBigDecimal("total") == null
                                ? BigDecimal.ZERO
                                : resultSet.getBigDecimal("total"),
                        normalizeStatus(resultSet.getString("status")),
                        normalizePaymentStatus(resultSet.getString("payment_status")),
                        normalizeShippingStatus(resultSet.getString("shipping_status")),
                        resultSet.getTimestamp("delivered_at")
                );
            }
        }
    }

    private void markOrderReturnRequested(Connection connection,
                                          int orderId,
                                          BigDecimal refundAmount,
                                          String refundMethod) throws SQLException {
        String sql = """
                UPDATE store_order
                SET refund_status = ?,
                    refund_amount = ?,
                    refund_method = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "REQUESTED");
            statement.setBigDecimal(2, refundAmount == null ? BigDecimal.ZERO : refundAmount);
            statement.setString(3, refundMethod);
            statement.setInt(4, orderId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       VALIDATION
    ========================================================= */

    private boolean isReturnable(OrderSnapshot order) {
        if (order == null) {
            return false;
        }

        if (!"completed".equals(order.status())) {
            return false;
        }

        if (!"PAID".equals(order.paymentStatus())) {
            return false;
        }

        if (!"DELIVERED".equals(order.shippingStatus())) {
            return false;
        }

        if (order.deliveredAt() == null) {
            return false;
        }

        long deliveredMillis = order.deliveredAt().getTime();
        long nowMillis = System.currentTimeMillis();
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;

        return nowMillis - deliveredMillis <= sevenDaysMillis;
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
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private String normalizeRefundMethod(String refundMethod) {
        if (refundMethod == null || refundMethod.trim().isEmpty()) {
            return "MANUAL";
        }

        String value = refundMethod.trim().toUpperCase();

        return switch (value) {
            case "VNPAY", "BANK_TRANSFER", "CASH", "STORE_CREDIT", "MANUAL" -> value;
            default -> "MANUAL";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "";
        }

        return status.trim().toLowerCase();
    }

    private String normalizePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            return "PENDING";
        }

        String value = paymentStatus.trim().toUpperCase();

        return switch (value) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "CANCELLED", "REFUNDED" ->
                    "CANCELLED".equals(value) ? "CANCELED" : value;
            default -> "PENDING";
        };
    }

    private String normalizeShippingStatus(String shippingStatus) {
        if (shippingStatus == null || shippingStatus.trim().isEmpty()) {
            return "";
        }

        String value = shippingStatus.trim().toUpperCase();

        return "CANCELLED".equals(value) ? "CANCELED" : value;
    }

    private void redirectWithMessage(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String path,
                                     String type,
                                     String message) throws IOException {
        String separator = path.contains("?") ? "&" : "?";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);

        response.sendRedirect(
                request.getContextPath()
                        + path
                        + separator
                        + type
                        + "="
                        + encoded
        );
    }

    private record OrderSnapshot(
            int id,
            int userId,
            BigDecimal total,
            String status,
            String paymentStatus,
            String shippingStatus,
            java.sql.Timestamp deliveredAt
    ) {
    }
}
