package com.webshop.app.dao;

import com.webshop.app.model.Order;
import com.webshop.app.model.ShippingStatus;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminOrderDAO {

    private static final String PAID = "PAID";
    private static final String COD = "COD";

    private static final String ORDER_PROCESSING = "processing";
    private static final String ORDER_CONFIRMED = "confirmed";
    private static final String ORDER_SHIPPING = "shipping";
    private static final String ORDER_COMPLETED = "completed";
    private static final String ORDER_CANCELLED = "cancelled";

    private static final String DEFAULT_PAYMENT_STATUS = "PENDING";
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
            shipping_method,
            shipping_provider,
            shipping_fee,
            shipping_code,
            shipping_status,
            shipped_at,
            delivered_at,
            created_at
            """;

    /* =========================================================
       FIND
    ========================================================= */

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                ORDER BY created_at DESC, id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(mapRow(resultSet));
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findAll error", e);
        }
    }

    public Order findById(int id) {
        String sql = """
                SELECT
                """ + SELECT_COLUMNS + """
                FROM store_order
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findById error", e);
        }
    }

    /* =========================================================
       UPDATE ORDER STATUS
    ========================================================= */

    public boolean updateStatus(int id, String status) {
        String sql = """
                UPDATE store_order
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeOrderStatus(status));
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateStatus error", e);
        }
    }

    public boolean updateStatusAndPaymentStatus(int id, String status, String paymentStatus) {
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
            statement.setInt(3, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateStatusAndPaymentStatus error", e);
        }
    }

    /* =========================================================
       UPDATE SHIPPING / TRACKING
    ========================================================= */

    public boolean updateShippingCreated(int id,
                                         String shippingProvider,
                                         String shippingCode) {
        String sql = """
                UPDATE store_order
                SET shipping_provider = ?,
                    shipping_code = ?,
                    shipping_status = ?
                WHERE id = ?
                """;

        String normalizedShippingStatus = ShippingStatus.PENDING_PICKUP.getCode();

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizeShippingProvider(shippingProvider));
                statement.setString(2, trimToNull(shippingCode));
                statement.setString(3, normalizedShippingStatus);
                statement.setInt(4, id);

                boolean updated = statement.executeUpdate() > 0;

                if (updated) {
                    insertTrackingLog(
                            connection,
                            id,
                            normalizedShippingStatus,
                            "Đơn hàng đã có mã vận đơn, chờ lấy hàng.",
                            null
                    );
                }

                connection.commit();
                return updated;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateShippingCreated error", e);
        }
    }

    public boolean updateShippingInfo(int id,
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

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeShippingProvider(shippingProvider));
            statement.setString(2, trimToNull(shippingCode));
            statement.setString(3, normalizeShippingMethod(shippingMethod));
            statement.setBigDecimal(4, vnd0(shippingFee));
            statement.setInt(5, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateShippingInfo error", e);
        }
    }

    public boolean updateShippingStatus(int id, String shippingStatus) {
        return updateShippingStatus(id, shippingStatus, null, null);
    }

    public boolean updateShippingStatus(int id,
                                        String shippingStatus,
                                        Integer adminId,
                                        String note) {
        Order currentOrder = findById(id);

        if (currentOrder == null) {
            return false;
        }

        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);

        String nextOrderStatus = getOrderStatusByShippingStatus(
                normalizedShippingStatus,
                currentOrder.getStatus()
        );

        String nextPaymentStatus = getPaymentStatusByShippingStatus(
                normalizedShippingStatus,
                currentOrder.getPaymentMethod(),
                currentOrder.getPaymentStatus()
        );

        String sql = """
                UPDATE store_order
                SET shipping_status = ?,
                    status = ?,
                    payment_status = ?,
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

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;

                statement.setString(index++, normalizedShippingStatus);
                statement.setString(index++, nextOrderStatus);
                statement.setString(index++, nextPaymentStatus);
                statement.setString(index++, normalizedShippingStatus);
                statement.setString(index++, normalizedShippingStatus);
                statement.setInt(index++, id);

                boolean updated = statement.executeUpdate() > 0;

                if (updated) {
                    insertTrackingLog(
                            connection,
                            id,
                            normalizedShippingStatus,
                            defaultIfBlank(note, buildTrackingNote(normalizedShippingStatus)),
                            adminId
                    );
                }

                connection.commit();
                return updated;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateShippingStatus error", e);
        }
    }

    public boolean markPendingPickup(int id) {
        return updateShippingStatus(id, ShippingStatus.PENDING_PICKUP.getCode());
    }

    public boolean markDelivering(int id) {
        return updateShippingStatus(id, ShippingStatus.DELIVERING.getCode());
    }

    public boolean markDelivered(int id) {
        return updateShippingStatus(id, ShippingStatus.DELIVERED.getCode());
    }

    public boolean markDeliveryFailed(int id) {
        return updateShippingStatus(id, ShippingStatus.FAILED.getCode());
    }

    public boolean cancelShipping(int id) {
        return updateShippingStatus(id, ShippingStatus.CANCELED.getCode());
    }

    /* =========================================================
       TRACKING LOG
    ========================================================= */

    public List<OrderTrackingView> findTrackingByOrderId(int orderId) {
        List<OrderTrackingView> trackingList = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    order_id,
                    shipping_status,
                    note,
                    updated_by,
                    created_at
                FROM store_order_tracking
                WHERE order_id = ?
                ORDER BY created_at ASC, id ASC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderTrackingView tracking = new OrderTrackingView();

                    tracking.setId(resultSet.getLong("id"));
                    tracking.setOrderId(resultSet.getInt("order_id"));
                    tracking.setShippingStatus(normalizeShippingStatus(resultSet.getString("shipping_status")));
                    tracking.setNote(resultSet.getString("note"));

                    int updatedBy = resultSet.getInt("updated_by");
                    tracking.setUpdatedBy(resultSet.wasNull() ? null : updatedBy);

                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    tracking.setCreatedAt(toLocalDateTime(createdAt));

                    trackingList.add(tracking);
                }
            }

            return trackingList;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.findTrackingByOrderId error", e);
        }
    }

    private void insertTrackingLog(Connection connection,
                                   int orderId,
                                   String shippingStatus,
                                   String note,
                                   Integer updatedBy) throws SQLException {
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
            statement.setString(3, defaultIfBlank(note, buildTrackingNote(shippingStatus)));

            if (updatedBy == null || updatedBy <= 0) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, updatedBy);
            }

            statement.executeUpdate();
        }
    }

    /* =========================================================
       MAPPER
    ========================================================= */

    private Order mapRow(ResultSet resultSet) throws SQLException {
        Order order = new Order();

        order.setId(resultSet.getInt("id"));
        setNullableUserId(order, resultSet);

        order.setFullName(resultSet.getString("full_name"));
        order.setPhone(resultSet.getString("phone"));
        order.setAddress(resultSet.getString("address"));

        order.setTotal(vnd0(resultSet.getBigDecimal("total")));
        order.setCouponDiscount(vnd0(resultSet.getBigDecimal("coupon_discount")));

        order.setPaymentMethod(resultSet.getString("payment_method"));
        order.setPaymentStatus(resultSet.getString("payment_status"));
        order.setStatus(resultSet.getString("status"));
        order.setVnpTxnRef(resultSet.getString("vnp_txn_ref"));

        order.setShippingMethod(resultSet.getString("shipping_method"));
        order.setShippingProvider(resultSet.getString("shipping_provider"));
        order.setShippingFee(vnd0(resultSet.getBigDecimal("shipping_fee")));
        order.setShippingCode(resultSet.getString("shipping_code"));
        order.setShippingStatus(resultSet.getString("shipping_status"));

        order.setShippedAt(toLocalDateTime(resultSet.getTimestamp("shipped_at")));
        order.setDeliveredAt(toLocalDateTime(resultSet.getTimestamp("delivered_at")));
        order.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));

        return order;
    }

    private void setNullableUserId(Order order, ResultSet resultSet) throws SQLException {
        int userId = resultSet.getInt("user_id");

        if (resultSet.wasNull()) {
            order.setUserId(0);
        } else {
            order.setUserId(userId);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    /* =========================================================
       NORMALIZE / BUSINESS RULES
    ========================================================= */

    private BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String normalizeOrderStatus(String status) {
        String normalized = defaultIfBlank(status, ORDER_PROCESSING)
                .trim()
                .toLowerCase();

        return switch (normalized) {
            case "processing", "confirmed", "shipping", "completed", "cancelled", "canceled" -> normalized;
            default -> ORDER_PROCESSING;
        };
    }

    private String normalizePaymentStatus(String paymentStatus) {
        String normalized = defaultIfBlank(paymentStatus, DEFAULT_PAYMENT_STATUS)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "CANCELLED", "REFUNDED" ->
                    "CANCELLED".equals(normalized) ? "CANCELED" : normalized;
            default -> DEFAULT_PAYMENT_STATUS;
        };
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = defaultIfBlank(paymentMethod, COD)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "COD", "VNPAY" -> normalized;
            default -> COD;
        };
    }

    private String normalizeShippingMethod(String shippingMethod) {
        String normalized = defaultIfBlank(shippingMethod, DEFAULT_SHIPPING_METHOD)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "ECONOMY", "FAST", "EXPRESS" -> normalized;
            default -> DEFAULT_SHIPPING_METHOD;
        };
    }

    private String normalizeShippingProvider(String shippingProvider) {
        String normalized = defaultIfBlank(shippingProvider, DEFAULT_SHIPPING_PROVIDER)
                .trim()
                .toUpperCase();

        return switch (normalized) {
            case "INTERNAL", "GHTK", "GHN", "VIETTEL_POST", "OTHER" -> normalized;
            default -> DEFAULT_SHIPPING_PROVIDER;
        };
    }

    private String normalizeShippingStatus(String shippingStatus) {
        return ShippingStatus.normalizeCode(shippingStatus);
    }

    private String getOrderStatusByShippingStatus(String shippingStatus, String currentOrderStatus) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);
        String normalizedOrderStatus = normalizeOrderStatus(currentOrderStatus);

        return switch (normalizedShippingStatus) {
            case "PENDING_PICKUP" -> {
                if (ORDER_PROCESSING.equals(normalizedOrderStatus)) {
                    yield ORDER_CONFIRMED;
                }

                yield normalizedOrderStatus;
            }
            case "DELIVERING" -> ORDER_SHIPPING;
            case "DELIVERED" -> ORDER_COMPLETED;
            case "FAILED" -> ORDER_SHIPPING;
            case "CANCELED" -> ORDER_CANCELLED;
            default -> normalizedOrderStatus;
        };
    }

    private String getPaymentStatusByShippingStatus(String shippingStatus,
                                                    String paymentMethod,
                                                    String currentPaymentStatus) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);
        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
        String normalizedPaymentStatus = normalizePaymentStatus(currentPaymentStatus);

        /*
         * COD chỉ chuyển PAID khi giao thành công.
         * VNPAY đã được xử lý thanh toán ở luồng thanh toán riêng.
         */
        if ("DELIVERED".equals(normalizedShippingStatus) && COD.equals(normalizedPaymentMethod)) {
            return PAID;
        }

        return normalizedPaymentStatus;
    }

    private String buildTrackingNote(String shippingStatus) {
        String normalizedShippingStatus = normalizeShippingStatus(shippingStatus);

        return switch (normalizedShippingStatus) {
            case "PENDING_PICKUP" -> "Đơn hàng đang chờ lấy hàng.";
            case "DELIVERING" -> "Đơn hàng đang được vận chuyển.";
            case "DELIVERED" -> "Đơn hàng đã được giao thành công.";
            case "FAILED" -> "Giao hàng thất bại. Shop sẽ liên hệ lại với khách hàng.";
            case "CANCELED" -> "Vận chuyển đã bị hủy.";
            default -> "Cập nhật trạng thái vận chuyển.";
        };
    }

    /* =========================================================
       SIMPLE VIEW MODEL FOR TRACKING LOG
       JSP có thể gọi:
       ${tracking.shippingStatusLabel}
       ${tracking.createdAt}
    ========================================================= */

    public static class OrderTrackingView {
        private long id;
        private int orderId;
        private String shippingStatus;
        private String note;
        private Integer updatedBy;
        private LocalDateTime createdAt;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getShippingStatus() {
            return shippingStatus;
        }

        public void setShippingStatus(String shippingStatus) {
            this.shippingStatus = ShippingStatus.normalizeCode(shippingStatus);
        }

        public String getShippingStatusLabel() {
            return ShippingStatus.labelOf(shippingStatus);
        }

        public String getShippingStatusCssClass() {
            return ShippingStatus.cssClassOf(shippingStatus);
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Integer getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(Integer updatedBy) {
            this.updatedBy = updatedBy;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
