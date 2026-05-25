package com.webshop.app.dao;

import com.webshop.app.model.Order;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    private static final String PAID = "PAID";

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

    private static BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private static Timestamp toTimestamp(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return Timestamp.valueOf(value);
    }

    private static String generateInternalShippingCode(int orderId) {
        if (orderId <= 0) {
            return null;
        }

        return "MC-SHIP-" + String.format("%06d", orderId);
    }

    /* ================= CREATE ================= */

    public int create(Connection conn, Order order) throws SQLException {

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
                    shipping_method,
                    shipping_provider,
                    shipping_fee,
                    shipping_code,
                    shipping_status,
                    shipped_at,
                    delivered_at,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = conn.prepareStatement(
                sql,
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {

            int index = 1;

            String shippingMethod = defaultIfBlank(order.getShippingMethod(), "ECONOMY");
            String shippingProvider = defaultIfBlank(order.getShippingProvider(), "INTERNAL");
            String shippingStatus = defaultIfBlank(order.getShippingStatus(), "PENDING");

            statement.setInt(index++, order.getUserId());
            statement.setString(index++, order.getFullName());
            statement.setString(index++, order.getPhone());
            statement.setString(index++, order.getAddress());

            statement.setBigDecimal(
                    index++,
                    vnd0(order.getTotal())
            );

            statement.setBigDecimal(
                    index++,
                    order.getCouponDiscount() != null
                            ? vnd0(order.getCouponDiscount())
                            : BigDecimal.ZERO
            );

            statement.setString(index++, order.getPaymentMethod());
            statement.setString(index++, order.getPaymentStatus());
            statement.setString(index++, order.getStatus());
            statement.setString(index++, order.getVnpTxnRef());

            statement.setString(index++, shippingMethod);
            statement.setString(index++, shippingProvider);

            statement.setBigDecimal(
                    index++,
                    order.getShippingFee() != null
                            ? vnd0(order.getShippingFee())
                            : BigDecimal.ZERO
            );

            statement.setString(index++, order.getShippingCode());
            statement.setString(index++, shippingStatus);

            statement.setTimestamp(index++, toTimestamp(order.getShippedAt()));
            statement.setTimestamp(index++, toTimestamp(order.getDeliveredAt()));

            statement.setTimestamp(
                    index++,
                    new Timestamp(System.currentTimeMillis())
            );

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    int orderId = resultSet.getInt(1);

                    /*
                     * Nếu đơn hàng chưa có mã vận chuyển thì tự sinh mã nội bộ.
                     * Việc update dùng cùng Connection để vẫn nằm trong transaction checkout.
                     */
                    if (order.getShippingCode() == null || order.getShippingCode().isBlank()) {
                        String generatedShippingCode = generateInternalShippingCode(orderId);

                        updateShippingCreated(
                                conn,
                                orderId,
                                shippingProvider,
                                generatedShippingCode
                        );

                        order.setShippingCode(generatedShippingCode);
                        order.setShippingProvider(shippingProvider);
                        order.setShippingStatus("CREATED");
                    }

                    return orderId;
                }

                throw new SQLException(
                        "Không lấy được generated order id."
                );
            }
        }
    }

    /* ================= FIND ================= */

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

    public Order findById(Connection conn, int orderId)
            throws SQLException {

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
            throw new RuntimeException(
                    "OrderDAO.findLatestByUser error",
                    e
            );
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

    /* ================= USER STATS ================= */

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
            throw new RuntimeException(
                    "OrderDAO.totalSpentByUserVnd error",
                    e
            );
        }
    }

    /* ================= UPDATE ORDER STATUS ================= */

    public void updateStatus(int orderId, String status) {

        String sql = """
                UPDATE store_order
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
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

            statement.setString(1, status);
            statement.setString(2, paymentStatus);
            statement.setInt(3, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateStatusAndPaymentStatus error", e);
        }
    }

    public void updatePaymentStatus(
            int orderId,
            String paymentStatus,
            String status,
            String txnRef
    ) {

        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, paymentStatus);
            statement.setString(2, status);
            statement.setString(3, txnRef);
            statement.setInt(4, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "OrderDAO.updatePaymentStatus error",
                    e
            );
        }
    }

    public void updatePaymentStatus(
            Connection conn,
            int orderId,
            String paymentStatus,
            String status,
            String txnRef
    ) throws SQLException {

        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?,
                    vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, paymentStatus);
            statement.setString(2, status);
            statement.setString(3, txnRef);
            statement.setInt(4, orderId);

            statement.executeUpdate();
        }
    }

    /* ================= UPDATE SHIPPING ================= */

    public void updateShippingCreated(int orderId,
                                      String shippingProvider,
                                      String shippingCode) {

        String sql = """
                UPDATE store_order
                SET shipping_provider = ?,
                    shipping_code = ?,
                    shipping_status = 'CREATED'
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, defaultIfBlank(shippingProvider, "INTERNAL"));
            statement.setString(2, shippingCode);
            statement.setInt(3, orderId);

            statement.executeUpdate();

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
                SET shipping_provider = ?,
                    shipping_code = ?,
                    shipping_status = 'CREATED'
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, defaultIfBlank(shippingProvider, "INTERNAL"));
            statement.setString(2, shippingCode);
            statement.setInt(3, orderId);

            statement.executeUpdate();
        }
    }

    public void updateShippingStatus(int orderId, String shippingStatus) {

        String normalizedStatus = defaultIfBlank(shippingStatus, "PENDING").toUpperCase();

        String sql;

        if ("DELIVERING".equals(normalizedStatus)) {
            sql = """
                    UPDATE store_order
                    SET shipping_status = ?,
                        shipped_at = COALESCE(shipped_at, NOW())
                    WHERE id = ?
                    """;
        } else if ("DELIVERED".equals(normalizedStatus)) {
            sql = """
                    UPDATE store_order
                    SET shipping_status = ?,
                        delivered_at = COALESCE(delivered_at, NOW())
                    WHERE id = ?
                    """;
        } else {
            sql = """
                    UPDATE store_order
                    SET shipping_status = ?
                    WHERE id = ?
                    """;
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedStatus);
            statement.setInt(2, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateShippingStatus error", e);
        }
    }

    /* ================= VNPAY ================= */

    public void setVnpTxnRef(int orderId, String txnRef) {

        String sql = """
                UPDATE store_order
                SET vnp_txn_ref = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txnRef);
            statement.setInt(2, orderId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "OrderDAO.setVnpTxnRef error",
                    e
            );
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
            throw new RuntimeException(
                    "OrderDAO.findByTxnRef error",
                    e
            );
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
            throw new RuntimeException(
                    "OrderDAO.findIdByTxnRef error",
                    e
            );
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
            throw new RuntimeException(
                    "OrderDAO.getTotalByTxnRef error",
                    e
            );
        }
    }

    public void updatePaymentByTxnRef(
            String txnRef,
            String paymentStatus,
            String status
    ) {

        String sql = """
                UPDATE store_order
                SET payment_status = ?,
                    status = ?
                WHERE vnp_txn_ref = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, paymentStatus);
            statement.setString(2, status);
            statement.setString(3, txnRef);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "OrderDAO.updatePaymentByTxnRef error",
                    e
            );
        }
    }

    public void markPaidByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, PAID, "confirmed");
    }

    public void markFailedByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, "FAILED", "cancelled");
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

                return status != null
                        && PAID.equalsIgnoreCase(status);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "OrderDAO.isPaidByTxnRef error",
                    e
            );
        }
    }

    /* ================= CHART ================= */

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

        LocalDate endDateExclusive =
                current.plusMonths(1).atDay(1);

        Map<String, BigDecimal> map = new HashMap<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, PAID);

            statement.setDate(
                    3,
                    Date.valueOf(startDate)
            );

            statement.setDate(
                    4,
                    Date.valueOf(endDateExclusive)
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int year = resultSet.getInt("y");
                    int month = resultSet.getInt("m");

                    BigDecimal total =
                            vnd0(resultSet.getBigDecimal("sum_total"));

                    map.put(year + "-" + month, total);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "OrderDAO.userChartValues error",
                    e
            );
        }

        for (int i = 5; i >= 0; i--) {

            YearMonth ym = current.minusMonths(i);

            String key =
                    ym.getYear() + "-" + ym.getMonthValue();

            values.add(
                    map.getOrDefault(key, BigDecimal.ZERO)
            );
        }

        return values;
    }

    /* ================= MAPPER ================= */

    private Order mapRow(ResultSet resultSet)
            throws SQLException {

        Order order = new Order();

        order.setId(resultSet.getInt("id"));
        order.setUserId(resultSet.getInt("user_id"));

        order.setFullName(
                resultSet.getString("full_name")
        );

        order.setPhone(
                resultSet.getString("phone")
        );

        order.setAddress(
                resultSet.getString("address")
        );

        order.setTotal(
                vnd0(resultSet.getBigDecimal("total"))
        );

        order.setCouponDiscount(
                vnd0(resultSet.getBigDecimal("coupon_discount"))
        );

        order.setPaymentMethod(
                resultSet.getString("payment_method")
        );

        order.setPaymentStatus(
                resultSet.getString("payment_status")
        );

        order.setStatus(
                resultSet.getString("status")
        );

        order.setVnpTxnRef(
                resultSet.getString("vnp_txn_ref")
        );

        order.setShippingMethod(
                resultSet.getString("shipping_method")
        );

        order.setShippingProvider(
                resultSet.getString("shipping_provider")
        );

        order.setShippingFee(
                vnd0(resultSet.getBigDecimal("shipping_fee"))
        );

        order.setShippingCode(
                resultSet.getString("shipping_code")
        );

        order.setShippingStatus(
                resultSet.getString("shipping_status")
        );

        Timestamp shippedAt =
                resultSet.getTimestamp("shipped_at");

        if (shippedAt != null) {
            order.setShippedAt(
                    shippedAt.toLocalDateTime()
            );
        }

        Timestamp deliveredAt =
                resultSet.getTimestamp("delivered_at");

        if (deliveredAt != null) {
            order.setDeliveredAt(
                    deliveredAt.toLocalDateTime()
            );
        }

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            order.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        return order;
    }
}
