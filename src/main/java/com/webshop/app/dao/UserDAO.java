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
                                          max_uses, used_count, is_active, start_date, end_date,
                                          type, description, min_order_amount
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
                                       max_uses, used_count, is_active, start_date, end_date,
                                       type, description, min_order_amount
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
                       max_uses, used_count, is_active, start_date, end_date,
                       type, description, min_order_amount
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
            start_date,
            end_date,
            used_count,
            is_active,
            max_uses,
            max_discount_amount,
            description,
            min_order_amount,
            created_at,
            updated_at,
            type
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)
        """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coupon.getCode());
            statement.setInt(2, coupon.getDiscountPercent());
            setNullableDate(statement, 3, coupon.getStartDate());
            setNullableDate(statement, 4, coupon.getEndDate());

            statement.setInt(5, 0); // used_count
            statement.setBoolean(6, coupon.isActive());

            statement.setInt(7, coupon.getMaxUses());
            setNullableBigDecimal(statement, 8, coupon.getMaxDiscountAmount());

            statement.setString(9,
                    coupon.getDescription() == null || coupon.getDescription().trim().isEmpty()
                            ? null
                            : coupon.getDescription()
            );

            if (coupon.getMinOrderAmount() == null) {
                statement.setNull(10, java.sql.Types.DECIMAL);
            } else {
                statement.setBigDecimal(10, coupon.getMinOrderAmount());
            }

            statement.setString(11, coupon.getType());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.create error", e);
        }
    }

    public void update(Coupon coupon) {
        String sql = """
            UPDATE store_coupon
            SET code=?,
                discount_percent=?,
                start_date=?,
                end_date=?,
                used_count=?,
                is_active=?,
                max_uses=?,
                max_discount_amount=?,
                description=?,
                min_order_amount=?,
                updated_at=NOW(),
                type=?
            WHERE id=?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coupon.getCode());
            statement.setInt(2, coupon.getDiscountPercent());

            setNullableDate(statement, 3, coupon.getStartDate());
            setNullableDate(statement, 4, coupon.getEndDate());

            statement.setInt(5, coupon.getUsedCount());

            statement.setBoolean(6, coupon.isActive());

            statement.setInt(7, coupon.getMaxUses());
            setNullableBigDecimal(statement, 8, coupon.getMaxDiscountAmount());

            statement.setString(9,
                    (coupon.getDescription() == null || coupon.getDescription().trim().isEmpty())
                            ? null
                            : coupon.getDescription()
            );

            if (coupon.getMinOrderAmount() == null) {
                statement.setNull(10, java.sql.Types.DECIMAL);
            } else {
                statement.setBigDecimal(10, coupon.getMinOrderAmount());
            }

            statement.setString(11, coupon.getType());

            statement.setInt(12, coupon.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.update error", e);
        }
    }

    public void delete(int id) {
        String sql = """
                DELETE FROM store_coupon
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

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

        coupon.setType(resultSet.getString("type"));
        coupon.setDescription(resultSet.getString("description"));
        coupon.setMinOrderAmount(resultSet.getBigDecimal("min_order_amount"));

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

    public int deactivateExpiredCoupons() {

        String sql = """
            UPDATE store_coupon
            SET is_active = 0
            WHERE is_active = 1
            AND end_date < CURDATE()
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            return ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean increaseUsedCountIfAvailable(
            Connection conn,
            long couponId
    ) throws SQLException {

        String sql = """
            UPDATE store_coupon
            SET used_count = used_count + 1
            WHERE id = ?
            AND is_active = 1
            AND used_count < max_uses
            AND CURDATE() BETWEEN start_date AND end_date
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, couponId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean softDisable(long id) {

        String sql = """
            UPDATE store_coupon
            SET is_active = 0
            WHERE id = ?
        """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Coupon> findActiveCouponsForHome() {

        List<Coupon> coupons = new ArrayList<>();

        String sql = """
        SELECT id, code, discount_percent, max_discount_amount,
               max_uses, used_count, is_active, start_date, end_date,
               type, description, min_order_amount
        FROM store_coupon
        WHERE is_active = 1
          AND (start_date IS NULL OR start_date <= CURDATE())
          AND (end_date IS NULL OR end_date >= CURDATE())
        ORDER BY discount_percent DESC, id DESC
        LIMIT 6
        """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findActiveCouponsForHome error", e);
        }

        return coupons;
    }

    /* ===================== LƯU VÀ HIỂN THỊ VOUCHER USER ===================== */

    // 1. Hàm thực hiện lưu mã vào ví của User khi bấm nút ở trang chủ
    public boolean saveVoucherToUserCollection(int userId, String couponCode) {
        // Sử dụng INSERT IGNORE để nếu người dùng có bấm lưu trùng mã đã lưu rồi thì database sẽ bỏ qua không báo lỗi
        String sql = """
        INSERT IGNORE INTO user_coupon (user_id, coupon_id)
        SELECT ?, id FROM store_coupon WHERE code = ? AND is_active = 1
    """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, couponCode.trim());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Hàm lấy danh sách các mã giảm giá mà User đó đã lưu để hiển thị lên trang Account
    public List<Coupon> findSavedCouponsByUserId(int userId) {
        List<Coupon> coupons = new ArrayList<>();
        String sql = """
        SELECT c.* FROM store_coupon c
        JOIN user_coupon uc ON c.id = uc.coupon_id
        WHERE uc.user_id = ? AND c.is_active = 1
    """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Gọi hàm mapRow có sẵn trong CouponDAO của bạn để map dữ liệu
                    coupons.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findSavedCouponsByUserId error", e);
        }
        return coupons;
    }
}
