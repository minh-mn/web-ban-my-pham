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

public class UserCouponDAO {

    /*
     * Rank level:
     * MEMBER  = 0
     * SILVER  = 1
     * GOLD    = 2
     * DIAMOND = 3
     * VIP     = 4
     */

    /* =========================
     * PUBLIC METHODS
     * ========================= */

    public List<UserCouponView> findAvailableCouponsByRankCode(String userRankCode) {

        String sql = """
                SELECT
                    id,
                    code,
                    discount_percent,
                    max_discount_amount,
                    min_order_amount,
                    start_date,
                    end_date,
                    is_active AS active,
                    max_uses,
                    used_count,
                    COALESCE(min_rank_code, 'MEMBER') AS min_rank_code
                FROM store_coupon
                WHERE is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (
                        max_uses IS NULL
                        OR max_uses <= 0
                        OR COALESCE(used_count, 0) < max_uses
                  )
                ORDER BY
                    CASE COALESCE(min_rank_code, 'MEMBER')
                        WHEN 'VIP' THEN 5
                        WHEN 'DIAMOND' THEN 4
                        WHEN 'GOLD' THEN 3
                        WHEN 'SILVER' THEN 2
                        WHEN 'MEMBER' THEN 1
                        ELSE 0
                    END DESC,
                    discount_percent DESC,
                    end_date ASC
                """;

        String safeUserRank = normalizeRankCode(userRankCode);
        int userRankLevel = rankLevel(safeUserRank);

        List<UserCouponView> coupons = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UserCouponView coupon = mapCoupon(resultSet);

                if (canUseCoupon(userRankLevel, coupon.getMinRankCode())) {
                    coupons.add(coupon);
                }
            }

