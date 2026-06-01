package com.webshop.app.dao;

import com.webshop.app.model.Order;
import com.webshop.app.model.ShippingStatus;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    private static final String PAID = "PAID";
    private static final String COD = "COD";

    private static final String PAYMENT_PENDING = "PENDING";
    private static final String PAYMENT_FAILED = "FAILED";
    private static final String PAYMENT_CANCELED = "CANCELED";

    private static final String ORDER_PROCESSING = "processing";
    private static final String ORDER_CONFIRMED = "confirmed";
    private static final String ORDER_SHIPPING = "shipping";
    private static final String ORDER_COMPLETED = "completed";
    private static final String ORDER_CANCELLED = "cancelled";

    private static final String DEFAULT_SHIPPING_METHOD = "ECONOMY";
    private static final String DEFAULT_SHIPPING_PROVIDER = "INTERNAL";

    private static final String SELECT_COLUMNS = """
            id,
            user_id,
            full_name,
            phone,
            address,
            total,
            coupon_discount,
            payment_method,
            payment_status,
            status,
            vnp_txn_ref,
            coupon_id,
            stock_deducted,
            payment_attempt_count,
            last_payment_error,
            cancel_reason,
            cancelled_at,
            refund_status,
            refund_amount,
            refund_method,
            shipping_method,
            shipping_provider,
            shipping_fee,
            shipping_code,
            shipping_status,
            shipped_at,
            delivered_at,
            customer_received_confirmed,
            customer_received_at,
            auto_completed_at,
            receive_confirm_note,
            created_at
            """;

    private static BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private static Timestamp toTimestamp(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return Timestamp.valueOf(value);
    }

    private static String normalizeOrderStatus(String status) {
        String normalized = defaultIfBlank(status, ORDER_PROCESSING)
                .trim()
                .toLowerCase();

        if ("canceled".equals(normalized)) {
            return ORDER_CANCELLED;
        }

        return switch (normalized) {
            case "processing", "confirmed", "shipping", "completed", "cancelled" -> normalized;
            default -> ORDER_PROCESSING;
        };
    }

    private static String normalizePaymentStatus(String paymentStatus) {
        String normalized = defaultIfBlank(paymentStatus, PAYMENT_PENDING)
                .trim()
                .toUpperCase();

        if ("CANCELLED".equals(normalized)) {
            return PAYMENT_CANCELED;
        }

        return switch (normalized) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "REFUNDED" -> normalized;
            default -> PAYMENT_PENDING;
        };
    }

    private static String normalizePaymentMethod(String paymentMethod) {
        String normalized = defaultIfBlank(paymentMethod, COD)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "COD", "VNPAY" -> normalized;
            default -> COD;
        };
    }

    private static String normalizeShippingMethod(String shippingMethod) {
        String normalized = defaultIfBlank(shippingMethod, DEFAULT_SHIPPING_METHOD)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "ECONOMY", "FAST", "EXPRESS" -> normalized;
            default -> DEFAULT_SHIPPING_METHOD;
        };
    }

    private static String normalizeShippingProvider(String shippingProvider) {
        String normalized = defaultIfBlank(shippingProvider, DEFAULT_SHIPPING_PROVIDER)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "INTERNAL", "GHTK", "GHN", "VIETTEL_POST", "OTHER" -> normalized;
            default -> DEFAULT_SHIPPING_PROVIDER;
        };
    }

    private static String normalizeShippingStatus(String shippingStatus) {
        return ShippingStatus.normalizeCode(shippingStatus);
    }

    private static String normalizeRefundStatus(String refundStatus) {
        String normalized = defaultIfBlank(refundStatus, "NONE")
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "NONE", "PENDING", "REQUESTED", "APPROVED", "REJECTED", "RETURNED", "REFUNDED" -> normalized;
            default -> "NONE";
        };
    }

    private static String normalizeRefundMethod(String refundMethod) {
        String normalized = trimToNull(refundMethod);

        if (normalized == null) {
            return null;
        }

        normalized = normalized.toUpperCase();

        return switch (normalized) {
            case "VNPAY", "BANK_TRANSFER", "CASH", "STORE_CREDIT", "MANUAL" -> normalized;
            default -> "MANUAL";
        };
    }

    private static String generateInternalShippingCode(int orderId) {
        if (orderId <= 0) {
            return null;
        }

        return "MC-SHIP-" + String.format("%06d", orderId);
    }

    private static boolean shouldHaveShippingCode(String shippingStatus) {
        String normalized = normalizeShippingStatus(shippingStatus);

        return switch (normalized) {
            case "PENDING_PICKUP", "DELIVERING", "DELIVERED", "FAILED" -> true;
            default -> false;
        };
    }

    private static String resolveShippingCodeForDisplay(
            int orderId,
            String shippingCode,
            String shippingStatus
    ) {
        if (!isBlank(shippingCode)) {
            return shippingCode.trim();
        }

        if (shouldHaveShippingCode(shippingStatus)) {
            return generateInternalShippingCode(orderId);
        }

        return null;
    }

    private static String getOrderStatusByShippingStatus(String shippingStatus, String currentOrderStatus) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);
        String normalizedOrderStatus = normalizeOrderStatus(currentOrderStatus);

        /*
         * Đồng bộ logic:
         * PENDING_PICKUP -> confirmed
         * DELIVERING     -> shipping
         * DELIVERED      -> completed
         * FAILED         -> shipping, để shop có thể giao lại
         * CANCELED       -> cancelled
         */
        return switch (normalizedShippingStatus) {
            case "PENDING_PICKUP" -> ORDER_CONFIRMED;
            case "DELIVERING" -> ORDER_SHIPPING;
            case "DELIVERED" -> ORDER_COMPLETED;
            case "FAILED" -> ORDER_SHIPPING;
            case "CANCELED" -> ORDER_CANCELLED;
            default -> normalizedOrderStatus;
        };
    }

    private static String getPaymentStatusByShippingStatus(
            String shippingStatus,
            String paymentMethod,
            String currentPaymentStatus
    ) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);
        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
        String normalizedPaymentStatus = normalizePaymentStatus(currentPaymentStatus);

        /*
         * COD chỉ được xem là đã thanh toán khi giao thành công.
         * VNPAY đã được xử lý thanh toán ở luồng riêng.
         */
        if ("DELIVERED".equals(normalizedShippingStatus) && COD.equals(normalizedPaymentMethod)) {
            return PAID;
        }

        /*
         * Nếu vận chuyển bị hủy khi thanh toán còn đang chờ,
         * chuyển payment_status sang CANCELED để thống nhất dữ liệu.
         */
        if ("CANCELED".equals(normalizedShippingStatus)
                && PAYMENT_PENDING.equals(normalizedPaymentStatus)) {
            return PAYMENT_CANCELED;
        }

        return normalizedPaymentStatus;
    }

    /* =========================================================
       CREATE
    ========================================================= */

    public int create(Connection conn, Order order) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (order == null) {
            throw new SQLException("Order must not be null");
        }

        String sql = """
                INSERT INTO store_order
                (
                    user_id,
                    full_name,
                    phone,
                    address,
                    total,
                    coupon_discount,
                    payment_method,
                    payment_status,
                    status,
                    vnp_txn_ref,
                    coupon_id,
                    stock_deducted,
                    payment_attempt_count,
                    last_payment_error,
                    cancel_reason,
                    cancelled_at,
                    refund_status,
                    refund_amount,
                    refund_method,
                    shipping_method,
                    shipping_provider,
                    shipping_fee,
                    shipping_code,
                    shipping_status,
                    shipped_at,
                    delivered_at,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            int index = 1;

            String shippingMethod = normalizeShippingMethod(order.getShippingMethod());
            String shippingProvider = normalizeShippingProvider(order.getShippingProvider());
            String shippingStatus = normalizeShippingStatus(order.getShippingStatus());

            statement.setInt(index++, order.getUserId());
            statement.setString(index++, order.getFullName());
            statement.setString(index++, order.getPhone());
            statement.setString(index++, order.getAddress());
            statement.setBigDecimal(index++, vnd0(order.getTotal()));
            statement.setBigDecimal(index++, vnd0(order.getCouponDiscount()));
            statement.setString(index++, normalizePaymentMethod(order.getPaymentMethod()));
            statement.setString(index++, normalizePaymentStatus(order.getPaymentStatus()));
            statement.setString(index++, normalizeOrderStatus(order.getStatus()));
            statement.setString(index++, trimToNull(order.getVnpTxnRef()));

            if (order.getCouponId() == null) {
                statement.setNull(index++, Types.BIGINT);
            } else {
                statement.setInt(index++, order.getCouponId());
            }

            statement.setBoolean(index++, order.isStockDeducted());
            statement.setInt(index++, order.getPaymentAttemptCount());
            statement.setString(index++, trimToNull(order.getLastPaymentError()));

            statement.setString(index++, trimToNull(order.getCancelReason()));
            statement.setTimestamp(index++, toTimestamp(order.getCancelledAt()));
            statement.setString(index++, normalizeRefundStatus(order.getRefundStatus()));
            statement.setBigDecimal(index++, vnd0(order.getRefundAmount()));
            statement.setString(index++, normalizeRefundMethod(order.getRefundMethod()));

            statement.setString(index++, shippingMethod);
            statement.setString(index++, shippingProvider);
            statement.setBigDecimal(index++, vnd0(order.getShippingFee()));
            statement.setString(index++, trimToNull(order.getShippingCode()));
            statement.setString(index++, shippingStatus);
            statement.setTimestamp(index++, toTimestamp(order.getShippedAt()));
            statement.setTimestamp(index++, toTimestamp(order.getDeliveredAt()));
            statement.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (!resultSet.next()) {
                    throw new SQLException("Không lấy được generated order id.");
                }

                int orderId = resultSet.getInt(1);

                /*
                 * Mọi đơn hàng có tracking vận chuyển đều cần mã vận đơn.
                 * Nếu chưa có shipping_code thì tự sinh mã nội bộ MC-SHIP-xxxxxx.
                 */
                if (isBlank(order.getShippingCode()) && shouldHaveShippingCode(shippingStatus)) {
                    String generatedShippingCode = generateInternalShippingCode(orderId);

                    updateShippingCreated(
                            conn,
                            orderId,
                            shippingProvider,
                            generatedShippingCode
                    );

                    order.setShippingCode(generatedShippingCode);
                    order.setShippingProvider(shippingProvider);
                    order.setShippingMethod(shippingMethod);
                    order.setShippingStatus(ShippingStatus.PENDING_PICKUP.getCode());
                }

                return orderId;
            }
        }
    }

    /* =========================================================
       FIND
    ========================================================= */

    public Order findById(int orderId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findById error", e);
        }
    }

    public Order findById(Connection conn, int orderId) throws SQLException {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }
        }
    }

    public Order findByIdAndUserId(int orderId, int userId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE id = ?
                  AND user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findByIdAndUserId error", e);
        }
    }

    public Order findLatestByUser(int userId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE user_id = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findLatestByUser error", e);
        }
    }

    public List<Order> findAll() {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                ORDER BY id DESC
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(mapRow(resultSet));
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findAll error", e);
        }
    }

    public List<Order> findByUser(int userId) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE user_id = ?
                ORDER BY id DESC
                """;

        List<Order> orders = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapRow(resultSet));
                }
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findByUser error", e);
        }
    }

    /* =========================================================
       USER STATS
    ========================================================= */

    public int countByUser(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM store_order
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.countByUser error", e);
        }
    }

    public BigDecimal totalSpentByUserVnd(int userId) {
        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE user_id = ?
                  AND payment_status = ?
                  AND LOWER(status) NOT IN ('cancelled', 'canceled')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return vnd0(resultSet.getBigDecimal(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.totalSpentByUserVnd error", e);
        }
    }

    /* =========================================================
       UPDATE ORDER STATUS
    ========================================================= */

    public void updateStatus(int orderId, String status) {
        String sql = """
                UPDATE store_order
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeOrderStatus(status));
            statement.setInt(2, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateStatus error", e);
        }
    }

    public void updateStatusAndPaymentStatus(int orderId, String status, String paymentStatus) {
        String sql = """
                UPDATE store_order
                SET status = ?,
                    payment_status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeOrderStatus(status));
            statement.setString(2, normalizePaymentStatus(paymentStatus));
            statement.setInt(3, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateStatusAndPaymentStatus error", e);
        }
    }

    public void updatePaymentStatus(int orderId,
                                    String paymentStatus,
                                    String status,
                                    String txnRef) {
        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizePaymentStatus(paymentStatus));
            statement.setString(2, normalizeOrderStatus(status));
            statement.setString(3, trimToNull(txnRef));
            statement.setInt(4, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updatePaymentStatus error", e);
        }
    }

    public void updatePaymentStatus(Connection conn,
                                    int orderId,
                                    String paymentStatus,
                                    String status,
                                    String txnRef) throws SQLException {
        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, normalizePaymentStatus(paymentStatus));
            statement.setString(2, normalizeOrderStatus(status));
            statement.setString(3, trimToNull(txnRef));
            statement.setInt(4, orderId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       UPDATE SHIPPING / TRACKING
    ========================================================= */

    public void updateShippingCreated(int orderId,
                                      String shippingProvider,
                                      String shippingCode) {
        try (Connection connection = DBConnection.getConnection()) {
            updateShippingCreated(connection, orderId, shippingProvider, shippingCode);
        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateShippingCreated error", e);
        }
    }

    public void updateShippingCreated(Connection conn,
                                      int orderId,
                                      String shippingProvider,
                                      String shippingCode) throws SQLException {
        String sql = """
                UPDATE store_order
                SET shipping_provider = COALESCE(NULLIF(shipping_provider, ''), ?),
                    shipping_method = COALESCE(NULLIF(shipping_method, ''), ?),
                    shipping_code = ?,
                    shipping_status = ?,
                    status = CASE
                        WHEN LOWER(COALESCE(status, '')) IN ('', 'processing') THEN ?
                        ELSE status
                    END
                WHERE id = ?
                """;

        String safeShippingCode = isBlank(shippingCode)
                ? generateInternalShippingCode(orderId)
                : shippingCode.trim();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            int index = 1;

            statement.setString(index++, normalizeShippingProvider(shippingProvider));
            statement.setString(index++, DEFAULT_SHIPPING_METHOD);
            statement.setString(index++, safeShippingCode);
            statement.setString(index++, ShippingStatus.PENDING_PICKUP.getCode());
            statement.setString(index++, ORDER_CONFIRMED);
            statement.setInt(index++, orderId);

            int updated = statement.executeUpdate();

            if (updated > 0) {
                insertTrackingLogSafely(
                        conn,
                        orderId,
                        ShippingStatus.PENDING_PICKUP.getCode(),
                        "Đơn hàng đã có mã vận đơn " + safeShippingCode + ", chờ lấy hàng.",
                        null
                );
            }
        }
    }

    public void updateShippingInfo(int orderId,
                                   String shippingProvider,
                                   String shippingCode,
                                   String shippingMethod,
                                   BigDecimal shippingFee) {
        String sql = """
                UPDATE store_order
                SET shipping_provider = ?,
                    shipping_code = ?,
                    shipping_method = ?,
                    shipping_fee = ?
                WHERE id = ?
                """;

        String safeShippingCode = isBlank(shippingCode)
                ? generateInternalShippingCode(orderId)
                : shippingCode.trim();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeShippingProvider(shippingProvider));
            statement.setString(2, safeShippingCode);
            statement.setString(3, normalizeShippingMethod(shippingMethod));
            statement.setBigDecimal(4, vnd0(shippingFee));
            statement.setInt(5, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateShippingInfo error", e);
        }
    }

    public void ensureShippingCode(int orderId) {
        String sql = """
                UPDATE store_order
                SET shipping_code = ?,
                    shipping_provider = COALESCE(NULLIF(shipping_provider, ''), ?),
                    shipping_method = COALESCE(NULLIF(shipping_method, ''), ?)
                WHERE id = ?
                  AND (shipping_code IS NULL OR shipping_code = '')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, generateInternalShippingCode(orderId));
            statement.setString(2, DEFAULT_SHIPPING_PROVIDER);
            statement.setString(3, DEFAULT_SHIPPING_METHOD);
            statement.setInt(4, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.ensureShippingCode error", e);
        }
    }

    public int backfillMissingShippingCodes() {
        String sql = """
                UPDATE store_order
                SET shipping_code = CONCAT('MC-SHIP-', LPAD(id, 6, '0')),
                    shipping_provider = COALESCE(NULLIF(shipping_provider, ''), ?),
                    shipping_method = COALESCE(NULLIF(shipping_method, ''), ?)
                WHERE (shipping_code IS NULL OR shipping_code = '')
                  AND UPPER(COALESCE(shipping_status, '')) IN
                      (
                        'PENDING_PICKUP', 'PENDING', 'CREATED', 'PICKING',
                        'DELIVERING', 'SHIPPING', 'IN_TRANSIT',
                        'DELIVERED', 'SUCCESS', 'COMPLETED',
                        'FAILED', 'DELIVERY_FAILED', 'RETURNED'
                      )
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, DEFAULT_SHIPPING_PROVIDER);
            statement.setString(2, DEFAULT_SHIPPING_METHOD);

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.backfillMissingShippingCodes error", e);
        }
    }

    public void updateShippingStatus(int orderId, String shippingStatus) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                updateShippingStatus(connection, orderId, shippingStatus);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateShippingStatus error", e);
        }
    }

    public void updateShippingStatus(Connection conn,
                                     int orderId,
                                     String shippingStatus) throws SQLException {
        Order order = findById(conn, orderId);

        if (order == null) {
            throw new SQLException("Không tìm thấy đơn hàng #" + orderId);
        }

        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);

        String nextOrderStatus = getOrderStatusByShippingStatus(
                normalizedShippingStatus,
                order.getStatus()
        );

        String nextPaymentStatus = getPaymentStatusByShippingStatus(
                normalizedShippingStatus,
                order.getPaymentMethod(),
                order.getPaymentStatus()
        );

        String safeShippingCode = isBlank(order.getShippingCode())
                ? generateInternalShippingCode(orderId)
                : order.getShippingCode().trim();

        String safeShippingProvider = normalizeShippingProvider(order.getShippingProvider());
        String safeShippingMethod = normalizeShippingMethod(order.getShippingMethod());

        String sql = """
                UPDATE store_order
                SET shipping_status = ?,
                    status = ?,
                    payment_status = ?,
                    shipping_code = CASE
                        WHEN ? = 1 AND (shipping_code IS NULL OR shipping_code = '') THEN ?
                        ELSE shipping_code
                    END,
                    shipping_provider = COALESCE(NULLIF(shipping_provider, ''), ?),
                    shipping_method = COALESCE(NULLIF(shipping_method, ''), ?),
                    shipped_at = CASE
                        WHEN ? IN ('DELIVERING', 'DELIVERED') AND shipped_at IS NULL THEN NOW()
                        ELSE shipped_at
                    END,
                    delivered_at = CASE
                        WHEN ? = 'DELIVERED' AND delivered_at IS NULL THEN NOW()
                        ELSE delivered_at
                    END
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            int index = 1;

            statement.setString(index++, normalizedShippingStatus);
            statement.setString(index++, nextOrderStatus);
            statement.setString(index++, nextPaymentStatus);

            statement.setInt(index++, shouldHaveShippingCode(normalizedShippingStatus) ? 1 : 0);
            statement.setString(index++, safeShippingCode);

            statement.setString(index++, safeShippingProvider);
            statement.setString(index++, safeShippingMethod);

            statement.setString(index++, normalizedShippingStatus);
            statement.setString(index++, normalizedShippingStatus);
            statement.setInt(index++, orderId);

            int updated = statement.executeUpdate();

            if (updated > 0) {
                insertTrackingLogSafely(
                        conn,
                        orderId,
                        normalizedShippingStatus,
                        buildTrackingNote(normalizedShippingStatus, safeShippingCode),
                        null
                );
            }
        }
    }

    public void markPendingPickup(int orderId) {
        updateShippingStatus(orderId, ShippingStatus.PENDING_PICKUP.getCode());
    }

    public void markDelivering(int orderId) {
        updateShippingStatus(orderId, ShippingStatus.DELIVERING.getCode());
    }

    public void markDelivered(int orderId) {
        updateShippingStatus(orderId, ShippingStatus.DELIVERED.getCode());
    }

    public void markDeliveryFailed(int orderId) {
        updateShippingStatus(orderId, ShippingStatus.FAILED.getCode());
    }

    public void cancelShipping(int orderId) {
        updateShippingStatus(orderId, ShippingStatus.CANCELED.getCode());
    }


    /* =========================================================
       TRACKING LOG
    ========================================================= */

    private void insertTrackingLogSafely(Connection connection,
                                         int orderId,
                                         String shippingStatus,
                                         String note,
                                         Integer updatedBy) {
        String sql = """
                INSERT INTO store_order_tracking
                (
                    order_id,
                    shipping_status,
                    note,
                    updated_by,
                    created_at
                )
                VALUES (?, ?, ?, ?, NOW())
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, normalizeShippingStatus(shippingStatus));
            statement.setString(3, defaultIfBlank(note, buildTrackingNote(shippingStatus, null)));

            if (updatedBy == null || updatedBy <= 0) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, updatedBy);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            /*
             * Không để lỗi bảng tracking làm hỏng thao tác chính.
             * Nếu database cũ chưa có store_order_tracking, chức năng đơn hàng vẫn chạy được.
             */
            e.printStackTrace();
        }
    }

    private String buildTrackingNote(String shippingStatus, String shippingCode) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);
        String codeText = isBlank(shippingCode) ? "" : " Mã vận đơn: " + shippingCode + ".";

        return switch (normalizedShippingStatus) {
            case "PENDING_PICKUP" -> "Đơn hàng đang chờ lấy hàng." + codeText;
            case "DELIVERING" -> "Đơn hàng đang được vận chuyển." + codeText;
            case "DELIVERED" -> "Đơn hàng đã được giao thành công." + codeText;
            case "FAILED" -> "Giao hàng thất bại. Shop sẽ liên hệ lại với khách hàng." + codeText;
            case "CANCELED" -> "Vận chuyển đã bị hủy.";
            default -> "Cập nhật trạng thái vận chuyển." + codeText;
        };
    }


    /* =========================================================
       NOTIFICATION HELPERS - ISSUE 114
    ========================================================= */

    private void notifyOrderCancelledByUserSafely(Connection conn,
                                                  Order order,
                                                  int orderId,
                                                  int userId,
                                                  String reason) {
        if (conn == null || orderId <= 0 || userId <= 0) {
            return;
        }

        try {
            NotificationDAO notificationDAO = new NotificationDAO();

            notificationDAO.createUserNotification(
                    conn,
                    userId,
                    "ORDER_CANCELLED",
                    "Đơn hàng đã được hủy",
                    "Đơn hàng #" + orderId + " của bạn đã được hủy thành công.",
                    "/orders/detail?id=" + orderId,
                    "ORDER",
                    (long) orderId
            );

            notificationDAO.createAdminNotification(
                    conn,
                    "ORDER_CANCELLED",
                    "Khách hàng đã hủy đơn",
                    "Khách hàng #" + userId + " đã hủy đơn hàng #" + orderId
                            + buildReasonText(reason),
                    "/admin/orders?action=detail&id=" + orderId,
                    "ORDER",
                    (long) orderId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm rollback thao tác hủy đơn.
             */
            e.printStackTrace();
        }
    }

    private void notifyReturnRequestedSafely(Connection conn,
                                             Order order,
                                             int orderId,
                                             BigDecimal refundAmount,
                                             String refundMethod) {
        if (conn == null || order == null || orderId <= 0 || order.getUserId() <= 0) {
            return;
        }

        try {
            NotificationDAO notificationDAO = new NotificationDAO();

            notificationDAO.createUserNotification(
                    conn,
                    order.getUserId(),
                    "RETURN_REQUEST_CREATED",
                    "Đã gửi yêu cầu hoàn hàng",
                    "Yêu cầu hoàn hàng cho đơn hàng #" + orderId + " đã được ghi nhận.",
                    "/orders/detail?id=" + orderId,
                    "ORDER",
                    (long) orderId
            );

            notificationDAO.createAdminNotification(
                    conn,
                    "RETURN_REQUEST_CREATED",
                    "Có yêu cầu hoàn hàng mới",
                    "Khách hàng #" + order.getUserId() + " đã gửi yêu cầu hoàn hàng cho đơn hàng #"
                            + orderId + ". Số tiền đề xuất: " + vnd0(refundAmount) + " VND.",
                    "/admin/orders?action=detail&id=" + orderId,
                    "ORDER",
                    (long) orderId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm hỏng thao tác hoàn hàng.
             */
            e.printStackTrace();
        }
    }

    private void notifyRefundStatusSafely(Connection conn,
                                          Order order,
                                          int orderId,
                                          String refundStatus,
                                          BigDecimal refundAmount) {
        if (conn == null || order == null || orderId <= 0 || order.getUserId() <= 0) {
            return;
        }

        String normalizedRefundStatus = normalizeRefundStatus(refundStatus);

        String type;
        String title;
        String message;

        switch (normalizedRefundStatus) {
            case "APPROVED", "RETURNED" -> {
                type = "RETURN_REQUEST_APPROVED";
                title = "Yêu cầu hoàn hàng đã được duyệt";
                message = "Yêu cầu hoàn hàng cho đơn hàng #" + orderId + " đã được duyệt.";
            }
            case "REFUNDED" -> {
                type = "RETURN_REQUEST_APPROVED";
                title = "Đơn hàng đã được hoàn tiền";
                message = "Đơn hàng #" + orderId + " đã được hoàn tiền "
                        + vnd0(refundAmount) + " VND.";
            }
            case "REJECTED" -> {
                type = "RETURN_REQUEST_REJECTED";
                title = "Yêu cầu hoàn hàng bị từ chối";
                message = "Yêu cầu hoàn hàng cho đơn hàng #" + orderId + " đã bị từ chối.";
            }
            default -> {
                return;
            }
        }

        try {
            new NotificationDAO().createUserNotification(
                    conn,
                    order.getUserId(),
                    type,
                    title,
                    message,
                    "/orders/detail?id=" + orderId,
                    "ORDER",
                    (long) orderId
            );
        } catch (SQLException e) {
            /*
             * Không để lỗi notification làm hỏng thao tác cập nhật hoàn tiền.
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


    /* =========================================================
       CANCEL / RETURN / REFUND
    ========================================================= */

    public boolean cancelOrderByUser(int orderId, int userId, String reason) {
        if (orderId <= 0 || userId <= 0) {
            return false;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean updated = cancelOrderByUser(connection, orderId, userId, reason);
                connection.commit();
                return updated;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.cancelOrderByUser error", e);
        }
    }

    public boolean cancelOrderByUser(Connection conn, int orderId, int userId, String reason) throws SQLException {
        Order order = findByIdForUpdate(conn, orderId, userId);

        if (order == null || !isCancelable(order)) {
            return false;
        }

        boolean paidVnpay = "VNPAY".equalsIgnoreCase(order.getPaymentMethod())
                && PAID.equalsIgnoreCase(order.getPaymentStatus());

        String sql = """
                UPDATE store_order
                SET status = ?,
                    shipping_status = ?,
                    cancel_reason = ?,
                    cancelled_at = NOW(),
                    refund_status = ?,
                    refund_amount = ?,
                    refund_method = ?,
                    payment_status = CASE
                        WHEN UPPER(COALESCE(payment_status, 'PENDING')) = 'PENDING' THEN ?
                        ELSE payment_status
                    END
                WHERE id = ?
                  AND user_id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, ORDER_CANCELLED);
            statement.setString(index++, ShippingStatus.CANCELED.getCode());
            statement.setString(index++, trimToNull(reason));
            statement.setString(index++, paidVnpay ? "PENDING" : "NONE");
            statement.setBigDecimal(index++, paidVnpay ? vnd0(order.getTotal()) : BigDecimal.ZERO);
            statement.setString(index++, paidVnpay ? "VNPAY" : null);
            statement.setString(index++, PAYMENT_CANCELED);
            statement.setInt(index++, orderId);
            statement.setInt(index++, userId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                insertTrackingLogSafely(
                        conn,
                        orderId,
                        ShippingStatus.CANCELED.getCode(),
                        defaultIfBlank(reason, "Khách hàng đã hủy đơn hàng."),
                        null
                );

                notifyOrderCancelledByUserSafely(
                        conn,
                        order,
                        orderId,
                        userId,
                        reason
                );
            }

            return updated;
        }
    }

    public boolean isOrderCancelable(int orderId, int userId) {
        Order order = findByIdAndUserId(orderId, userId);
        return order != null && isCancelable(order);
    }

    public boolean isOrderReturnable(int orderId, int userId) {
        Order order = findByIdAndUserId(orderId, userId);
        return order != null && order.isReturnable();
    }

    public void markReturnRequested(Connection conn, int orderId, BigDecimal refundAmount, String refundMethod) throws SQLException {
        Order order = findById(conn, orderId);

        String sql = """
                UPDATE store_order
                SET refund_status = ?,
                    refund_amount = ?,
                    refund_method = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, "REQUESTED");
            statement.setBigDecimal(2, vnd0(refundAmount));
            statement.setString(3, normalizeRefundMethod(refundMethod));
            statement.setInt(4, orderId);

            int updated = statement.executeUpdate();

            if (updated > 0) {
                notifyReturnRequestedSafely(
                        conn,
                        order,
                        orderId,
                        refundAmount,
                        refundMethod
                );
            }
        }
    }

    public boolean updateRefundStatus(int orderId,
                                      String refundStatus,
                                      BigDecimal refundAmount,
                                      String refundMethod) {
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

        String normalizedRefundStatus = normalizeRefundStatus(refundStatus);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Order order = findById(connection, orderId);

                boolean updated;

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, normalizedRefundStatus);
                    statement.setBigDecimal(2, vnd0(refundAmount));
                    statement.setString(3, normalizeRefundMethod(refundMethod));
                    statement.setString(4, normalizedRefundStatus);
                    statement.setInt(5, orderId);

                    updated = statement.executeUpdate() > 0;
                }

                if (updated) {
                    notifyRefundStatusSafely(
                            connection,
                            order,
                            orderId,
                            normalizedRefundStatus,
                            refundAmount
                    );
                }

                connection.commit();
                return updated;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateRefundStatus error", e);
        }
    }

    private Order findByIdForUpdate(Connection conn, int orderId, int userId) throws SQLException {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE id = ?
                  AND user_id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }
        }
    }

    private boolean isCancelable(Order order) {
        if (order == null) {
            return false;
        }

        String orderStatus = normalizeOrderStatus(order.getStatus());
        String shippingStatus = normalizeShippingStatus(order.getShippingStatus());

        boolean orderOpen = ORDER_PROCESSING.equals(orderStatus)
                || ORDER_CONFIRMED.equals(orderStatus);

        boolean shippingNotStarted = ShippingStatus.PENDING_PICKUP.getCode().equals(shippingStatus);

        return orderOpen && shippingNotStarted;
    }


    public boolean confirmReceivedByUser(int orderId, int userId, String note) {
        if (orderId <= 0 || userId <= 0) {
            return false;
        }

        String sql = """
                UPDATE store_order
                SET customer_received_confirmed = 1,
                    customer_received_at = NOW(),
                    receive_confirm_note = ?,
                    delivered_at = COALESCE(delivered_at, NOW()),
                    shipping_status = 'DELIVERED',
                    status = 'completed',
                    payment_status = CASE
                        WHEN UPPER(COALESCE(payment_method, 'COD')) = 'COD' THEN 'PAID'
                        ELSE payment_status
                    END
                WHERE id = ?
                  AND user_id = ?
                  AND UPPER(COALESCE(shipping_status, '')) = 'DELIVERED'
                  AND customer_received_confirmed = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trimToNull(note));
            statement.setInt(2, orderId);
            statement.setInt(3, userId);

            boolean updated = statement.executeUpdate() > 0;

            if (updated) {
                insertTrackingLogSafely(
                        connection,
                        orderId,
                        ShippingStatus.DELIVERED.getCode(),
                        defaultIfBlank(note, "Khách hàng đã xác nhận nhận hàng thành công."),
                        null
                );
            }

            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.confirmReceivedByUser error", e);
        }
    }

    public int autoCompleteUnconfirmedDeliveredOrders(int daysAfterDelivered) {
        int safeDays = Math.max(daysAfterDelivered, 1);
        String sql = """
                UPDATE store_order
                SET customer_received_confirmed = 1,
                    customer_received_at = COALESCE(customer_received_at, DATE_ADD(delivered_at, INTERVAL ? DAY)),
                    auto_completed_at = NOW(),
                    receive_confirm_note = COALESCE(receive_confirm_note, 'Hệ thống tự động xác nhận đã nhận hàng sau 7 ngày kể từ khi giao thành công.'),
                    status = 'completed',
                    payment_status = CASE
                        WHEN UPPER(COALESCE(payment_method, 'COD')) = 'COD' THEN 'PAID'
                        ELSE payment_status
                    END
                WHERE UPPER(COALESCE(shipping_status, '')) = 'DELIVERED'
                  AND delivered_at IS NOT NULL
                  AND customer_received_confirmed = 0
                  AND delivered_at <= DATE_SUB(NOW(), INTERVAL ? DAY)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, safeDays);
            statement.setInt(2, safeDays);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.autoCompleteUnconfirmedDeliveredOrders error", e);
        }
    }

    /* =========================================================
       VNPAY
    ========================================================= */

    public void setVnpTxnRef(int orderId, String txnRef) {
        String sql = """
                UPDATE store_order
                SET vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, trimToNull(txnRef));
            statement.setInt(2, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.setVnpTxnRef error", e);
        }
    }

    public Order findByTxnRef(String txnRef) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txnRef);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findByTxnRef error", e);
        }
    }

    public Integer findIdByTxnRef(String txnRef) {
        String sql = """
                SELECT id
                FROM store_order
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txnRef);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findIdByTxnRef error", e);
        }
    }

    public BigDecimal getTotalByTxnRef(String txnRef) {
        String sql = """
                SELECT total
                FROM store_order
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txnRef);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return vnd0(resultSet.getBigDecimal(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.getTotalByTxnRef error", e);
        }
    }

    public void updatePaymentByTxnRef(String txnRef,
                                      String paymentStatus,
                                      String status) {
        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizePaymentStatus(paymentStatus));
            statement.setString(2, normalizeOrderStatus(status));
            statement.setString(3, txnRef);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updatePaymentByTxnRef error", e);
        }
    }

    public void markPaidByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, PAID, ORDER_CONFIRMED);
    }

    public void markFailedByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, PAYMENT_PENDING, ORDER_PROCESSING);
    }

    public boolean isPaidByTxnRef(String txnRef) {
        String sql = """
                SELECT payment_status
                FROM store_order
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txnRef);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                String status = resultSet.getString(1);
                return status != null && PAID.equalsIgnoreCase(status);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.isPaidByTxnRef error", e);
        }
    }

    public void prepareVnpayPaymentAttempt(int orderId, String txnRef) {
        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?,
                    payment_attempt_count = COALESCE(payment_attempt_count, 0) + 1,
                    last_payment_error = NULL
                WHERE id = ?
                  AND UPPER(payment_method) = 'VNPAY'
                  AND UPPER(COALESCE(payment_status, 'PENDING')) <> 'PAID'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAYMENT_PENDING);
            statement.setString(2, ORDER_PROCESSING);
            statement.setString(3, trimToNull(txnRef));
            statement.setInt(4, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.prepareVnpayPaymentAttempt error", e);
        }
    }

    public void markVnpayAwaitingRetry(int orderId, String txnRef, String errorMessage) {
        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?,
                    last_payment_error = ?
                WHERE id = ?
                  AND UPPER(payment_method) = 'VNPAY'
                  AND UPPER(COALESCE(payment_status, 'PENDING')) <> 'PAID'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAYMENT_PENDING);
            statement.setString(2, ORDER_PROCESSING);
            statement.setString(3, trimToNull(txnRef));
            statement.setString(4, trimToNull(errorMessage));
            statement.setInt(5, orderId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.markVnpayAwaitingRetry error", e);
        }
    }

    public void markStockDeducted(Connection conn, int orderId) throws SQLException {
        String sql = """
                UPDATE store_order
                SET stock_deducted = 1,
                    last_payment_error = NULL
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    /* =========================================================
       CHART
    ========================================================= */

    public List<String> userChartLabels(int userId) {
        List<String> labels = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            labels.add("T" + ym.getMonthValue());
        }

        return labels;
    }

    public List<BigDecimal> userChartValues(int userId) {
        List<BigDecimal> values = new ArrayList<>();

        String sql = """
                SELECT
                    YEAR(created_at) AS y,
                    MONTH(created_at) AS m,
                    COALESCE(SUM(total), 0) AS sum_total
                FROM store_order
                WHERE user_id = ?
                  AND payment_status = ?
                  AND LOWER(status) NOT IN ('cancelled', 'canceled')
                  AND created_at >= ?
                  AND created_at < ?
                GROUP BY YEAR(created_at), MONTH(created_at)
                ORDER BY y, m
                """;

        YearMonth current = YearMonth.now();
        YearMonth startYm = current.minusMonths(5);
        LocalDate startDate = startYm.atDay(1);
        LocalDate endDateExclusive = current.plusMonths(1).atDay(1);

        Map<String, BigDecimal> map = new HashMap<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, PAID);
            statement.setDate(3, Date.valueOf(startDate));
            statement.setDate(4, Date.valueOf(endDateExclusive));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int year = resultSet.getInt("y");
                    int month = resultSet.getInt("m");
                    BigDecimal total = vnd0(resultSet.getBigDecimal("sum_total"));

                    map.put(year + "-" + month, total);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.userChartValues error", e);
        }

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            values.add(map.getOrDefault(key, BigDecimal.ZERO));
        }

        return values;
    }

    /* =========================================================
       MAPPER
    ========================================================= */

    private Order mapRow(ResultSet resultSet) throws SQLException {
        Order order = new Order();

        int orderId = resultSet.getInt("id");
        String normalizedShippingStatus = normalizeShippingStatus(resultSet.getString("shipping_status"));

        String displayShippingCode = resolveShippingCodeForDisplay(
                orderId,
                resultSet.getString("shipping_code"),
                normalizedShippingStatus
        );

        order.setId(orderId);
        order.setUserId(resultSet.getInt("user_id"));
        order.setFullName(resultSet.getString("full_name"));
        order.setPhone(resultSet.getString("phone"));
        order.setAddress(resultSet.getString("address"));
        order.setTotal(vnd0(resultSet.getBigDecimal("total")));
        order.setCouponDiscount(vnd0(resultSet.getBigDecimal("coupon_discount")));
        order.setPaymentMethod(normalizePaymentMethod(resultSet.getString("payment_method")));
        order.setPaymentStatus(normalizePaymentStatus(resultSet.getString("payment_status")));
        order.setStatus(normalizeOrderStatus(resultSet.getString("status")));
        order.setVnpTxnRef(resultSet.getString("vnp_txn_ref"));

        Object couponIdObj = resultSet.getObject("coupon_id");
        if (couponIdObj instanceof Number) {
            order.setCouponId(((Number) couponIdObj).intValue());
        }

        order.setStockDeducted(resultSet.getBoolean("stock_deducted"));
        order.setPaymentAttemptCount(resultSet.getInt("payment_attempt_count"));
        order.setLastPaymentError(resultSet.getString("last_payment_error"));

        order.setCancelReason(resultSet.getString("cancel_reason"));
        Timestamp cancelledAt = resultSet.getTimestamp("cancelled_at");
        if (cancelledAt != null) {
            order.setCancelledAt(cancelledAt.toLocalDateTime());
        }
        order.setRefundStatus(normalizeRefundStatus(resultSet.getString("refund_status")));
        order.setRefundAmount(vnd0(resultSet.getBigDecimal("refund_amount")));
        order.setRefundMethod(normalizeRefundMethod(resultSet.getString("refund_method")));

        order.setShippingMethod(normalizeShippingMethod(resultSet.getString("shipping_method")));
        order.setShippingProvider(normalizeShippingProvider(resultSet.getString("shipping_provider")));
        order.setShippingFee(vnd0(resultSet.getBigDecimal("shipping_fee")));
        order.setShippingCode(displayShippingCode);
        order.setShippingStatus(normalizedShippingStatus);

        Timestamp shippedAt = resultSet.getTimestamp("shipped_at");
        if (shippedAt != null) {
            order.setShippedAt(shippedAt.toLocalDateTime());
        }

        Timestamp deliveredAt = resultSet.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            order.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        order.setCustomerReceivedConfirmed(resultSet.getBoolean("customer_received_confirmed"));

        Timestamp customerReceivedAt = resultSet.getTimestamp("customer_received_at");
        if (customerReceivedAt != null) {
            order.setCustomerReceivedAt(customerReceivedAt.toLocalDateTime());
        }

        Timestamp autoCompletedAt = resultSet.getTimestamp("auto_completed_at");
        if (autoCompletedAt != null) {
            order.setAutoCompletedAt(autoCompletedAt.toLocalDateTime());
        }

        order.setReceiveConfirmNote(resultSet.getString("receive_confirm_note"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }

        return order;
    }

    public void updateOrderStatus(Connection conn, long orderId, String newStatus) throws SQLException {
        String sql = "UPDATE store_order SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        }
    }
}
