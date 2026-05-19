package com.webshop.app.dao;

import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminStatsDAO {

    private static final String PAID = "PAID";

    private static BigDecimal vnd0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= BASIC ================= */

    public int countOrders() {

        String sql = """
                SELECT COUNT(*)
                FROM store_order
                WHERE payment_status = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countOrders error", e);
        }
    }

    public BigDecimal totalRevenueVnd() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return vnd0(resultSet.getBigDecimal(1));
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.totalRevenueVnd error", e);
        }
    }

    public BigDecimal averageOrderValueVnd() {

        BigDecimal revenue = totalRevenueVnd();
        int orders = countOrders();

        if (orders <= 0) {
            return BigDecimal.ZERO;
        }

        return revenue
                .divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= LAST 30 DAYS ================= */

    public BigDecimal last30DaysRevenue() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return vnd0(resultSet.getBigDecimal(1));
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.last30DaysRevenue error", e);
        }
    }

    public BigDecimal prev30DaysRevenue() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                AND created_at >= DATE_SUB(NOW(), INTERVAL 60 DAY)
                AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return vnd0(resultSet.getBigDecimal(1));
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.prev30DaysRevenue error", e);
        }
    }

    public BigDecimal rollingDiffVnd() {
        return vnd0(last30DaysRevenue().subtract(prev30DaysRevenue()));
    }

    public int rollingGrowthPercent() {

        BigDecimal previous = prev30DaysRevenue();

        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal difference = last30DaysRevenue().subtract(previous);

        return difference.multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /* ================= THIS MONTH ================= */

    public BigDecimal thisMonthRevenue() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                AND YEAR(created_at) = YEAR(CURDATE())
                AND MONTH(created_at) = MONTH(CURDATE())
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return vnd0(resultSet.getBigDecimal(1));
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.thisMonthRevenue error", e);
        }
    }

    public BigDecimal prevMonthRevenueCalendar() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                AND YEAR(created_at) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
                AND MONTH(created_at) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return vnd0(resultSet.getBigDecimal(1));
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.prevMonthRevenueCalendar error", e);
        }
    }

    /* ================= CHART ================= */

    public List<String> chartLabels() {

        String sql = """
                SELECT DATE_FORMAT(created_at, '%m/%Y') AS label
                FROM store_order
                WHERE payment_status = ?
                GROUP BY DATE_FORMAT(created_at, '%m/%Y')
                ORDER BY MIN(created_at)
                """;

        List<String> labels = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                labels.add(resultSet.getString("label"));
            }

            return labels;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.chartLabels error", e);
        }
    }

    public List<BigDecimal> chartValues() {

        String sql = """
                SELECT COALESCE(SUM(total), 0) AS revenue
                FROM store_order
                WHERE payment_status = ?
                GROUP BY DATE_FORMAT(created_at, '%m/%Y')
                ORDER BY MIN(created_at)
                """;

        List<BigDecimal> values = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                values.add(vnd0(resultSet.getBigDecimal("revenue")));
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.chartValues error", e);
        }
    }

    /* ================= TOP PRODUCTS ================= */

    public List<Object[]> topSellingProducts() {

        String sql = """
                SELECT
                    p.title AS name,
                    COALESCE(SUM(oi.quantity), 0) AS sold,
                    COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
                FROM store_orderitem oi
                JOIN store_product p ON p.id = oi.product_id
                JOIN store_order o ON o.id = oi.order_id
                WHERE o.payment_status = ?
                GROUP BY p.title
                ORDER BY sold DESC
                LIMIT 5
                """;

        List<Object[]> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                products.add(new Object[]{
                        resultSet.getString("name"),
                        resultSet.getInt("sold"),
                        vnd0(resultSet.getBigDecimal("revenue"))
                });
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.topSellingProducts error", e);
        }
    }
}