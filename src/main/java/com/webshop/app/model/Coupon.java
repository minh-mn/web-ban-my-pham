package com.webshop.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Coupon {

    private int id;
    private String code;
    private int discountPercent;
    private BigDecimal maxDiscountAmount;

    private int maxUses;
    private int usedCount;

    private boolean active;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal minOrderAmount;
    private String minRankCode;

    public Coupon() {
        this.discountPercent = 0;
        this.maxUses = 0;
        this.usedCount = 0;
        this.active = true;
        this.minOrderAmount = BigDecimal.ZERO;
        this.minRankCode = "MEMBER";
    }

    /* ================= GET / SET ================= */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = normalizeCode(code);
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
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
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        if (maxDiscountAmount == null) {
            this.maxDiscountAmount = null;
            return;
        }

        if (maxDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.maxDiscountAmount = null;
            return;
        }

        this.maxDiscountAmount = money0(maxDiscountAmount);
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = Math.max(maxUses, 0);
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = Math.max(usedCount, 0);
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount == null ? BigDecimal.ZERO : money0(minOrderAmount);
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        if (minOrderAmount == null || minOrderAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.minOrderAmount = BigDecimal.ZERO;
            return;
        }

        this.minOrderAmount = money0(minOrderAmount);
    }

    public String getMinRankCode() {
        return normalizeRankCode(minRankCode);
    }

    public void setMinRankCode(String minRankCode) {
        this.minRankCode = normalizeRankCode(minRankCode);
    }

    /* ================= BUSINESS HELPERS ================= */

    public boolean hasMaxDiscountAmount() {
        return maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasUsageLimit() {
        return maxUses > 0;
    }

    public boolean isUsageLimitReached() {
        return hasUsageLimit() && usedCount >= maxUses;
    }

    public boolean hasMinimumOrderAmount() {
        return getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isStarted() {
        return startDate == null || !LocalDate.now().isBefore(startDate);
    }

    public boolean isNotExpired() {
        return endDate == null || !LocalDate.now().isAfter(endDate);
    }

    public boolean isAvailableToday() {
        return active && isStarted() && isNotExpired() && !isUsageLimitReached();
    }

    public String getDiscountLabel() {
        return discountPercent + "%";
    }

    public String getMinOrderAmountLabel() {
        return getMinOrderAmount().toPlainString();
    }

    public BigDecimal calculateDiscountAmount(BigDecimal orderAmount) {
        BigDecimal safeOrderAmount = money0(orderAmount);

        if (safeOrderAmount.compareTo(BigDecimal.ZERO) <= 0 || discountPercent <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = safeOrderAmount
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (hasMaxDiscountAmount() && discountAmount.compareTo(maxDiscountAmount) > 0) {
            return money0(maxDiscountAmount);
        }

        return money0(discountAmount);
    }

    /* ================= NORMALIZE HELPERS ================= */

    private static BigDecimal money0(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private static String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toUpperCase();
    }

    private static String normalizeRankCode(String value) {
        if (value == null || value.isBlank()) {
            return "MEMBER";
        }

        return value.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountPercent=" + discountPercent +
                ", maxDiscountAmount=" + maxDiscountAmount +
                ", maxUses=" + maxUses +
                ", usedCount=" + usedCount +
                ", active=" + active +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", minOrderAmount=" + minOrderAmount +
                ", minRankCode='" + minRankCode + '\'' +
                '}';
    }
}