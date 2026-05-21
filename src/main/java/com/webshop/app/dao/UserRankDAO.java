package com.webshop.app.dao;

import com.webshop.app.model.UserRank;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRankDAO {

    private static final String PAID = "PAID";

    private static BigDecimal money0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= FIND RANK ================= */

    public List<UserRank> findAllActive() {

        String sql = """
                SELECT
                    id,
                    code,
                    name,
                    min_spent,
                    discount_percent,
                    css_class,
                    active,
                    created_at
                FROM store_rank
                WHERE active = 1
                ORDER BY min_spent ASC
                """;

        List<UserRank> ranks = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ranks.add(mapRank(resultSet));
            }

            return ranks;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.findAllActive error", e);
        }
    }

    public UserRank findByCode(String code) {

        String sql = """
                SELECT
                    id,
                    code,
                    name,
                    min_spent,
                    discount_percent,
                    css_class,
                    active,
                    created_at
                FROM store_rank
                WHERE code = ?
                AND active = 1
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRank(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.findByCode error", e);
        }
    }

    public UserRank findBestRankByTotalSpent(BigDecimal totalSpent) {

        String sql = """
                SELECT
                    id,
                    code,
                    name,
                    min_spent,
                    discount_percent,
                    css_class,
                    active,
                    created_at
                FROM store_rank
                WHERE active = 1
                AND min_spent <= ?
                ORDER BY min_spent DESC
                LIMIT 1
                """;

        BigDecimal safeTotalSpent = money0(totalSpent);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, safeTotalSpent);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRank(resultSet);
                }
            }

            return getDefaultRank();

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.findBestRankByTotalSpent error", e);
        }
    }

    public UserRank findNextRank(BigDecimal totalSpent) {

        String sql = """
                SELECT
                    id,
                    code,
                    name,
                    min_spent,
                    discount_percent,
                    css_class,
                    active,
                    created_at
                FROM store_rank
                WHERE active = 1
                AND min_spent > ?
                ORDER BY min_spent ASC
                LIMIT 1
                """;

        BigDecimal safeTotalSpent = money0(totalSpent);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, safeTotalSpent);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRank(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.findNextRank error", e);
        }
    }

    /* ================= USER SPENDING ================= */

    public BigDecimal calculateTotalPaidSpentByUserId(long userId) {

        String sql = """
                SELECT COALESCE(SUM(total), 0) AS total_spent
                FROM store_order
                WHERE user_id = ?
                AND payment_status = ?
                AND LOWER(status) NOT IN ('cancelled', 'canceled')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return money0(resultSet.getBigDecimal("total_spent"));
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.calculateTotalPaidSpentByUserId error", e);
        }
    }

    public int countPaidOrdersByUserId(long userId) {

        String sql = """
                SELECT COUNT(*) AS total_orders
                FROM store_order
                WHERE user_id = ?
                AND payment_status = ?
                AND LOWER(status) NOT IN ('cancelled', 'canceled')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, PAID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total_orders");
                }
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.countPaidOrdersByUserId error", e);
        }
    }

    public UserRank findCurrentRankByUserId(long userId) {
        BigDecimal totalSpent = calculateTotalPaidSpentByUserId(userId);
        return findBestRankByTotalSpent(totalSpent);
    }

    public UserRank findNextRankByUserId(long userId) {
        BigDecimal totalSpent = calculateTotalPaidSpentByUserId(userId);
        return findNextRank(totalSpent);
    }

    public BigDecimal calculateAmountToNextRank(long userId) {

        BigDecimal totalSpent = calculateTotalPaidSpentByUserId(userId);
        UserRank nextRank = findNextRank(totalSpent);

        if (nextRank == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal amountNeeded = nextRank.getMinSpent().subtract(totalSpent);

        if (amountNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return money0(amountNeeded);
    }

    /* ================= CHECK / SEED SUPPORT ================= */

    public int countRanks() {

        String sql = """
                SELECT COUNT(*)
                FROM store_rank
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.countRanks error", e);
        }
    }

    public void insertDefaultRanksIfEmpty() {

        if (countRanks() > 0) {
            return;
        }

        String sql = """
                INSERT INTO store_rank
                    (code, name, min_spent, discount_percent, css_class, active)
                VALUES
                    ('MEMBER', 'Thành viên', 0, 0, 'rank-member', 1),
                    ('SILVER', 'Bạc', 1000000, 3, 'rank-silver', 1),
                    ('GOLD', 'Vàng', 3000000, 5, 'rank-gold', 1),
                    ('DIAMOND', 'Kim cương', 7000000, 8, 'rank-diamond', 1),
                    ('VIP', 'VIP', 15000000, 10, 'rank-vip', 1)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserRankDAO.insertDefaultRanksIfEmpty error", e);
        }
    }

    /* ================= MAPPER ================= */

    private UserRank mapRank(ResultSet resultSet) throws SQLException {

        UserRank rank = new UserRank();

        rank.setId(resultSet.getLong("id"));
        rank.setCode(resultSet.getString("code"));
        rank.setName(resultSet.getString("name"));
        rank.setMinSpent(resultSet.getBigDecimal("min_spent"));
        rank.setDiscountPercent(resultSet.getInt("discount_percent"));
        rank.setCssClass(resultSet.getString("css_class"));
        rank.setActive(resultSet.getBoolean("active"));
        rank.setCreatedAt(resultSet.getTimestamp("created_at"));

        return rank;
    }

    private UserRank getDefaultRank() {

        return new UserRank(
                "MEMBER",
                "Thành viên",
                BigDecimal.ZERO,
                0,
                "rank-member"
        );
    }
}