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
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminOrderDAO {

    private static final String PAID = "PAID";
    private static final String COD = "COD";

    private static final String ORDER_PROCESSING = "processing";
    private static final String ORDER_CONFIRMED = "confirmed";
    private static final String ORDER_SHIPPING = "shipping";
    private static final String ORDER_COMPLETED = "completed";
    private static final String ORDER_CANCELLED = "cancelled";

    private static final String PAYMENT_PENDING = "PENDING";
    private static final String PAYMENT_CANCELED = "CANCELED";

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


    public static class OrderSearchFilter {
        private String keyword;
        private String orderStatus;
        private String paymentStatus;
        private String shippingStatus;
        private String shippingProvider;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private int page = 1;
        private int pageSize = 20;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public String getShippingStatus() {
            return shippingStatus;
        }

        public void setShippingStatus(String shippingStatus) {
            this.shippingStatus = shippingStatus;
        }

        public String getShippingProvider() {
            return shippingProvider;
        }

        public void setShippingProvider(String shippingProvider) {
            this.shippingProvider = shippingProvider;
        }

        public LocalDate getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(LocalDate dateFrom) {
            this.dateFrom = dateFrom;
        }

        public LocalDate getDateTo() {
            return dateTo;
        }

        public void setDateTo(LocalDate dateTo) {
            this.dateTo = dateTo;
        }

        public int getPage() {
            return Math.max(page, 1);
        }

        public void setPage(int page) {
            this.page = Math.max(page, 1);
        }

        public int getPageSize() {
            if (pageSize <= 0) {
                return 20;
            }

            return Math.min(pageSize, 100);
        }

        public void setPageSize(int pageSize) {
            if (pageSize <= 0) {
                this.pageSize = 20;
            } else {
                this.pageSize = Math.min(pageSize, 100);
            }
        }

        public int getOffset() {
            return (getPage() - 1) * getPageSize();
        }
    }

    /* =========================================================
       FIND
    ========================================================= */


    public List<Order> search(OrderSearchFilter filter) {
        OrderSearchFilter safeFilter = filter == null ? new OrderSearchFilter() : filter;
        List<Order> orders = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT
                """).append(SELECT_COLUMNS).append("""
                FROM store_order
                WHERE 1 = 1
                """);

        appendSearchWhere(sql, params, safeFilter);
        sql.append("""
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """);

        params.add(safeFilter.getPageSize());
        params.add(safeFilter.getOffset());

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bindParams(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapRow(resultSet));
                }
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.search error", e);
        }
    }

    public int count(OrderSearchFilter filter) {
        OrderSearchFilter safeFilter = filter == null ? new OrderSearchFilter() : filter;
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM store_order
                WHERE 1 = 1
                """);

        appendSearchWhere(sql, params, safeFilter);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bindParams(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.count error", e);
        }
    }

    private void appendSearchWhere(StringBuilder sql,
                                   List<Object> params,
                                   OrderSearchFilter filter) {
        String keyword = trimToNull(filter.getKeyword());

        if (keyword != null) {
            String like = "%" + keyword + "%";
            sql.append("""
                    AND (
                        CAST(id AS CHAR) LIKE ?
                        OR full_name LIKE ?
                        OR phone LIKE ?
                        OR address LIKE ?
                        OR shipping_code LIKE ?
                        OR vnp_txn_ref LIKE ?
                    )
                    """);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String orderStatus = trimToNull(filter.getOrderStatus());
        if (orderStatus != null) {
            sql.append(" AND LOWER(COALESCE(status, '')) = ?\n");
            params.add(normalizeOrderStatus(orderStatus));
        }

        String paymentStatus = trimToNull(filter.getPaymentStatus());
        if (paymentStatus != null) {
            sql.append(" AND UPPER(COALESCE(payment_status, '')) = ?\n");
            params.add(normalizePaymentStatus(paymentStatus));
        }

        String shippingStatus = trimToNull(filter.getShippingStatus());
        if (shippingStatus != null) {
            sql.append(" AND UPPER(COALESCE(shipping_status, '')) = ?\n");
            params.add(normalizeShippingStatus(shippingStatus));
        }

        String shippingProvider = trimToNull(filter.getShippingProvider());
        if (shippingProvider != null) {
            sql.append(" AND UPPER(COALESCE(shipping_provider, '')) = ?\n");
            params.add(normalizeShippingProvider(shippingProvider));
        }

        if (filter.getDateFrom() != null) {
            sql.append(" AND created_at >= ?\n");
            params.add(java.sql.Date.valueOf(filter.getDateFrom()));
        }

        if (filter.getDateTo() != null) {
            sql.append(" AND created_at < DATE_ADD(?, INTERVAL 1 DAY)\n");
            params.add(java.sql.Date.valueOf(filter.getDateTo()));
        }
    }

    private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;

            if (value instanceof Integer number) {
                statement.setInt(index, number);
            } else if (value instanceof java.sql.Date date) {
                statement.setDate(index, date);
            } else {
                statement.setString(index, String.valueOf(value));
            }
        }
    }

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
        String normalizedStatus = normalizeOrderStatus(status);
        String sql = """
                UPDATE store_order
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedStatus);
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
        String normalizedShippingStatus = ShippingStatus.PENDING_PICKUP.getCode();
        String safeShippingCode = safeShippingCode(id, shippingCode);

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

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                statement.setString(index++, normalizeShippingProvider(shippingProvider));
                statement.setString(index++, DEFAULT_SHIPPING_METHOD);
                statement.setString(index++, safeShippingCode);
                statement.setString(index++, normalizedShippingStatus);
                statement.setString(index++, ORDER_CONFIRMED);
                statement.setInt(index++, id);

                boolean updated = statement.executeUpdate() > 0;

                if (updated) {
                    insertTrackingLog(
                            connection,
                            id,
                            normalizedShippingStatus,
                            "Đơn hàng đã có mã vận đơn " + safeShippingCode + ", chờ lấy hàng.",
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
        String safeShippingCode = safeShippingCode(id, shippingCode);

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
            statement.setString(2, safeShippingCode);
            statement.setString(3, normalizeShippingMethod(shippingMethod));
            statement.setBigDecimal(4, vnd0(shippingFee));
            statement.setInt(5, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.updateShippingInfo error", e);
        }
    }

    public boolean ensureShippingCode(int id) {
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

            statement.setString(1, generateInternalShippingCode(id));
            statement.setString(2, DEFAULT_SHIPPING_PROVIDER);
            statement.setString(3, DEFAULT_SHIPPING_METHOD);
            statement.setInt(4, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminOrderDAO.ensureShippingCode error", e);
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
            throw new RuntimeException("AdminOrderDAO.backfillMissingShippingCodes error", e);
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
        String safeShippingCode = safeShippingCode(id, currentOrder.getShippingCode());
        String safeShippingProvider = normalizeShippingProvider(currentOrder.getShippingProvider());
        String safeShippingMethod = normalizeShippingMethod(currentOrder.getShippingMethod());

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

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
                statement.setInt(index++, id);

                boolean updated = statement.executeUpdate() > 0;

                if (updated) {
                    insertTrackingLog(
                            connection,
                            id,
                            normalizedShippingStatus,
                            defaultIfBlank(note, buildTrackingNote(normalizedShippingStatus, safeShippingCode)),
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
       ADMIN ORDER WORKFLOW HELPERS
    ========================================================= */

    public boolean confirmOrder(int id, Integer adminId, String note) {
        return updateShippingStatus(
                id,
                ShippingStatus.PENDING_PICKUP.getCode(),
                adminId,
                defaultIfBlank(note, "Admin đã xác nhận đơn hàng. Đơn hàng đang chờ lấy hàng.")
        );
    }

    public boolean startShipping(int id, Integer adminId, String note) {
        return updateShippingStatus(
                id,
                ShippingStatus.DELIVERING.getCode(),
                adminId,
                defaultIfBlank(note, "Đơn hàng đã được bàn giao cho đơn vị vận chuyển.")
        );
    }

    public boolean markDelivered(int id, Integer adminId, String note) {
        return updateShippingStatus(
                id,
                ShippingStatus.DELIVERED.getCode(),
                adminId,
                defaultIfBlank(note, "Đơn hàng đã giao thành công cho khách.")
        );
    }

    public boolean markDeliveryFailed(int id, Integer adminId, String note) {
        return updateShippingStatus(
                id,
                ShippingStatus.FAILED.getCode(),
                adminId,
                defaultIfBlank(note, "Giao hàng thất bại. Shop sẽ liên hệ lại với khách hàng.")
        );
    }

    public boolean cancelOrderShipping(int id, Integer adminId, String note) {
        return updateShippingStatus(
                id,
                ShippingStatus.CANCELED.getCode(),
                adminId,
                defaultIfBlank(note, "Đơn hàng đã bị hủy.")
        );
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
                    tracking.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));

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
            statement.setString(3, defaultIfBlank(note, buildTrackingNote(shippingStatus, null)));

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

        int orderId = resultSet.getInt("id");
        String normalizedShippingStatus = normalizeShippingStatus(resultSet.getString("shipping_status"));
        String displayShippingCode = resolveShippingCodeForDisplay(
                orderId,
                resultSet.getString("shipping_code"),
                normalizedShippingStatus
        );

        order.setId(orderId);
        setNullableUserId(order, resultSet);

        order.setFullName(resultSet.getString("full_name"));
        order.setPhone(resultSet.getString("phone"));
        order.setAddress(resultSet.getString("address"));

        order.setTotal(vnd0(resultSet.getBigDecimal("total")));
        order.setCouponDiscount(vnd0(resultSet.getBigDecimal("coupon_discount")));

        order.setPaymentMethod(normalizePaymentMethod(resultSet.getString("payment_method")));
        order.setPaymentStatus(normalizePaymentStatus(resultSet.getString("payment_status")));
        order.setStatus(normalizeOrderStatus(resultSet.getString("status")));
        order.setVnpTxnRef(resultSet.getString("vnp_txn_ref"));

        order.setShippingMethod(normalizeShippingMethod(resultSet.getString("shipping_method")));
        order.setShippingProvider(normalizeShippingProvider(resultSet.getString("shipping_provider")));
        order.setShippingFee(vnd0(resultSet.getBigDecimal("shipping_fee")));
        order.setShippingCode(displayShippingCode);
        order.setShippingStatus(normalizedShippingStatus);

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

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String generateInternalShippingCode(int orderId) {
        if (orderId <= 0) {
            return null;
        }

        return "MC-SHIP-" + String.format("%06d", orderId);
    }

    private String safeShippingCode(int orderId, String shippingCode) {
        if (!isBlank(shippingCode)) {
            return shippingCode.trim();
        }

        return generateInternalShippingCode(orderId);
    }

    private boolean shouldHaveShippingCode(String shippingStatus) {
        String normalized = normalizeShippingStatus(shippingStatus);

        return switch (normalized) {
            case "PENDING_PICKUP", "DELIVERING", "DELIVERED", "FAILED" -> true;
            default -> false;
        };
    }

    private String resolveShippingCodeForDisplay(int orderId,
                                                 String shippingCode,
                                                 String shippingStatus) {
        if (!isBlank(shippingCode)) {
            return shippingCode.trim();
        }

        if (shouldHaveShippingCode(shippingStatus)) {
            return generateInternalShippingCode(orderId);
        }

        return null;
    }

    private String normalizeOrderStatus(String status) {
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

    private String normalizePaymentStatus(String paymentStatus) {
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
            case "PENDING_PICKUP" -> ORDER_CONFIRMED;
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

        if ("DELIVERED".equals(normalizedShippingStatus) && COD.equals(normalizedPaymentMethod)) {
            return PAID;
        }

        if ("CANCELED".equals(normalizedShippingStatus)
                && PAYMENT_PENDING.equals(normalizedPaymentStatus)) {
            return PAYMENT_CANCELED;
        }

        return normalizedPaymentStatus;
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
       SIMPLE VIEW MODEL FOR TRACKING LOG
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

        public Date getCreatedAtDate() {
            if (createdAt == null) {
                return null;
            }

            return Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
        }
    }
}
