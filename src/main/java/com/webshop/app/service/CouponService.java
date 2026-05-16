package com.webshop.app.service;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.model.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class CouponService {

    private final CouponDAO couponDAO = new CouponDAO();

    public Coupon validateCoupon(String code, BigDecimal subtotal) {

        if (code == null || code.isBlank()) return null;

        Coupon cp = couponDAO.findByCode(code.trim());
        if (cp == null) return null;

        LocalDate now = LocalDate.now();

        if (!cp.isActive()) return null;

        if (cp.getStartDate() != null && now.isBefore(cp.getStartDate()))
            return null;

        if (cp.getEndDate() != null && now.isAfter(cp.getEndDate()))
            return null;

        if (cp.getMaxUses() > 0 && cp.getUsedCount() >= cp.getMaxUses())
            return null;

        return cp;
    }

    public BigDecimal calculateDiscount(Coupon cp, BigDecimal subtotal) {

        if (cp == null || subtotal == null) return BigDecimal.ZERO;

        BigDecimal discount = subtotal
                .multiply(BigDecimal.valueOf(cp.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (cp.getMaxDiscountAmount() != null) {
            discount = discount.min(cp.getMaxDiscountAmount());
        }

        return discount.max(BigDecimal.ZERO);
    }
}
