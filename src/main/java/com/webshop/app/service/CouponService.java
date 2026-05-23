package com.webshop.app.service;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

public class CouponService {

    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private static final Map<String, Integer> RANK_PRIORITY = Map.of(
            "MEMBER", 1,
            "SILVER", 2,
            "GOLD", 3,
            "DIAMOND", 4,
            "VIP", 5
    );

    private final CouponDAO couponDAO = new CouponDAO();

    /**
     * Hàm cũ để tránh lỗi các file khác đang gọi.
     * Nếu chưa truyền rank, hệ thống mặc định user là MEMBER.
     */
    public Coupon validateCoupon(String code, BigDecimal subtotal) {
        return validateCoupon(code, subtotal, DEFAULT_RANK_CODE);
    }

    /**
     * Hàm mới: validate coupon theo mã, tổng tiền hàng và rank hiện tại của user.
     *
     * Điều kiện hợp lệ:
     * - Mã coupon tồn tại
     * - Coupon đang active
     * - Chưa tới ngày bắt đầu thì không dùng được
     * - Quá ngày kết thúc thì không dùng được
     * - Chưa vượt quá số lượt sử dụng
     * - Subtotal phải >= min_order_amount
     * - User rank phải >= min_rank_code của coupon
     */
    public Coupon validateCoupon(
            String code,
            BigDecimal subtotal,
            String userRankCode
    ) {

        if (code == null || code.isBlank()) {
            return null;
        }

        BigDecimal safeSubtotal = normalizeMoney(subtotal);

        Coupon coupon = couponDAO.findByCode(code.trim().toUpperCase());
        if (coupon == null) {
            return null;
        }

        if (!isCouponActiveNow(coupon)) {
            return null;
        }

        if (!isUsageAvailable(coupon)) {
            return null;
        }

        if (!isSubtotalEnough(coupon, safeSubtotal)) {
            return null;
        }

        if (!isRankAllowed(userRankCode, coupon.getMinRankCode())) {
            return null;
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {

        if (coupon == null || subtotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeSubtotal = normalizeMoney(subtotal);

        if (safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = safeSubtotal
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (coupon.getMaxDiscountAmount() != null
                && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(coupon.getMaxDiscountAmount());
        }

        // Không cho tiền giảm vượt quá tổng tiền hàng.
        discount = discount.min(safeSubtotal);

        return discount.max(BigDecimal.ZERO);
    }

    public boolean isCouponActiveNow(Coupon coupon) {

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

        return coupon.getEndDate() == null || !today.isAfter(coupon.getEndDate());
    }

    public boolean isUsageAvailable(Coupon coupon) {

        if (coupon == null) {
            return false;
        }

        int maxUses = coupon.getMaxUses();
        int usedCount = coupon.getUsedCount();

        return maxUses <= 0 || usedCount < maxUses;
    }

    public boolean isSubtotalEnough(Coupon coupon, BigDecimal subtotal) {

        if (coupon == null) {
            return false;
        }

        BigDecimal safeSubtotal = normalizeMoney(subtotal);
        BigDecimal minOrderAmount = normalizeMoney(coupon.getMinOrderAmount());

        return safeSubtotal.compareTo(minOrderAmount) >= 0;
    }

    public boolean isRankAllowed(String userRankCode, String couponMinRankCode) {

        String safeUserRank = normalizeRankCode(userRankCode);
        String safeCouponRank = normalizeRankCode(couponMinRankCode);

        int userPriority = getRankPriority(safeUserRank);
        int couponPriority = getRankPriority(safeCouponRank);

        return userPriority >= couponPriority;
    }

    public String buildCouponErrorMessage(
            Coupon coupon,
            BigDecimal subtotal,
            String userRankCode
    ) {

        if (coupon == null) {
            return "Mã giảm giá không tồn tại hoặc đã bị tắt.";
        }

        if (!isCouponActiveNow(coupon)) {
            return "Mã giảm giá chưa đến thời gian sử dụng hoặc đã hết hạn.";
        }

        if (!isUsageAvailable(coupon)) {
            return "Mã giảm giá đã hết lượt sử dụng.";
        }

        if (!isSubtotalEnough(coupon, subtotal)) {
            return "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này.";
        }

        if (!isRankAllowed(userRankCode, coupon.getMinRankCode())) {
            return "Hạng thành viên của bạn chưa đủ điều kiện để dùng mã này.";
        }

        return "";
    }

    private int getRankPriority(String rankCode) {
        return RANK_PRIORITY.getOrDefault(normalizeRankCode(rankCode), 1);
    }

    private String normalizeRankCode(String rankCode) {
        if (rankCode == null || rankCode.isBlank()) {
            return DEFAULT_RANK_CODE;
        }

        String normalized = rankCode.trim().toUpperCase();

        if (!RANK_PRIORITY.containsKey(normalized)) {
            return DEFAULT_RANK_CODE;
        }

        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value;
    }
}