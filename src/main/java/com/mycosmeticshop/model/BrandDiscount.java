package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BrandDiscount {

    private int id;
    private int brandId;

    // DB thường lưu "PERCENT" / "AMOUNT" (VARCHAR) hoặc mã số (INT)
    private DiscountType discountType;

    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active;

    // ===== (OPTIONAL) dùng cho Admin list để hiển thị tên brand =====
    // Nếu bạn join brand trong DAO thì set field này, không ảnh hưởng DB.
    private String brandName;

    /**
     * Kiểm tra discount có hiệu lực tại thời điểm today hay không.
     * Safe-null: tránh NullPointerException nếu dữ liệu chưa đủ.
     */
    public boolean isValid(LocalDate today) {
        if (!active) return false;
        if (today == null || startDate == null || endDate == null) return false;
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    // ================= GET/SET =================

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getBrandId() {
        return brandId;
    }
    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }
    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
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

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    // ===== Optional: brandName =====
    public String getBrandName() {
        return brandName;
    }
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}