            return coupons;

        } catch (SQLException e) {
            throw new RuntimeException("UserCouponDAO.findAvailableCouponsByRankCode error", e);
        }
    }

    public List<UserCouponView> findAvailableCouponsByRankLevel(int userRankLevel) {
        String rankCode = rankCodeFromLevel(userRankLevel);
        return findAvailableCouponsByRankCode(rankCode);
    }

    public List<UserCouponView> findAvailableCouponsForMember() {
        return findAvailableCouponsByRankCode("MEMBER");
    }

    public boolean canUserRankUseCoupon(String userRankCode, String couponCode) {

        if (couponCode == null || couponCode.isBlank()) {
            return false;
        }

        String sql = """
                SELECT
                    COALESCE(min_rank_code, 'MEMBER') AS min_rank_code,
                    is_active AS active,
                    start_date,
                    end_date,
                    max_uses,
                    used_count
                FROM store_coupon
                WHERE code = ?
                  AND is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (
                        max_uses IS NULL
                        OR max_uses <= 0
                        OR COALESCE(used_count, 0) < max_uses
                  )
                LIMIT 1
                """;

        int userRankLevel = rankLevel(userRankCode);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, couponCode.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                String minRankCode = normalizeRankCode(resultSet.getString("min_rank_code"));

                return canUseCoupon(userRankLevel, minRankCode);
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserCouponDAO.canUserRankUseCoupon error", e);
        }
    }

    public UserCouponView findAvailableCouponByCodeAndRank(String couponCode, String userRankCode) {

        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }

        String sql = """
                SELECT
                    id,
                    code,
                    discount_percent,
                    max_discount_amount,
                    min_order_amount,
                    start_date,
                    end_date,
                    is_active AS active,
                    max_uses,
                    used_count,
                    COALESCE(min_rank_code, 'MEMBER') AS min_rank_code
                FROM store_coupon
                WHERE code = ?
                  AND is_active = 1
                  AND (start_date IS NULL OR start_date <= CURDATE())
                  AND (end_date IS NULL OR end_date >= CURDATE())
                  AND (
                        max_uses IS NULL
                        OR max_uses <= 0
                        OR COALESCE(used_count, 0) < max_uses
                  )
                LIMIT 1
                """;

        int userRankLevel = rankLevel(userRankCode);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, couponCode.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                UserCouponView coupon = mapCoupon(resultSet);

                if (!canUseCoupon(userRankLevel, coupon.getMinRankCode())) {
                    return null;
                }

                return coupon;
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserCouponDAO.findAvailableCouponByCodeAndRank error", e);
        }
    }


    public boolean hasUserUsedCoupon(int userId, long couponId) {
        try (Connection connection = DBConnection.getConnection()) {
            return hasUserUsedCoupon(connection, userId, couponId);
        } catch (SQLException e) {
            throw new RuntimeException("UserCouponDAO.hasUserUsedCoupon error", e);
        }
    }

    public boolean hasUserUsedCoupon(Connection conn, int userId, long couponId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (userId <= 0 || couponId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM user_coupon
                WHERE user_id = ?
                  AND coupon_id = ?
                  AND is_used = 1
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setLong(2, couponId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void markCouponUsed(Connection conn, int userId, long couponId, int orderId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (userId <= 0 || couponId <= 0 || orderId <= 0) {
            throw new SQLException("Invalid userId/couponId/orderId when marking coupon used.");
        }

        String sql = """
                INSERT INTO user_coupon
                    (user_id, coupon_id, saved_at, is_used, used_at, used_order_id)
                VALUES
                    (?, ?, NOW(), 1, NOW(), ?)
                ON DUPLICATE KEY UPDATE
                    is_used = 1,
                    used_at = COALESCE(used_at, NOW()),
                    used_order_id = COALESCE(used_order_id, VALUES(used_order_id))
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setLong(2, couponId);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    public List<UserCouponView> findAvailableCouponsForUser(int userId, String userRankCode) {
        String sql = """
                SELECT
                    c.id,
                    c.code,
                    c.discount_percent,
                    c.max_discount_amount,
                    c.min_order_amount,
                    c.start_date,
                    c.end_date,
                    c.is_active AS active,
                    c.max_uses,
                    c.used_count,
                    COALESCE(c.min_rank_code, 'MEMBER') AS min_rank_code
                FROM store_coupon c
                WHERE c.is_active = 1
                  AND (c.start_date IS NULL OR c.start_date <= CURDATE())
                  AND (c.end_date IS NULL OR c.end_date >= CURDATE())
                  AND (
                        c.max_uses IS NULL
                        OR c.max_uses <= 0
                        OR COALESCE(c.used_count, 0) < c.max_uses
                  )
                  AND NOT EXISTS (
                        SELECT 1
                        FROM user_coupon uc
                        WHERE uc.user_id = ?
                          AND uc.coupon_id = c.id
                          AND uc.is_used = 1
                  )
                ORDER BY
                    CASE COALESCE(c.min_rank_code, 'MEMBER')
                        WHEN 'VIP' THEN 5
                        WHEN 'DIAMOND' THEN 4
                        WHEN 'GOLD' THEN 3
                        WHEN 'SILVER' THEN 2
                        WHEN 'MEMBER' THEN 1
                        ELSE 0
                    END DESC,
                    c.discount_percent DESC,
                    c.end_date ASC
                """;

        String safeUserRank = normalizeRankCode(userRankCode);
        int userRankLevel = rankLevel(safeUserRank);
        List<UserCouponView> coupons = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UserCouponView coupon = mapCoupon(resultSet);

                    if (canUseCoupon(userRankLevel, coupon.getMinRankCode())) {
                        coupons.add(coupon);
                    }
                }
            }

            return coupons;

        } catch (SQLException e) {
            throw new RuntimeException("UserCouponDAO.findAvailableCouponsForUser error", e);
        }
    }

    /* =========================
     * HELPER LOGIC
     * ========================= */

    private boolean canUseCoupon(int userRankLevel, String minRankCode) {
        int couponMinRankLevel = rankLevel(minRankCode);
        return userRankLevel >= couponMinRankLevel;
    }

    private int rankLevel(String rankCode) {

        String safeRank = normalizeRankCode(rankCode);

        return switch (safeRank) {
            case "VIP" -> 4;
            case "DIAMOND" -> 3;
            case "GOLD" -> 2;
            case "SILVER" -> 1;
            case "MEMBER" -> 0;
            default -> 0;
        };
    }

    private String rankCodeFromLevel(int level) {

        if (level >= 4) {
            return "VIP";
        }

        if (level == 3) {
            return "DIAMOND";
        }

        if (level == 2) {
            return "GOLD";
        }

        if (level == 1) {
            return "SILVER";
        }

        return "MEMBER";
    }

    private String normalizeRankCode(String rankCode) {

        if (rankCode == null || rankCode.isBlank()) {
            return "MEMBER";
        }

        return rankCode.trim().toUpperCase();
    }

    private BigDecimal money0(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private UserCouponView mapCoupon(ResultSet resultSet) throws SQLException {

        UserCouponView coupon = new UserCouponView();

        coupon.setId(resultSet.getLong("id"));
        coupon.setCode(resultSet.getString("code"));
        coupon.setDiscountPercent(resultSet.getInt("discount_percent"));
        coupon.setMaxDiscountAmount(money0(resultSet.getBigDecimal("max_discount_amount")));
        coupon.setMinOrderAmount(money0(resultSet.getBigDecimal("min_order_amount")));
        coupon.setStartDate(resultSet.getTimestamp("start_date"));
        coupon.setEndDate(resultSet.getTimestamp("end_date"));
        coupon.setActive(resultSet.getBoolean("active"));

        Object maxUsesObj = resultSet.getObject("max_uses");
        coupon.setMaxUses(maxUsesObj == null ? null : resultSet.getInt("max_uses"));

        Object usedCountObj = resultSet.getObject("used_count");
        coupon.setUsedCount(usedCountObj == null ? 0 : resultSet.getInt("used_count"));

        coupon.setMinRankCode(normalizeRankCode(resultSet.getString("min_rank_code")));
        coupon.setMinRankLabel(toVietnameseRankName(coupon.getMinRankCode()));

        return coupon;
    }

    private String toVietnameseRankName(String rankCode) {

        String safeRank = normalizeRankCode(rankCode);

        return switch (safeRank) {
            case "VIP" -> "VIP";
            case "DIAMOND" -> "Kim cương";
            case "GOLD" -> "Vàng";
            case "SILVER" -> "Bạc";
            case "MEMBER" -> "Thành viên";
            default -> "Thành viên";
        };
    }

    /* =========================
     * DTO FOR JSP
     * ========================= */

    public static class UserCouponView {

        private Long id;
        private String code;
        private Integer discountPercent;
        private BigDecimal maxDiscountAmount;
        private BigDecimal minOrderAmount;
        private java.sql.Timestamp startDate;
        private java.sql.Timestamp endDate;
        private Boolean active;
        private Integer maxUses;
        private Integer usedCount;
        private String minRankCode;
        private String minRankLabel;

        public UserCouponView() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCode() {
            return code == null ? "" : code;
        }

        public void setCode(String code) {
            this.code = code == null ? "" : code.trim();
        }

        public Integer getDiscountPercent() {
            return discountPercent == null ? 0 : discountPercent;
        }

        public void setDiscountPercent(Integer discountPercent) {
            if (discountPercent == null) {
                this.discountPercent = 0;
                return;
            }

            if (discountPercent < 0) {
                this.discountPercent = 0;
                return;
            }

            if (discountPercent > 100) {
                this.discountPercent = 100;
                return;
            }

            this.discountPercent = discountPercent;
        }

        public BigDecimal getMaxDiscountAmount() {
            return maxDiscountAmount == null ? BigDecimal.ZERO : maxDiscountAmount;
        }

        public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
            this.maxDiscountAmount = maxDiscountAmount == null ? BigDecimal.ZERO : maxDiscountAmount;
        }

        public BigDecimal getMinOrderAmount() {
            return minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
        }

        public void setMinOrderAmount(BigDecimal minOrderAmount) {
            this.minOrderAmount = minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
        }

        public java.sql.Timestamp getStartDate() {
            return startDate;
        }

        public void setStartDate(java.sql.Timestamp startDate) {
            this.startDate = startDate;
        }

        public java.sql.Timestamp getEndDate() {
            return endDate;
        }

        public void setEndDate(java.sql.Timestamp endDate) {
            this.endDate = endDate;
        }

        public Boolean getActive() {
            return active != null && active;
        }

        public void setActive(Boolean active) {
            this.active = active != null && active;
        }

        public Integer getMaxUses() {
            return maxUses;
        }

        public void setMaxUses(Integer maxUses) {
            this.maxUses = maxUses;
        }

        public Integer getUsedCount() {
            return usedCount == null ? 0 : usedCount;
        }

        public void setUsedCount(Integer usedCount) {
            this.usedCount = usedCount == null ? 0 : usedCount;
        }

        public String getMinRankCode() {
            return minRankCode == null ? "MEMBER" : minRankCode;
        }

        public void setMinRankCode(String minRankCode) {
            if (minRankCode == null || minRankCode.isBlank()) {
                this.minRankCode = "MEMBER";
            } else {
                this.minRankCode = minRankCode.trim().toUpperCase();
            }
        }

        public String getMinRankLabel() {
            return minRankLabel == null ? "Thành viên" : minRankLabel;
        }

        public void setMinRankLabel(String minRankLabel) {
            this.minRankLabel = minRankLabel == null ? "Thành viên" : minRankLabel;
        }

        public boolean hasMaxDiscount() {
            return getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean hasMinOrderAmount() {
            return getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean hasUsageLimit() {
            return maxUses != null && maxUses > 0;
        }

        public int getRemainingUses() {
            if (!hasUsageLimit()) {
                return -1;
            }

            return Math.max(maxUses - getUsedCount(), 0);
        }

        public String getDiscountLabel() {
            return "Giảm " + getDiscountPercent() + "%";
        }

        public String getConditionLabel() {
            if (!hasMinOrderAmount()) {
                return "Không yêu cầu giá trị đơn tối thiểu";
            }

            return "Áp dụng cho đơn từ " + getMinOrderAmount().toPlainString() + " ₫";
        }

        @Override
        public String toString() {
            return "UserCouponView{" +
                    "id=" + id +
                    ", code='" + code + '\'' +
                    ", discountPercent=" + discountPercent +
                    ", maxDiscountAmount=" + maxDiscountAmount +
                    ", minOrderAmount=" + minOrderAmount +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", active=" + active +
                    ", maxUses=" + maxUses +
                    ", usedCount=" + usedCount +
                    ", minRankCode='" + minRankCode + '\'' +
                    ", minRankLabel='" + minRankLabel + '\'' +
                    '}';
        }
    }
}