package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    private static final String PAID = "PAID";

    // Chuẩn hoá tiền VND: không lẻ, làm tròn HALF_UP
    private static BigDecimal vnd0(BigDecimal x) {
        if (x == null) return BigDecimal.ZERO;
        return x.setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= CREATE ================= */
    public int create(Connection conn, Order o) throws SQLException {

        // ✅ FIX: thêm created_at để tránh NULL (DB đang NOT NULL)
        String sql = "INSERT INTO store_order "
                + "(user_id, full_name, phone, address, total, "
                + " payment_method, payment_status, status, vnp_txn_ref, created_at) "
                + "OUTPUT INSERTED.id "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, o.getUserId());
            ps.setString(2, o.getFullName());
            ps.setString(3, o.getPhone());
            ps.setString(4, o.getAddress());
            ps.setBigDecimal(5, vnd0(o.getTotal())); // ✅ tiền VND sạch
            ps.setString(6, o.getPaymentMethod());
            ps.setString(7, o.getPaymentStatus());
            ps.setString(8, o.getStatus());
            ps.setString(9, o.getVnpTxnRef());

            // ✅ created_at = now
            ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /* ================= FIND BY ID ================= */
    public Order findById(int orderId) {
        String sql = "SELECT id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findById error", e);
        }
    }

    /**
     * ✅ Overload dùng chung transaction (khuyến nghị dùng trong finalize VNPAY)
     */
    public Order findById(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }
        }
    }

    /* ================= USER STATS ================= */

    public int countByUser(int userId) {

        // Nếu bạn muốn thống kê chỉ đơn đã thanh toán, đổi SQL thành:
        // String sql = "SELECT COUNT(*) FROM store_order WHERE user_id = ? AND payment_status = 'PAID'";
        String sql = "SELECT COUNT(*) FROM store_order WHERE user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.countByUser error", e);
        }
    }

    public BigDecimal totalSpentByUserVnd(int userId) {

        String sql = "SELECT COALESCE(SUM(total), 0) FROM store_order "
                + "WHERE user_id = ? AND payment_status = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, PAID);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return vnd0(rs.getBigDecimal(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.totalSpentByUserVnd error", e);
        }
    }

    public Order findLatestByUser(int userId) {

        String sql = "SELECT TOP 1 id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order WHERE user_id = ? ORDER BY id DESC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findLatestByUser error", e);
        }
    }

    public void updatePaymentStatus(int orderId, String paymentStatus, String status, String vnpTxnRef) {

        String sql = "UPDATE store_order SET payment_status = ?, status = ?, vnp_txn_ref = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setString(2, status);
            ps.setString(3, vnpTxnRef);
            ps.setInt(4, orderId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updatePaymentStatus error", e);
        }
    }

    /**
     * ✅ Overload dùng chung transaction (khuyến nghị dùng trong finalize VNPAY)
     */
    public void updatePaymentStatus(Connection conn, int orderId, String paymentStatus, String status, String vnpTxnRef)
            throws SQLException {

        String sql = "UPDATE store_order SET payment_status = ?, status = ?, vnp_txn_ref = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, status);
            ps.setString(3, vnpTxnRef);
            ps.setInt(4, orderId);
            ps.executeUpdate();
        }
    }

    public List<Order> findAll() {

        String sql = "SELECT id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order ORDER BY id DESC";

        List<Order> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findAll error", e);
        }
    }

    public List<Order> findByUser(int userId) {

        String sql = "SELECT id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order WHERE user_id = ? ORDER BY id DESC";

        List<Order> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findByUser error", e);
        }
    }

    // ================== HELPER: MAP RESULTSET -> ORDER ==================
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setFullName(rs.getString("full_name"));
        o.setPhone(rs.getString("phone"));
        o.setAddress(rs.getString("address"));

        // ✅ tiền VND sạch
        o.setTotal(vnd0(rs.getBigDecimal("total")));

        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPaymentStatus(rs.getString("payment_status"));
        o.setStatus(rs.getString("status"));
        o.setVnpTxnRef(rs.getString("vnp_txn_ref"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            o.setCreatedAt(ts.toLocalDateTime());
        }
        return o;
    }

    /* ================= CHART HELPERS ================= */

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

        String sql = "SELECT YEAR(created_at) AS y, MONTH(created_at) AS m, COALESCE(SUM(total),0) AS sum_total "
                + "FROM store_order "
                + "WHERE user_id = ? "
                + "  AND payment_status = ? "
                + "  AND created_at >= ? "
                + "  AND created_at < ? "
                + "GROUP BY YEAR(created_at), MONTH(created_at) "
                + "ORDER BY y, m";

        YearMonth current = YearMonth.now();
        YearMonth startYm = current.minusMonths(5);

        LocalDate startDate = startYm.atDay(1);
        LocalDate endDateExclusive = current.plusMonths(1).atDay(1);

        Map<String, BigDecimal> map = new HashMap<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, PAID);
            ps.setDate(3, java.sql.Date.valueOf(startDate));
            ps.setDate(4, java.sql.Date.valueOf(endDateExclusive));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int y = rs.getInt("y");
                    int m = rs.getInt("m");
                    BigDecimal sum = vnd0(rs.getBigDecimal("sum_total"));
                    map.put(y + "-" + m, sum);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.userChartValues error", e);
        }

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            values.add(map.containsKey(key) ? map.get(key) : BigDecimal.ZERO);
        }

        return values;
    }

    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE store_order SET status = ? WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updateStatus error", e);
        }
    }

    /* =========================================================
       VNPAY SUPPORT (NEW)
       ========================================================= */

    /** Set txnRef cho order sau khi tạo orderId (dùng cho /vnpay/payment) */
    public void setVnpTxnRef(int orderId, String txnRef) {
        String sql = "UPDATE store_order SET vnp_txn_ref = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txnRef);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.setVnpTxnRef error", e);
        }
    }

    /** Tìm order theo txnRef */
    public Order findByTxnRef(String txnRef) {
        String sql = "SELECT id, user_id, full_name, phone, address, total, "
                + "payment_method, payment_status, status, vnp_txn_ref, created_at "
                + "FROM store_order WHERE vnp_txn_ref = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findByTxnRef error", e);
        }
    }

    /** Lấy orderId theo txnRef (Return/IPN dùng) */
    public Integer findIdByTxnRef(String txnRef) {
        String sql = "SELECT id FROM store_order WHERE vnp_txn_ref = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.findIdByTxnRef error", e);
        }
    }

    /** Lấy total theo txnRef để verify vnp_Amount (VNPAY gửi amount * 100) */
    public BigDecimal getTotalByTxnRef(String txnRef) {
        String sql = "SELECT total FROM store_order WHERE vnp_txn_ref = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return vnd0(rs.getBigDecimal(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.getTotalByTxnRef error", e);
        }
    }

    /** Cập nhật payment_status/status theo txnRef */
    public void updatePaymentByTxnRef(String txnRef, String paymentStatus, String status) {
        String sql = "UPDATE store_order SET payment_status = ?, status = ? WHERE vnp_txn_ref = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setString(2, status);
            ps.setString(3, txnRef);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.updatePaymentByTxnRef error", e);
        }
    }

    /** Đánh dấu thanh toán thành công */
    public void markPaidByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, PAID, "CONFIRMED");
    }

    /** Đánh dấu thanh toán thất bại / bị hủy */
    public void markFailedByTxnRef(String txnRef) {
        updatePaymentByTxnRef(txnRef, "FAILED", "CANCELLED");
    }

    /** Chống xử lý lặp (IPN có thể gọi nhiều lần) */
    public boolean isPaidByTxnRef(String txnRef) {
        String sql = "SELECT payment_status FROM store_order WHERE vnp_txn_ref = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String st = rs.getString(1);
                return st != null && PAID.equalsIgnoreCase(st);
            }

        } catch (SQLException e) {
            throw new RuntimeException("OrderDAO.isPaidByTxnRef error", e);
        }
    }
}
