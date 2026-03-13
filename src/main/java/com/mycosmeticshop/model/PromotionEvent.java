package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromotionEvent {

    public enum Scope { ALL, CATEGORY, BRAND }

    private int id;
    private String name;

    private Scope scope;                 // ALL | CATEGORY | BRAND
    private DiscountType discountType;   // PERCENT | AMOUNT (tuỳ enum của bạn)
    private BigDecimal discountValue;    // numeric(10,2)
    private BigDecimal maxDiscountAmount; // numeric(10,2) nullable

    // FK (DB là BIGINT, model đang dùng Integer theo project hiện tại)
    private Integer categoryId;
    private Integer brandId;

    private LocalDate startDate;         // NOT NULL (theo schema)
    private LocalDate endDate;           // NOT NULL (theo schema)
    private boolean active;              // is_active

    /* ===================== VIEW ONLY (OPTIONAL) ===================== */
    // Dùng cho trang admin list nếu DAO join ra name
    private String brandName;
    private String categoryName;

    /* ===================== BUSINESS ===================== */

    /**
     * Event hợp lệ tại thời điểm today.
     * - An toàn null: không bị NPE nếu today/startDate/endDate vô tình null
     * - Theo schema hiện tại startDate/endDate NOT NULL, nhưng vẫn safe.
     */
    public boolean isValid(LocalDate today) {
        if (!active || today == null) return false;

        if (startDate != null && today.isBefore(startDate)) return false;
        if (endDate != null && today.isAfter(endDate)) return false;

        return true;
    }

    /* ===================== GETTERS / SETTERS ===================== */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
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

    /* ===================== VIEW ONLY GETTERS/SETTERS ===================== */

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /* ===================== OPTIONAL: HELPER FOR JSP ===================== */

    /**
     * Hiển thị phạm vi áp dụng dễ hiểu trên JSP.
     */
    public String getScopeLabel() {
        if (scope == null) return "";
        switch (scope) {
            case ALL: return "Toàn bộ sản phẩm";
            case CATEGORY: return "Theo danh mục";
            case BRAND: return "Theo thương hiệu";
            default: return scope.name();
        }
    }

    /**
     * Hiển thị chuỗi giảm giá, ví dụ: "10%" hoặc "50000₫"
     * (Nếu DiscountType của bạn chỉ có PERCENT thì vẫn OK, bạn có thể giữ lại phần PERCENT).
     */
    public String getDiscountLabel() {
        if (discountType == null || discountValue == null) return "";
        if (discountType == DiscountType.PERCENT) {
            // discountValue có thể 10.00 -> hiển thị 10%
            return stripTrailingZeros(discountValue) + "%";
        }
        return stripTrailingZeros(discountValue) + "₫";
    }

    private String stripTrailingZeros(BigDecimal v) {
        try {
            return v.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return v.toPlainString();
        }
    }
}
