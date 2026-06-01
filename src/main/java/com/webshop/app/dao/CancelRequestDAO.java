package com.webshop.app.dao;

import com.webshop.app.model.CancelRequest;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CancelRequestDAO {

    private static final String SELECT_COLUMNS = """
            cr.id,
            cr.order_id,
            cr.user_id,
            cr.reason,
            cr.status,
            cr.admin_note,
            cr.refund_amount,
            cr.refund_method,
            cr.requested_at,
            cr.processed_at,
            cr.processed_by,
            u.username,
            COALESCE(u.full_name, o.full_name) AS customer_name,
            o.total AS order_total,
            o.payment_method,
            o.payment_status,
            o.status AS order_status,
            o.shipping_status
            """;

    public long create(Connection conn, CancelRequest request) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }
        if (request == null) {
            throw new SQLException("CancelRequest must not be null");
        }

        String sql = """
                INSERT INTO store_cancel_request
                (
                    order_id,
                    user_id,
                    reason,
                    status,
                    admin_note,
                    refund_amount,
                    refund_method,
                    requested_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            statement.setLong(index++, request.getOrderId());
            statement.setInt(index++, request.getUserId());
            statement.setString(index++, trimToDefault(request.getReason(), "Khách hàng yêu cầu hủy đơn."));
            statement.setString(index++, normalizeStatus(request.getStatus()));
            statement.setString(index++, trimToNull(request.getAdminNote()));
            statement.setBigDecimal(index++, vnd0(request.getRefundAmount()));
            statement.setString(index++, normalizeRefundMethod(request.getRefundMethod()));
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    long createdId = resultSet.getLong(1);

                    notifyCancelRequestCreatedSafely(
                            conn,
                            request,
                            createdId
                    );

                    return createdId;
                }
            }
        }

        return 0;
    }

    public CancelRequest findById(long id) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_cancel_request cr
                JOIN store_order o ON o.id = cr.order_id
                JOIN users u ON u.id = cr.user_id
                WHERE cr.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("CancelRequestDAO.findById error", e);
        }
    }

    public CancelRequest findByOrderId(long orderId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_cancel_request cr
                JOIN store_order o ON o.id = cr.order_id
                JOIN users u ON u.id = cr.user_id
                WHERE cr.order_id = ?
                ORDER BY cr.id DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("CancelRequestDAO.findByOrderId error", e);
        }
    }

    public List<CancelRequest> findAll() {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_cancel_request cr
                JOIN store_order o ON o.id = cr.order_id
                JOIN users u ON u.id = cr.user_id
                ORDER BY cr.requested_at DESC, cr.id DESC
                """;

        List<CancelRequest> requests = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                requests.add(mapRow(resultSet));
            }

            return requests;
        } catch (SQLException e) {
            throw new RuntimeException("CancelRequestDAO.findAll error", e);
        }
    }

    public boolean existsActiveByOrderId(Connection conn, long orderId) throws SQLException {
        String sql = """
                SELECT 1
                FROM store_cancel_request
                WHERE order_id = ?
                  AND UPPER(status) = 'REQUESTED'
                LIMIT 1
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean existsActiveByOrderId(long orderId) {
        try (Connection connection = DBConnection.getConnection()) {
            return existsActiveByOrderId(connection, orderId);
        } catch (SQLException e) {
            throw new RuntimeException("CancelRequestDAO.existsActiveByOrderId error", e);
        }
    }

    public boolean updateStatus(long id,
                                String status,
                                String adminNote,
                                BigDecimal refundAmount,
                                String refundMethod,
                                Integer processedBy) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedRefundMethod = normalizeRefundMethod(refundMethod);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                CancelRequest current = findByIdForUpdate(connection, id);
                if (current == null) {
                    connection.rollback();
                    return false;
                }

                BigDecimal finalRefundAmount = refundAmount == null
                        ? current.getRefundAmount()
                        : vnd0(refundAmount);

                String finalRefundMethod = normalizedRefundMethod == null
                        ? current.getRefundMethod()
                        : normalizedRefundMethod;

                updateCancelRequestStatus(
                        connection,
                        id,
                        normalizedStatus,
                        adminNote,
                        finalRefundAmount,
                        finalRefundMethod,
                        processedBy
                );

                if ("APPROVED".equals(normalizedStatus)) {
                    approveOrderCancellation(
                            connection,
                            current.getOrderId(),
                            current.getReason(),
                            finalRefundAmount,
                            finalRefundMethod
                    );
                }

                notifyCancelRequestResultSafely(
                        connection,
                        current,
                        normalizedStatus
                );

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("CancelRequestDAO.updateStatus error", e);
        }
    }

    private CancelRequest findByIdForUpdate(Connection connection, long id) throws SQLException {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_cancel_request cr
                JOIN store_order o ON o.id = cr.order_id
                JOIN users u ON u.id = cr.user_id
                WHERE cr.id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRow(resultSet);
            }
        }
    }

    private void updateCancelRequestStatus(Connection connection,
                                           long id,
                                           String status,
                                           String adminNote,
                                           BigDecimal refundAmount,
                                           String refundMethod,
                                           Integer processedBy) throws SQLException {
        String sql = """
                UPDATE store_cancel_request
                SET status = ?,
                    admin_note = ?,
                    refund_amount = ?,
                    refund_method = ?,
                    processed_by = ?,
                    processed_at = NOW()
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, normalizeStatus(status));
            statement.setString(index++, trimToNull(adminNote));
            statement.setBigDecimal(index++, vnd0(refundAmount));
            statement.setString(index++, normalizeRefundMethod(refundMethod));
            if (processedBy == null || processedBy <= 0) {
                statement.setNull(index++, java.sql.Types.INTEGER);
            } else {
                statement.setInt(index++, processedBy);
            }
            statement.setLong(index++, id);
            statement.executeUpdate();
        }
    }

    private void approveOrderCancellation(Connection connection,
                                          long orderId,
                                          String reason,
                                          BigDecimal refundAmount,
                                          String refundMethod) throws SQLException {
        String lockSql = """
                SELECT payment_method, payment_status, total
                FROM store_order
                WHERE id = ?
                FOR UPDATE
                """;

        String paymentMethod;
        String paymentStatus;
        BigDecimal total;

        try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Không tìm thấy đơn hàng #" + orderId);
                }
                paymentMethod = resultSet.getString("payment_method");
                paymentStatus = resultSet.getString("payment_status");
                total = vnd0(resultSet.getBigDecimal("total"));
            }
        }

        boolean paidOnline = "VNPAY".equalsIgnoreCase(paymentMethod) && "PAID".equalsIgnoreCase(paymentStatus);
        BigDecimal finalRefundAmount = refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0
                ? (paidOnline ? total : BigDecimal.ZERO)
                : vnd0(refundAmount);
        String finalRefundMethod = paidOnline
                ? (normalizeRefundMethod(refundMethod) == null ? "VNPAY" : normalizeRefundMethod(refundMethod))
                : null;

        String sql = """
                UPDATE store_order
                SET status = 'cancelled',
                    shipping_status = 'CANCELED',
                    cancel_reason = ?,
                    cancelled_at = NOW(),
                    refund_status = ?,
                    refund_amount = ?,
                    refund_method = ?,
                    payment_status = CASE
                        WHEN UPPER(COALESCE(payment_status, 'PENDING')) = 'PENDING' THEN 'CANCELED'
                        ELSE payment_status
                    END
                WHERE id = ?
                  AND UPPER(COALESCE(shipping_status, 'PENDING_PICKUP')) IN ('PENDING_PICKUP', 'PENDING', 'CREATED', 'PICKING')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trimToNull(reason));
            statement.setString(2, paidOnline ? "PENDING" : "NONE");
            statement.setBigDecimal(3, finalRefundAmount);
            statement.setString(4, finalRefundMethod);
            statement.setLong(5, orderId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       NOTIFICATION HELPERS - ISSUE 114
    ========================================================= */

    private void notifyCancelRequestCreatedSafely(Connection connection,
                                                  CancelRequest request,
                                                  long cancelRequestId) {
        if (connection == null
                || request == null
                || cancelRequestId <= 0
                || request.getOrderId() <= 0
                || request.getUserId() <= 0) {
            return;
        }

        try {
            NotificationDAO notificationDAO = new NotificationDAO();

            notificationDAO.createUserNotification(
                    connection,
                    request.getUserId(),
                    "CANCEL_REQUEST_CREATED",
                    "Đã gửi yêu cầu hủy đơn",
                    "Yêu cầu hủy đơn hàng #" + request.getOrderId() + " của bạn đã được ghi nhận.",
                    "/orders/detail?id=" + request.getOrderId(),
                    "CANCEL_REQUEST",
                    cancelRequestId
            );

            notificationDAO.createAdminNotification(
                    connection,
                    "CANCEL_REQUEST_CREATED",
                    "Có yêu cầu hủy đơn mới",
                    "Khách hàng #" + request.getUserId()
                            + " đã gửi yêu cầu hủy đơn hàng #" + request.getOrderId()
                            + buildReasonText(request.getReason()),
                    "/admin/orders?action=detail&id=" + request.getOrderId(),
                    "CANCEL_REQUEST",
                    cancelRequestId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm hỏng thao tác tạo yêu cầu hủy.
             */
            e.printStackTrace();
        }
    }

    private void notifyCancelRequestResultSafely(Connection connection,
                                                 CancelRequest request,
                                                 String status) {
        if (connection == null
                || request == null
                || request.getId() <= 0
                || request.getOrderId() <= 0
                || request.getUserId() <= 0) {
            return;
        }

        String normalizedStatus = normalizeStatus(status);

        if (!"APPROVED".equals(normalizedStatus) && !"REJECTED".equals(normalizedStatus)) {
            return;
        }

        boolean approved = "APPROVED".equals(normalizedStatus);

        String type = approved
                ? "CANCEL_REQUEST_APPROVED"
                : "CANCEL_REQUEST_REJECTED";

        String title = approved
                ? "Yêu cầu hủy đơn đã được duyệt"
                : "Yêu cầu hủy đơn bị từ chối";

        String message = approved
                ? "Yêu cầu hủy đơn hàng #" + request.getOrderId() + " của bạn đã được duyệt."
                : "Yêu cầu hủy đơn hàng #" + request.getOrderId() + " của bạn đã bị từ chối.";

        try {
            new NotificationDAO().createUserNotification(
                    connection,
                    request.getUserId(),
                    type,
                    title,
                    message,
                    "/orders/detail?id=" + request.getOrderId(),
                    "CANCEL_REQUEST",
                    request.getId()
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm rollback thao tác duyệt/từ chối hủy đơn.
             */
            e.printStackTrace();
        }
    }

    private String buildReasonText(String reason) {
        String safeReason = trimToNull(reason);

        if (safeReason == null) {
            return ".";
        }

        return ". Lý do: " + safeReason;
    }


    private CancelRequest mapRow(ResultSet resultSet) throws SQLException {
        CancelRequest request = new CancelRequest();
        request.setId(resultSet.getLong("id"));
        request.setOrderId(resultSet.getLong("order_id"));
        request.setUserId(resultSet.getInt("user_id"));
        request.setReason(resultSet.getString("reason"));
        request.setStatus(resultSet.getString("status"));
        request.setAdminNote(resultSet.getString("admin_note"));
        request.setRefundAmount(vnd0(resultSet.getBigDecimal("refund_amount")));
        request.setRefundMethod(resultSet.getString("refund_method"));

        Timestamp requestedAt = resultSet.getTimestamp("requested_at");
        if (requestedAt != null) {
            request.setRequestedAt(requestedAt.toLocalDateTime());
        }

        Timestamp processedAt = resultSet.getTimestamp("processed_at");
        if (processedAt != null) {
            request.setProcessedAt(processedAt.toLocalDateTime());
        }

        int processedBy = resultSet.getInt("processed_by");
        if (!resultSet.wasNull()) {
            request.setProcessedBy(processedBy);
        }

        request.setUsername(resultSet.getString("username"));
        request.setCustomerName(resultSet.getString("customer_name"));
        request.setOrderTotal(vnd0(resultSet.getBigDecimal("order_total")));
        request.setPaymentMethod(resultSet.getString("payment_method"));
        request.setPaymentStatus(resultSet.getString("payment_status"));
        request.setOrderStatus(resultSet.getString("order_status"));
        request.setShippingStatus(resultSet.getString("shipping_status"));
        return request;
    }

    private BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String normalizeStatus(String status) {
        String normalized = trimToDefault(status, "REQUESTED").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "REQUESTED", "APPROVED", "REJECTED" -> normalized;
            default -> "REQUESTED";
        };
    }

    private String normalizeRefundMethod(String refundMethod) {
        String normalized = trimToNull(refundMethod);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "VNPAY", "BANK_TRANSFER", "CASH", "STORE_CREDIT", "MANUAL" -> normalized;
            default -> "MANUAL";
        };
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}
