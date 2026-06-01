package com.webshop.app.dao;

import com.webshop.app.model.ReturnRequest;
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

public class ReturnRequestDAO {

    private static final String SELECT_COLUMNS = """
            rr.id,
            rr.order_id,
            rr.user_id,
            rr.reason,
            rr.status,
            rr.admin_note,
            rr.refund_amount,
            rr.refund_method,
            rr.requested_at,
            rr.processed_at,
            rr.processed_by,
            u.username,
            COALESCE(u.full_name, o.full_name) AS customer_name,
            o.total AS order_total,
            o.payment_method,
            o.payment_status,
            o.status AS order_status
            """;

    public long create(Connection conn, ReturnRequest request) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (request == null) {
            throw new SQLException("ReturnRequest must not be null");
        }

        String sql = """
                INSERT INTO store_return_request
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
            statement.setString(index++, trimToDefault(request.getReason(), "Khách hàng yêu cầu hoàn hàng."));
            statement.setString(index++, normalizeStatus(request.getStatus()));
            statement.setString(index++, trimToNull(request.getAdminNote()));
            statement.setBigDecimal(index++, vnd0(request.getRefundAmount()));
            statement.setString(index++, normalizeRefundMethod(request.getRefundMethod()));

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    long createdId = resultSet.getLong(1);

                    notifyReturnRequestCreatedSafely(
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

    public ReturnRequest findById(long id) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_return_request rr
                JOIN store_order o ON o.id = rr.order_id
                JOIN users u ON u.id = rr.user_id
                WHERE rr.id = ?
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
            throw new RuntimeException("ReturnRequestDAO.findById error", e);
        }
    }

    public ReturnRequest findByOrderId(long orderId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_return_request rr
                JOIN store_order o ON o.id = rr.order_id
                JOIN users u ON u.id = rr.user_id
                WHERE rr.order_id = ?
                ORDER BY rr.id DESC
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
            throw new RuntimeException("ReturnRequestDAO.findByOrderId error", e);
        }
    }

    public List<ReturnRequest> findByUserId(int userId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_return_request rr
                JOIN store_order o ON o.id = rr.order_id
                JOIN users u ON u.id = rr.user_id
                WHERE rr.user_id = ?
                ORDER BY rr.requested_at DESC, rr.id DESC
                """;

