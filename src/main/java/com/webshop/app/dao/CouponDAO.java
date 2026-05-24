package com.webshop.app.dao;

import com.webshop.app.model.Coupon;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
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

    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private static final String COUPON_SELECT_COLUMNS = """
            id,
            code,
            discount_percent,
            max_discount_amount,
            max_uses,
            used_count,
            is_active,
            start_date,
            end_date,
            type,
            description,
            min_order_amount,
            min_rank_code
            """;

    /* =========================================================
       FRONTEND / CHECKOUT
    ========================================================= */

    /**
     * Tìm coupon theo code, KHÔNG lọc active/hạn dùng.
     *
     * Lý do:
     * - CheckoutServlet cần phân biệt:
     *   + Mã không tồn tại.
     *   + Mã tồn tại nhưng bị tắt.
     *   + Mã tồn tại nhưng hết hạn.
     *   + Mã tồn tại nhưng chưa đủ điều kiện đơn hàng.
     */
    public Coupon findByCode(String code) {
        if (isBlank(code)) {
            return null;
        }

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

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

    /**
     * Tìm coupon đang active + còn hạn + còn lượt.
     * Dùng cho các luồng chỉ cần mã có thể xem/lưu, chưa xét min_order_amount.
     */
    public Coupon findUsableBaseByCode(String code) {
        if (isBlank(code)) {
            return null;
        }

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                  AND is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (max_uses <= 0 OR used_count < max_uses)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findUsableBaseByCode error", e);
        }
    }

    /**
     * Dùng cho JS kiểm tra mã nhập tay:
     * lấy tất cả mã đang active, còn hạn, còn lượt.
     * Không lọc min_order_amount để checkout có thể hiện mã chưa đủ điều kiện ở trạng thái mờ.
     */
    public List<Coupon> findAllActiveCouponsForCheckout() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon
                WHERE is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (max_uses <= 0 OR used_count < max_uses)
                ORDER BY
                  min_order_amount ASC,
                  discount_percent DESC,
                  COALESCE(max_discount_amount, 999999999) DESC,
                  id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                coupons.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAllActiveCouponsForCheckout error", e);
        }

        return coupons;
    }

    /**
     * Lấy danh sách mã dùng được cho tổng đơn hiện tại.
     * Dùng khi chỉ muốn hiện các mã đã đủ điều kiện.
     */
    public List<Coupon> findAvailableCouponsForCheckout(BigDecimal subtotal) {
        List<Coupon> coupons = new ArrayList<>();
        BigDecimal safeSubtotal = safeMoney(subtotal);

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon
                WHERE is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (max_uses <= 0 OR used_count < max_uses)
                  AND (min_order_amount IS NULL OR min_order_amount <= ?)
                ORDER BY
                  discount_percent DESC,
                  COALESCE(max_discount_amount, 999999999) DESC,
                  id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, safeSubtotal);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coupons.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findAvailableCouponsForCheckout error", e);
        }

        return coupons;
    }

    /**
     * Kiểm tra coupon đủ điều kiện cơ bản: active, còn hạn, còn lượt.
     */
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

    /**
     * Kiểm tra coupon có dùng được cho tổng đơn hiện tại không.
     */
    public boolean isUsableForSubtotal(Coupon coupon, BigDecimal subtotal) {
        if (!isValid(coupon)) {
            return false;
        }

        BigDecimal safeSubtotal = safeMoney(subtotal);
        BigDecimal minOrderAmount = safeMoney(coupon.getMinOrderAmount());

        return safeSubtotal.compareTo(minOrderAmount) >= 0;
    }

    /**
     * Trả lý do lỗi rõ ràng cho CheckoutServlet khi user nhập mã tay.
     */
    public String getCouponInvalidReason(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) {
            return "Mã khuyến mãi không tồn tại trong hệ thống.";
        }

        if (!coupon.isActive()) {
            return "Mã khuyến mãi hiện không còn hoạt động.";
        }

        LocalDate today = LocalDate.now();

        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            return "Mã khuyến mãi chưa đến thời gian sử dụng.";
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            return "Mã khuyến mãi đã hết hạn.";
        }

        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return "Mã khuyến mãi đã hết lượt sử dụng.";
        }

        BigDecimal safeSubtotal = safeMoney(subtotal);
        BigDecimal minOrderAmount = safeMoney(coupon.getMinOrderAmount());

        if (safeSubtotal.compareTo(minOrderAmount) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
        }

        return null;
    }

    /**
     * Tăng used_count trong transaction, có kiểm tra còn lượt + còn hạn.
     */
    public void increaseUsedCount(Connection connection, int couponId) throws SQLException {
        String sql = """
                UPDATE store_coupon
                SET used_count = used_count + 1,
                    updated_at = NOW()
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, couponId);
            statement.executeUpdate();
        }
    }

    public boolean increaseUsedCountIfAvailable(Connection conn, long couponId) throws SQLException {
        String sql = """
                UPDATE store_coupon
                SET used_count = used_count + 1,
                    updated_at = NOW()
                WHERE id = ?
                  AND is_active = 1
                  AND (max_uses <= 0 OR used_count < max_uses)
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, couponId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Coupon> findActiveCouponsForHome() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
                FROM store_coupon
                WHERE is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (max_uses <= 0 OR used_count < max_uses)
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

    /* =========================================================
       ADMIN
    ========================================================= */

    public List<Coupon> findAll() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
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
                SELECT
                """ + COUPON_SELECT_COLUMNS + """
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
                    min_rank_code,
                    created_at,
                    updated_at,
                    type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(coupon.getCode()));
            statement.setInt(2, coupon.getDiscountPercent());

            setNullableDate(statement, 3, coupon.getStartDate());
            setNullableDate(statement, 4, coupon.getEndDate());

            statement.setInt(5, 0);
            statement.setBoolean(6, coupon.isActive());

            statement.setInt(7, Math.max(coupon.getMaxUses(), 0));
            setNullableBigDecimal(statement, 8, coupon.getMaxDiscountAmount());

            statement.setString(9, normalizeNullableText(coupon.getDescription()));
            statement.setBigDecimal(10, normalizeMinOrderAmount(coupon.getMinOrderAmount()));
            statement.setString(11, normalizeRankCode(coupon.getMinRankCode()));
            statement.setString(12, normalizeCouponType(coupon.getType()));

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
                    start_date = ?,
                    end_date = ?,
                    used_count = ?,
                    is_active = ?,
                    max_uses = ?,
                    max_discount_amount = ?,
                    description = ?,
                    min_order_amount = ?,
                    min_rank_code = ?,
                    updated_at = NOW(),
                    type = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(coupon.getCode()));
            statement.setInt(2, coupon.getDiscountPercent());

            setNullableDate(statement, 3, coupon.getStartDate());
            setNullableDate(statement, 4, coupon.getEndDate());

            statement.setInt(5, Math.max(coupon.getUsedCount(), 0));
            statement.setBoolean(6, coupon.isActive());

            statement.setInt(7, Math.max(coupon.getMaxUses(), 0));
            setNullableBigDecimal(statement, 8, coupon.getMaxDiscountAmount());

            statement.setString(9, normalizeNullableText(coupon.getDescription()));
            statement.setBigDecimal(10, normalizeMinOrderAmount(coupon.getMinOrderAmount()));
            statement.setString(11, normalizeRankCode(coupon.getMinRankCode()));
            statement.setString(12, normalizeCouponType(coupon.getType()));

            statement.setInt(13, coupon.getId());

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
                END,
                    updated_at = NOW()
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
        if (isBlank(code)) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizeCode(code));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.existsByCode error", e);
        }
    }

    public boolean softDisable(long id) {
        String sql = """
                UPDATE store_coupon
                SET is_active = 0,
                    updated_at = NOW()
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.softDisable error", e);
        }
    }

    public int deactivateExpiredCoupons() {
        String sql = """
                UPDATE store_coupon
                SET is_active = 0,
                    updated_at = NOW()
                WHERE is_active = 1
                  AND end_date IS NOT NULL
                  AND end_date < CURDATE()
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.deactivateExpiredCoupons error", e);
        }
    }

    /* =========================================================
       USER COUPON WALLET
    ========================================================= */

    public boolean saveVoucherToUserCollection(int userId, String couponCode) {
        if (userId <= 0 || isBlank(couponCode)) {
            return false;
        }

        String sql = """
                INSERT IGNORE INTO user_coupon (user_id, coupon_id)
                SELECT ?, id
                FROM store_coupon
                WHERE UPPER(code) = UPPER(?)
                  AND is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (max_uses <= 0 OR used_count < max_uses)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, normalizeCode(couponCode));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.saveVoucherToUserCollection error", e);
        }
    }

    public List<Coupon> findSavedCouponsByUserId(int userId) {
        List<Coupon> coupons = new ArrayList<>();

        if (userId <= 0) {
            return coupons;
        }

        String sql = """
                SELECT c.id,
                       c.code,
                       c.discount_percent,
                       c.max_discount_amount,
                       c.max_uses,
                       c.used_count,
                       c.is_active,
                       c.start_date,
                       c.end_date,
                       c.type,
                       c.description,
                       c.min_order_amount,
                       c.min_rank_code
                FROM store_coupon c
                JOIN user_coupon uc ON c.id = uc.coupon_id
                WHERE uc.user_id = ?
                  AND c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (c.max_uses <= 0 OR c.used_count < c.max_uses)
                ORDER BY uc.saved_at DESC, c.discount_percent DESC, c.id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    coupons.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CouponDAO.findSavedCouponsByUserId error", e);
        }

        return coupons;
    }

    /* =========================================================
       MAPPER / HELPER
    ========================================================= */

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
        coupon.setMinRankCode(resultSet.getString("min_rank_code"));

        Date startDate = resultSet.getDate("start_date");
        Date endDate = resultSet.getDate("end_date");

        coupon.setStartDate(startDate == null ? null : startDate.toLocalDate());
        coupon.setEndDate(endDate == null ? null : endDate.toLocalDate());

        return coupon;
    }

    private void setNullableDate(PreparedStatement statement, int index, LocalDate date)
            throws SQLException {

        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private void setNullableBigDecimal(PreparedStatement statement, int index, BigDecimal value)
            throws SQLException {

        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    private BigDecimal normalizeMinOrderAmount(BigDecimal value) {
        return safeMoney(value);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String normalizeCouponType(String type) {
        if (type == null || type.isBlank()) {
            return "DISCOUNT";
        }

        return type.trim().toUpperCase();
    }

    private String normalizeRankCode(String rankCode) {
        if (rankCode == null || rankCode.isBlank()) {
            return DEFAULT_RANK_CODE;
        }

        return rankCode.trim().toUpperCase();
    }

    private String normalizeNullableText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return text.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
