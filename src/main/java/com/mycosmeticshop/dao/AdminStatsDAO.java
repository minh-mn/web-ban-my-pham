package com.mycosmeticshop.dao;

import com.mycosmeticshop.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminStatsDAO {

    private static final String PAID = "PAID";

    // Chuẩn hoá tiền VND: không lẻ, làm tròn HALF_UP
    private static BigDecimal vnd0(BigDecimal x) {
        if (x == null) return BigDecimal.ZERO;
        return x.setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= BASIC ================= */

    // Tổng đơn: nếu muốn "tổng đơn đã thanh toán" thì lọc PAID (khuyến nghị cho dashboard tiền)
    public int countOrders() {
        String sql = "SELECT COUNT(*) FROM dbo.store_order WHERE payment_status = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countOrders error", e);
        }
    }

    public BigDecimal totalRevenueVnd() {
        String sql =
                "SELECT COALESCE(SUM(total), 0) " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return vnd0(rs.getBigDecimal(1));

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.totalRevenueVnd error", e);
        }
    }

    /**
     * AOV: tính theo PAID để đồng nhất với revenue.
     * AOV = SUM(PAID total) / COUNT(PAID orders)
     */
    public BigDecimal averageOrderValueVnd() {
        BigDecimal revenue = totalRevenueVnd();
        int orders = countOrders();

        if (orders <= 0) return BigDecimal.ZERO;

        return revenue
                .divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= ROLLING 30 DAYS (giữ lại nếu bạn cần) ================= */

    // 30 ngày gần nhất
    public BigDecimal last30DaysRevenue() {
        String sql =
                "SELECT COALESCE(SUM(total), 0) " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "AND created_at >= DATEADD(day, -30, SYSDATETIMEOFFSET())";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return vnd0(rs.getBigDecimal(1));

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.last30DaysRevenue error", e);
        }
    }

    // 30 ngày trước đó: [-60, -30)
    public BigDecimal prev30DaysRevenue() {
        String sql =
                "SELECT COALESCE(SUM(total), 0) " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "AND created_at >= DATEADD(day, -60, SYSDATETIMEOFFSET()) " +
                "AND created_at <  DATEADD(day, -30, SYSDATETIMEOFFSET())";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return vnd0(rs.getBigDecimal(1));

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.prev30DaysRevenue error", e);
        }
    }

    public BigDecimal rollingDiffVnd() {
        return vnd0(last30DaysRevenue().subtract(prev30DaysRevenue()));
    }

    public int rollingGrowthPercent() {
        BigDecimal prev = prev30DaysRevenue();
        if (prev.compareTo(BigDecimal.ZERO) <= 0) return 0;

        BigDecimal diff = last30DaysRevenue().subtract(prev);
        return diff.multiply(BigDecimal.valueOf(100))
                   .divide(prev, 0, RoundingMode.HALF_UP)
                   .intValue();
    }

    /* ================= CALENDAR MONTH (THÁNG LỊCH) ================= */

    public BigDecimal thisMonthRevenue() {
        String sql =
                "DECLARE @startThisMonth datetime2 = DATEFROMPARTS(YEAR(SYSDATETIME()), MONTH(SYSDATETIME()), 1); " +
                "DECLARE @startNextMonth datetime2 = DATEADD(MONTH, 1, @startThisMonth); " +
                "SELECT COALESCE(SUM(total), 0) " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "AND created_at >= @startThisMonth " +
                "AND created_at <  @startNextMonth";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return vnd0(rs.getBigDecimal(1));

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.thisMonthRevenue error", e);
        }
    }

    public BigDecimal prevMonthRevenueCalendar() {
        String sql =
                "DECLARE @startThisMonth datetime2 = DATEFROMPARTS(YEAR(SYSDATETIME()), MONTH(SYSDATETIME()), 1); " +
                "DECLARE @startPrevMonth datetime2 = DATEADD(MONTH, -1, @startThisMonth); " +
                "SELECT COALESCE(SUM(total), 0) " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "AND created_at >= @startPrevMonth " +
                "AND created_at <  @startThisMonth";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return vnd0(rs.getBigDecimal(1));

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.prevMonthRevenueCalendar error", e);
        }
    }

    /* ================= CHART (MONTHLY) ================= */

    public List<String> chartLabels() {
        String sql =
                "SELECT FORMAT(CAST(created_at AS datetime2), 'MM/yyyy') AS label " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "GROUP BY FORMAT(CAST(created_at AS datetime2), 'MM/yyyy') " +
                "ORDER BY MIN(CAST(created_at AS datetime2))";

        List<String> labels = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) labels.add(rs.getString("label"));
            return labels;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.chartLabels error", e);
        }
    }

    public List<BigDecimal> chartValues() {
        String sql =
                "SELECT COALESCE(SUM(total), 0) AS revenue " +
                "FROM dbo.store_order " +
                "WHERE payment_status = ? " +
                "GROUP BY FORMAT(CAST(created_at AS datetime2), 'MM/yyyy') " +
                "ORDER BY MIN(CAST(created_at AS datetime2))";

        List<BigDecimal> values = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) values.add(vnd0(rs.getBigDecimal("revenue")));
            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.chartValues error", e);
        }
    }

    /* ================= TOP PRODUCTS ================= */

    public List<Object[]> topSellingProducts() {
        String sql =
                "SELECT TOP 5 " +
                "  p.title AS name, " +
                "  COALESCE(SUM(oi.quantity), 0) AS sold, " +
                "  COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue " +
                "FROM dbo.store_orderitem oi " +
                "JOIN dbo.store_product p ON p.id = oi.product_id " +
                "JOIN dbo.store_order o ON o.id = oi.order_id " +
                "WHERE o.payment_status = ? " +
                "GROUP BY p.title " +
                "ORDER BY sold DESC";

        List<Object[]> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, PAID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("name"),
                        rs.getInt("sold"),
                        vnd0(rs.getBigDecimal("revenue"))
                });
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.topSellingProducts error", e);
        }
    }
}