        List<ReturnRequest> requests = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRow(resultSet));
                }
            }

            return requests;

        } catch (SQLException e) {
            throw new RuntimeException("ReturnRequestDAO.findByUserId error", e);
        }
    }

    public List<ReturnRequest> findAll() {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_return_request rr
                JOIN store_order o ON o.id = rr.order_id
                JOIN users u ON u.id = rr.user_id
                ORDER BY rr.requested_at DESC, rr.id DESC
                """;

        List<ReturnRequest> requests = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                requests.add(mapRow(resultSet));
            }

            return requests;

        } catch (SQLException e) {
            throw new RuntimeException("ReturnRequestDAO.findAll error", e);
        }
    }

    public boolean existsActiveByOrderId(Connection conn, long orderId) throws SQLException {
        String sql = """
                SELECT 1
                FROM store_return_request
                WHERE order_id = ?
                  AND UPPER(status) IN ('REQUESTED', 'APPROVED', 'RETURNED', 'REFUNDED')
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
            throw new RuntimeException("ReturnRequestDAO.existsActiveByOrderId error", e);
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
                ReturnRequest current = findByIdForUpdate(connection, id);

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

                updateReturnRequestStatus(
                        connection,
                        id,
                        normalizedStatus,
                        adminNote,
                        finalRefundAmount,
                        finalRefundMethod,
                        processedBy
                );

                updateOrderRefundStatus(
                        connection,
                        current.getOrderId(),
                        mapOrderRefundStatus(normalizedStatus),
                        finalRefundAmount,
                        finalRefundMethod
                );

                notifyReturnRequestResultSafely(
                        connection,
                        current,
                        normalizedStatus,
                        finalRefundAmount
                );

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ReturnRequestDAO.updateStatus error", e);
        }
    }

    private ReturnRequest findByIdForUpdate(Connection connection, long id) throws SQLException {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_return_request rr
                JOIN store_order o ON o.id = rr.order_id
                JOIN users u ON u.id = rr.user_id
                WHERE rr.id = ?
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

    private void updateReturnRequestStatus(Connection connection,
                                           long id,
                                           String status,
                                           String adminNote,
                                           BigDecimal refundAmount,
                                           String refundMethod,
                                           Integer processedBy) throws SQLException {
        String sql = """
                UPDATE store_return_request
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

    private void updateOrderRefundStatus(Connection connection,
                                         long orderId,
                                         String refundStatus,
                                         BigDecimal refundAmount,
                                         String refundMethod) throws SQLException {
        String sql = """
                UPDATE store_order
                SET refund_status = ?,
                    refund_amount = ?,
                    refund_method = ?,
                    payment_status = CASE
                        WHEN ? = 'REFUNDED' THEN 'REFUNDED'
                        ELSE payment_status
                    END
                WHERE id = ?
                """;

        String normalizedRefundStatus = normalizeOrderRefundStatus(refundStatus);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedRefundStatus);
            statement.setBigDecimal(2, vnd0(refundAmount));
            statement.setString(3, normalizeRefundMethod(refundMethod));
            statement.setString(4, normalizedRefundStatus);
            statement.setLong(5, orderId);
            statement.executeUpdate();
        }
    }

    private String mapOrderRefundStatus(String requestStatus) {
        return switch (normalizeStatus(requestStatus)) {
            case "APPROVED" -> "APPROVED";
            case "REJECTED" -> "REJECTED";
            case "RETURNED" -> "RETURNED";
            case "REFUNDED" -> "REFUNDED";
            default -> "REQUESTED";
        };
    }

    /* =========================================================
       NOTIFICATION HELPERS - ISSUE 114
    ========================================================= */

    private void notifyReturnRequestCreatedSafely(Connection connection,
                                                  ReturnRequest request,
                                                  long returnRequestId) {
        if (connection == null
                || request == null
                || returnRequestId <= 0
                || request.getOrderId() <= 0
                || request.getUserId() <= 0) {
            return;
        }

        try {
            NotificationDAO notificationDAO = new NotificationDAO();

            notificationDAO.createUserNotification(
                    connection,
                    request.getUserId(),
                    "RETURN_REQUEST_CREATED",
                    "Đã gửi yêu cầu hoàn hàng",
                    "Yêu cầu hoàn hàng cho đơn hàng #" + request.getOrderId() + " của bạn đã được ghi nhận.",
                    "/orders/detail?id=" + request.getOrderId(),
                    "RETURN_REQUEST",
                    returnRequestId
            );

            notificationDAO.createAdminNotification(
                    connection,
                    "RETURN_REQUEST_CREATED",
                    "Có yêu cầu hoàn hàng mới",
                    "Khách hàng #" + request.getUserId()
                            + " đã gửi yêu cầu hoàn hàng cho đơn hàng #" + request.getOrderId()
                            + buildReasonText(request.getReason()),
                    "/admin/orders?action=detail&id=" + request.getOrderId(),
                    "RETURN_REQUEST",
                    returnRequestId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm hỏng thao tác tạo yêu cầu hoàn hàng.
             */
            e.printStackTrace();
        }
    }

    private void notifyReturnRequestResultSafely(Connection connection,
                                                 ReturnRequest request,
                                                 String status,
                                                 BigDecimal refundAmount) {
        if (connection == null
                || request == null
                || request.getId() <= 0
                || request.getOrderId() <= 0
                || request.getUserId() <= 0) {
            return;
        }

        String normalizedStatus = normalizeStatus(status);

        String type;
        String title;
        String message;

        switch (normalizedStatus) {
            case "APPROVED" -> {
                type = "RETURN_REQUEST_APPROVED";
                title = "Yêu cầu hoàn hàng đã được duyệt";
                message = "Yêu cầu hoàn hàng cho đơn hàng #" + request.getOrderId() + " của bạn đã được duyệt.";
            }
            case "RETURNED" -> {
                type = "RETURN_REQUEST_APPROVED";
                title = "Shop đã nhận hàng hoàn";
                message = "Shop đã ghi nhận hàng hoàn của đơn hàng #" + request.getOrderId() + ".";
            }
            case "REFUNDED" -> {
                type = "RETURN_REQUEST_APPROVED";
                title = "Đơn hàng đã được hoàn tiền";
                message = "Đơn hàng #" + request.getOrderId() + " đã được hoàn tiền "
                        + vnd0(refundAmount) + " VND.";
            }
            case "REJECTED" -> {
                type = "RETURN_REQUEST_REJECTED";
                title = "Yêu cầu hoàn hàng bị từ chối";
                message = "Yêu cầu hoàn hàng cho đơn hàng #" + request.getOrderId() + " của bạn đã bị từ chối.";
            }
            default -> {
                return;
            }
        }

        try {
            new NotificationDAO().createUserNotification(
                    connection,
                    request.getUserId(),
                    type,
                    title,
                    message,
                    "/orders/detail?id=" + request.getOrderId(),
                    "RETURN_REQUEST",
                    request.getId()
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm rollback thao tác xử lý hoàn hàng.
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


    private ReturnRequest mapRow(ResultSet resultSet) throws SQLException {
        ReturnRequest request = new ReturnRequest();

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
            case "REQUESTED", "APPROVED", "REJECTED", "RETURNED", "REFUNDED" -> normalized;
            default -> "REQUESTED";
        };
    }

    private String normalizeOrderRefundStatus(String status) {
        String normalized = trimToDefault(status, "REQUESTED").toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "NONE", "PENDING", "REQUESTED", "APPROVED", "REJECTED", "RETURNED", "REFUNDED" -> normalized;
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
