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
                AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
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

    /* ================= PRODUCT ANALYTICS ================= */

    public int countUnsoldProductsThisWeek() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product p
                WHERE p.is_active = 1
                AND NOT EXISTS (
                    SELECT 1
                    FROM store_orderitem oi
                    JOIN store_order o ON o.id = oi.order_id
                    WHERE oi.product_id = p.id
                    AND o.payment_status = ?
                    AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                    AND YEARWEEK(o.created_at, 1) = YEARWEEK(CURDATE(), 1)
                )
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
            throw new RuntimeException("AdminStatsDAO.countUnsoldProductsThisWeek error", e);
        }
    }

    public int countUnsoldProductsThisMonth() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product p
                WHERE p.is_active = 1
                AND NOT EXISTS (
                    SELECT 1
                    FROM store_orderitem oi
                    JOIN store_order o ON o.id = oi.order_id
                    WHERE oi.product_id = p.id
                    AND o.payment_status = ?
                    AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                    AND YEAR(o.created_at) = YEAR(CURDATE())
                    AND MONTH(o.created_at) = MONTH(CURDATE())
                )
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
            throw new RuntimeException("AdminStatsDAO.countUnsoldProductsThisMonth error", e);
        }
    }

    public int countUnsoldProductsLast30Days() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product p
                WHERE p.is_active = 1
                AND NOT EXISTS (
                    SELECT 1
                    FROM store_orderitem oi
                    JOIN store_order o ON o.id = oi.order_id
                    WHERE oi.product_id = p.id
                    AND o.payment_status = ?
                    AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                )
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
            throw new RuntimeException("AdminStatsDAO.countUnsoldProductsLast30Days error", e);
        }
    }

    public int countSoldProductsThisMonth() {

        String sql = """
                SELECT COUNT(DISTINCT p.id)
                FROM store_product p
                JOIN store_orderitem oi ON oi.product_id = p.id
                JOIN store_order o ON o.id = oi.order_id
                WHERE p.is_active = 1
                AND o.payment_status = ?
                AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                AND YEAR(o.created_at) = YEAR(CURDATE())
                AND MONTH(o.created_at) = MONTH(CURDATE())
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
            throw new RuntimeException("AdminStatsDAO.countSoldProductsThisMonth error", e);
        }
    }

    public int countOutOfStockProducts() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product
                WHERE is_active = 1
                AND stock = 0
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countOutOfStockProducts error", e);
        }
    }

    public int countLowStockProducts() {

        String sql = """
                SELECT COUNT(*)
                FROM store_product
                WHERE is_active = 1
                AND stock > 0
                AND stock <= 10
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.countLowStockProducts error", e);
        }
    }

    public List<String> productPerformanceLabels() {

        List<String> labels = new ArrayList<>();
        labels.add("Có bán trong tháng");
        labels.add("Không bán trong tháng");

        return labels;
    }

    public List<Integer> productPerformanceValues() {

        List<Integer> values = new ArrayList<>();
        values.add(countSoldProductsThisMonth());
        values.add(countUnsoldProductsThisMonth());

        return values;
    }

    public List<String> stockStatusLabels() {

        List<String> labels = new ArrayList<>();
        labels.add("Hết hàng");
        labels.add("Sắp hết hàng");
        labels.add("Còn hàng");

        return labels;
    }

    public List<Integer> stockStatusValues() {

        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN stock = 0 THEN 1 ELSE 0 END), 0) AS out_of_stock,
                    COALESCE(SUM(CASE WHEN stock > 0 AND stock <= 10 THEN 1 ELSE 0 END), 0) AS low_stock,
                    COALESCE(SUM(CASE WHEN stock > 10 THEN 1 ELSE 0 END), 0) AS normal_stock
                FROM store_product
                WHERE is_active = 1
                """;

        List<Integer> values = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                values.add(resultSet.getInt("out_of_stock"));
                values.add(resultSet.getInt("low_stock"));
                values.add(resultSet.getInt("normal_stock"));
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.stockStatusValues error", e);
        }
    }

    public List<Object[]> unsoldProductsThisMonth() {

        String sql = """
                SELECT
                    p.id,
                    p.title,
                    p.stock,
                    p.price,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name,
                    p.created_at
                FROM store_product p
                LEFT JOIN store_category c ON c.id = p.category_id
                WHERE p.is_active = 1
                AND NOT EXISTS (
                    SELECT 1
                    FROM store_orderitem oi
                    JOIN store_order o ON o.id = oi.order_id
                    WHERE oi.product_id = p.id
                    AND o.payment_status = ?
                    AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                    AND YEAR(o.created_at) = YEAR(CURDATE())
                    AND MONTH(o.created_at) = MONTH(CURDATE())
                )
                ORDER BY p.stock DESC, p.created_at ASC
                LIMIT 10
                """;

        List<Object[]> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Object[]{
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getInt("stock"),
                            vnd0(resultSet.getBigDecimal("price")),
                            resultSet.getString("category_name"),
                            resultSet.getTimestamp("created_at")
                    });
                }
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.unsoldProductsThisMonth error", e);
        }
    }

    public List<Object[]> lowStockProducts() {

        String sql = """
                SELECT
                    p.id,
                    p.title,
                    p.stock,
                    p.price,
                    COALESCE(c.name, 'Chưa phân loại') AS category_name
                FROM store_product p
                LEFT JOIN store_category c ON c.id = p.category_id
                WHERE p.is_active = 1
                AND p.stock <= 10
                ORDER BY p.stock ASC, p.title ASC
                LIMIT 10
                """;

        List<Object[]> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getInt("stock"),
                        vnd0(resultSet.getBigDecimal("price")),
                        resultSet.getString("category_name")
                });
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.lowStockProducts error", e);
        }
    }

    public List<Object[]> slowMovingProductsLast30Days() {

        String sql = """
                SELECT
                    p.id,
                    p.title,
                    p.stock,
                    COALESCE(SUM(CASE WHEN o.id IS NOT NULL THEN oi.quantity ELSE 0 END), 0) AS sold,
                    COALESCE(SUM(CASE WHEN o.id IS NOT NULL THEN oi.quantity * oi.price ELSE 0 END), 0) AS revenue
                FROM store_product p
                LEFT JOIN store_orderitem oi ON oi.product_id = p.id
                LEFT JOIN store_order o ON o.id = oi.order_id
                    AND o.payment_status = ?
                    AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                WHERE p.is_active = 1
                GROUP BY p.id, p.title, p.stock
                HAVING sold <= 2
                ORDER BY sold ASC, p.stock DESC
                LIMIT 10
                """;

        List<Object[]> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Object[]{
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getInt("stock"),
                            resultSet.getInt("sold"),
                            vnd0(resultSet.getBigDecimal("revenue"))
                    });
                }
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.slowMovingProductsLast30Days error", e);
        }
    }

    public List<String> categorySoldLabels() {

        String sql = """
                SELECT COALESCE(c.name, 'Chưa phân loại') AS category_name
                FROM store_orderitem oi
                JOIN store_order o ON o.id = oi.order_id
                JOIN store_product p ON p.id = oi.product_id
                LEFT JOIN store_category c ON c.id = p.category_id
                WHERE o.payment_status = ?
                AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY COALESCE(c.name, 'Chưa phân loại')
                ORDER BY SUM(oi.quantity) DESC
                LIMIT 8
                """;

        List<String> labels = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    labels.add(resultSet.getString("category_name"));
                }
            }

            return labels;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.categorySoldLabels error", e);
        }
    }

    public List<Integer> categorySoldValues() {

        String sql = """
                SELECT COALESCE(SUM(oi.quantity), 0) AS sold
                FROM store_orderitem oi
                JOIN store_order o ON o.id = oi.order_id
                JOIN store_product p ON p.id = oi.product_id
                LEFT JOIN store_category c ON c.id = p.category_id
                WHERE o.payment_status = ?
                AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY COALESCE(c.name, 'Chưa phân loại')
                ORDER BY SUM(oi.quantity) DESC
                LIMIT 8
                """;

        List<Integer> values = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    values.add(resultSet.getInt("sold"));
                }
            }

            return values;

        } catch (SQLException e) {
            throw new RuntimeException("AdminStatsDAO.categorySoldValues error", e);
        }
    }

    public List<BigDecimal> categoryRevenueValues() {

        String sql = """
                SELECT COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
                FROM store_orderitem oi
                JOIN store_order o ON o.id = oi.order_id
                JOIN store_product p ON p.id = oi.product_id
                LEFT JOIN store_category c ON c.id = p.category_id
                WHERE o.payment_status = ?
                AND LOWER(o.status) NOT IN ('cancelled', 'canceled')
                AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY COALESCE(c.name, 'Chưa phân loại')
                ORDER BY SUM(oi.quantity) DESC
                LIMIT 8
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
            throw new RuntimeException("AdminStatsDAO.categoryRevenueValues error", e);
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