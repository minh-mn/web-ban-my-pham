package com.webshop.app.dao;

import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countOrders error", e);
        }
    }

    public int countAllOrders() {

        String sql = """
                SELECT COUNT(*)
                FROM store_order
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countAllOrders error", e);
        }
    }

    public int countUsers() {

        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE role = 'USER'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countUsers error", e);
        }
    }

    public int countProducts() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product
                WHERE is_active = 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countProducts error", e);
        }
    }

    public int countPendingOrders() {

        String sql = """
                SELECT COUNT(*)
                FROM store_order
                WHERE status IN ('processing', 'confirmed')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countPendingOrders error", e);
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
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

    /* ================= TODAY ================= */

    public BigDecimal todayRevenueVnd() {

        String sql = """
                SELECT COALESCE(SUM(total), 0)
                FROM store_order
                WHERE payment_status = ?
                AND DATE(created_at) = CURDATE()
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.todayRevenueVnd error", e);
        }
    }

    public int todayOrdersCount() {

        String sql = """
                SELECT COUNT(*)
                FROM store_order
                WHERE DATE(created_at) = CURDATE()
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.todayOrdersCount error", e);
        }
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
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

        BigDecimal current = last30DaysRevenue();
        BigDecimal previous = prev30DaysRevenue();

        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal difference = current.subtract(previous);

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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return vnd0(resultSet.getBigDecimal(1));
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.prevMonthRevenueCalendar error", e);
        }
    }

    public int monthGrowthPercent() {

        BigDecimal current = thisMonthRevenue();
        BigDecimal previous = prevMonthRevenueCalendar();

        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal difference = current.subtract(previous);

        return difference.multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /* ================= MONTHLY REVENUE CHART ================= */

    public List<String> chartLabels() {

        String sql = """
                SELECT DATE_FORMAT(created_at, '%m/%Y') AS label
                FROM store_order
                WHERE payment_status = ?
                GROUP BY YEAR(created_at), MONTH(created_at), DATE_FORMAT(created_at, '%m/%Y')
                ORDER BY YEAR(created_at), MONTH(created_at)
                """;

        List<String> labels = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    labels.add(resultSet.getString("label"));
                }
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
                GROUP BY YEAR(created_at), MONTH(created_at)
                ORDER BY YEAR(created_at), MONTH(created_at)
                """;

        List<BigDecimal> values = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    values.add(vnd0(resultSet.getBigDecimal("revenue")));
                }
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.chartValues error", e);
        }
    }

    public List<String> last12MonthLabels() {

        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth currentMonth = YearMonth.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            labels.add(month.format(formatter));
        }

        return labels;
    }

    public List<BigDecimal> last12MonthRevenueValues() {

        List<BigDecimal> values = new ArrayList<>();

        String sql = """
                SELECT COALESCE(SUM(total), 0) AS revenue
                FROM store_order
                WHERE payment_status = ?
                AND YEAR(created_at) = ?
                AND MONTH(created_at) = ?
                """;

        YearMonth currentMonth = YearMonth.now();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 11; i >= 0; i--) {
                YearMonth month = currentMonth.minusMonths(i);

                statement.setString(1, PAID);
                statement.setInt(2, month.getYear());
                statement.setInt(3, month.getMonthValue());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        values.add(vnd0(resultSet.getBigDecimal("revenue")));
                    } else {
                        values.add(BigDecimal.ZERO);
                    }
                }
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.last12MonthRevenueValues error", e);
        }
    }

    /* ================= LAST 7 DAYS REVENUE CHART ================= */

    public List<String> last7DaysLabels() {

        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(formatter));
        }

        return labels;
    }

    public List<BigDecimal> last7DaysRevenueValues() {

        List<BigDecimal> values = new ArrayList<>();

        String sql = """
                SELECT COALESCE(SUM(total), 0) AS revenue
                FROM store_order
                WHERE payment_status = ?
                AND DATE(created_at) = ?
                """;

        LocalDate today = LocalDate.now();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);

                statement.setString(1, PAID);
                statement.setDate(2, Date.valueOf(date));

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        values.add(vnd0(resultSet.getBigDecimal("revenue")));
                    } else {
                        values.add(BigDecimal.ZERO);
                    }
                }
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.last7DaysRevenueValues error", e);
        }
    }

    /* ================= ORDER STATUS CHART ================= */

    public List<String> orderStatusLabels() {

        String sql = """
                SELECT status
                FROM store_order
                GROUP BY status
                ORDER BY CASE status
                    WHEN 'processing' THEN 1
                    WHEN 'confirmed' THEN 2
                    WHEN 'shipping' THEN 3
                    WHEN 'completed' THEN 4
                    WHEN 'cancelled' THEN 5
                    ELSE 6
                END
                """;

        List<String> labels = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String status = resultSet.getString("status");
                labels.add(toVietnameseStatus(status));
            }

            return labels;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.orderStatusLabels error", e);
        }
    }

    public List<Integer> orderStatusValues() {

        String sql = """
                SELECT status, COUNT(*) AS total
                FROM store_order
                GROUP BY status
                ORDER BY CASE status
                    WHEN 'processing' THEN 1
                    WHEN 'confirmed' THEN 2
                    WHEN 'shipping' THEN 3
                    WHEN 'completed' THEN 4
                    WHEN 'cancelled' THEN 5
                    ELSE 6
                END
                """;

        List<Integer> values = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                values.add(resultSet.getInt("total"));
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.orderStatusValues error", e);
        }
    }

    private String toVietnameseStatus(String status) {

        if (status == null) {
            return "Không xác định";
        }

        return switch (status.toLowerCase()) {
            case "processing" -> "Đang xử lý";
            case "confirmed" -> "Đã xác nhận";
            case "shipping" -> "Đang giao";
            case "completed" -> "Hoàn thành";
            case "cancelled", "canceled" -> "Đã hủy";
            default -> status;
        };
    }

    /* ================= TOP PRODUCTS ================= */

    public List<Object[]> topSellingProducts() {

        String sql = """
                SELECT
                    p.id AS product_id,
                    p.title AS name,
                    COALESCE(SUM(oi.quantity), 0) AS sold,
                    COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
                FROM store_orderitem oi
                JOIN store_product p ON p.id = oi.product_id
                JOIN store_order o ON o.id = oi.order_id
                WHERE o.payment_status = ?
                GROUP BY p.id, p.title
                ORDER BY sold DESC, revenue DESC
                LIMIT 5
                """;

        List<Object[]> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Object[]{
                            resultSet.getInt("product_id"),
                            resultSet.getString("name"),
                            resultSet.getInt("sold"),
                            vnd0(resultSet.getBigDecimal("revenue"))
                    });
                }
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.topSellingProducts error", e);
        }
    }

    /* ================= RECENT ORDERS ================= */

    public List<Object[]> recentOrders() {

        String sql = """
                SELECT
                    o.id,
                    o.full_name,
                    o.total,
                    o.status,
                    o.payment_status,
                    o.created_at
                FROM store_order o
                ORDER BY o.created_at DESC
                LIMIT 5
                """;

        List<Object[]> orders = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                orders.add(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        vnd0(resultSet.getBigDecimal("total")),
                        toVietnameseStatus(resultSet.getString("status")),
                        resultSet.getString("payment_status"),
                        resultSet.getTimestamp("created_at")
                });
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.recentOrders error", e);
        }
    }
}