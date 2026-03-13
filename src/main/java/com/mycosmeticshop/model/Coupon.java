package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Coupon {

    private int id;
    private String code;
    private int discountPercent;
    private BigDecimal maxDiscountAmount;

    // ✅ BẮT BUỘC (map với store_coupon.max_uses NOT NULL)
    private int maxUses;

    private int usedCount;
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;

    /* ================= BUSINESS ================= */

    public boolean isValid(LocalDate today) {
        return active &&
               (startDate == null || !today.isBefore(startDate)) &&
               (endDate == null || !today.isAfter(endDate)) &&
               (maxUses <= 0 || usedCount < maxUses);
    }

    /* ================= GET / SET ================= */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    // ===== maxUses =====
    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public boolean isActive() {
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
}
