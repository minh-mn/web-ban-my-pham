package com.webshop.app.dao;

import com.webshop.app.model.Coupon;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    /* ===================== FRONTEND ===================== */

    public Coupon findByCode(String code) {
        String sql = """
                SELECT id, code, discount_percent, max_discount_amount,
                       max_uses, used_count, is_active, start_date, end_date
                FROM store_coupon
                WHERE code = ?
                  AND is_active = 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findByCode error", e);
        }
    }

    public void increaseUsedCount(Connection connection, int couponId) throws SQLException {
        String sql = """
                UPDATE store_coupon
                SET used_count = used_count + 1
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, couponId);
            statement.executeUpdate();
        }
    }

    /* ===================== ADMIN ===================== */

    public List<Coupon> findAll() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT id, code, discount_percent, max_discount_amount,
                       max_uses, used_count, is_active, start_date, end_date
                FROM store_coupon
                ORDER BY id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAll error", e);
        }

        return coupons;
    }

    public Coupon findById(int id) {
        String sql = """
                SELECT id, code, discount_percent, max_discount_amount,
                       max_uses, used_count, is_active, start_date, end_date
                FROM store_coupon
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findById error", e);
        }
    }

    public void create(Coupon coupon) {
        String sql = """
                INSERT INTO store_coupon
                (
                    code,
                    discount_percent,
                    max_discount_amount,
                    max_uses,
                    used_count,
                    is_active,
                    start_date,
                    end_date
                )
                VALUES (?, ?, ?, ?, 0, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coupon.getCode());
            statement.setInt(2, coupon.getDiscountPercent());
            setNullableBigDecimal(statement, 3, coupon.getMaxDiscountAmount());
            statement.setInt(4, coupon.getMaxUses());
            statement.setBoolean(5, coupon.isActive());
            setNullableDate(statement, 6, coupon.getStartDate());
            setNullableDate(statement, 7, coupon.getEndDate());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.create error", e);
        }
    }

    public void update(Coupon coupon) {
        String sql = """
                UPDATE store_coupon
                SET code = ?,
                    discount_percent = ?,
                    max_discount_amount = ?,
                    max_uses = ?,
                    is_active = ?,
                    start_date = ?,
                    end_date = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coupon.getCode());
            statement.setInt(2, coupon.getDiscountPercent());
            setNullableBigDecimal(statement, 3, coupon.getMaxDiscountAmount());
            statement.setInt(4, coupon.getMaxUses());
            statement.setBoolean(5, coupon.isActive());
            setNullableDate(statement, 6, coupon.getStartDate());
            setNullableDate(statement, 7, coupon.getEndDate());
            statement.setInt(8, coupon.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.update error", e);
        }
    }

    /*
     * Nếu coupon đã được dùng trong đơn hàng:
     * - Không xóa thật vì store_order.coupon_id đang tham chiếu store_coupon.id.
     * - Chỉ tắt is_active = 0 để coupon không còn dùng được.
     *
     * Nếu coupon chưa từng được dùng:
     * - Cho phép hard delete.
     */
    public void delete(int id) {
        try (Connection connection = DBConnection.getConnection()) {

            if (isUsedInOrder(connection, id)) {
                deactivate(connection, id);
                return;
            }

            String sql = """
                    DELETE FROM store_coupon
                    WHERE id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {
        String sql = """
                UPDATE store_coupon
                SET is_active = CASE
                    WHEN is_active = 1 THEN 0
                    ELSE 1
                END
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.toggleActive error", e);
        }
    }

    public boolean existsByCode(String code) {
        String sql = """
                SELECT 1
                FROM store_coupon
                WHERE code = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.existsByCode error", e);
        }
    }

    public boolean isValid(Coupon coupon) {
        if (coupon == null) {
            return false;
        }

        if (!coupon.isActive()) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            return false;
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            return false;
        }

        return coupon.getMaxUses() <= 0 || coupon.getUsedCount() < coupon.getMaxUses();
    }

    /* ===================== DELETE HELPER ===================== */

    private boolean isUsedInOrder(Connection connection, int couponId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM store_order
                WHERE coupon_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, couponId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total") > 0;
                }

                return false;
            }
        }
    }

    private void deactivate(Connection connection, int id) throws SQLException {
        String sql = """
                UPDATE store_coupon
                SET is_active = 0
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /* ===================== MAPPER / HELPER ===================== */

    private Coupon mapRow(ResultSet resultSet) throws SQLException {
        Coupon coupon = new Coupon();

        coupon.setId(resultSet.getInt("id"));
        coupon.setCode(resultSet.getString("code"));
        coupon.setDiscountPercent(resultSet.getInt("discount_percent"));
        coupon.setMaxDiscountAmount(resultSet.getBigDecimal("max_discount_amount"));
        coupon.setMaxUses(resultSet.getInt("max_uses"));
        coupon.setUsedCount(resultSet.getInt("used_count"));
        coupon.setActive(resultSet.getBoolean("is_active"));

        Date startDate = resultSet.getDate("start_date");
        Date endDate = resultSet.getDate("end_date");

        coupon.setStartDate(startDate == null ? null : startDate.toLocalDate());
        coupon.setEndDate(endDate == null ? null : endDate.toLocalDate());

        return coupon;
    }

    private void setNullableDate(
            PreparedStatement statement,
            int index,
            LocalDate date
    ) throws SQLException {

        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private void setNullableBigDecimal(
            PreparedStatement statement,
            int index,
            java.math.BigDecimal value
    ) throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }
}