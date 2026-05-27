package com.webshop.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrandDiscount {

    public static final String SCOPE_ALL_BRAND_PRODUCTS = "ALL_BRAND_PRODUCTS";
    public static final String SCOPE_SELECTED_PRODUCTS = "SELECTED_PRODUCTS";

    private int id;

    private int brandId;

    /*
     * Phạm vi áp dụng:
     * - ALL_BRAND_PRODUCTS: áp dụng tất cả sản phẩm thuộc brandId
     * - SELECTED_PRODUCTS: chỉ áp dụng sản phẩm được chọn trong store_branddiscount_product
     */
    private String applyScope = SCOPE_ALL_BRAND_PRODUCTS;

    /*
     * DB thường lưu PERCENT / FIXED.
     * Nếu dữ liệu cũ còn dùng AMOUNT thì setter String sẽ tự chuyển về FIXED.
     */
    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;

    /*
     * Dùng cho Admin list để hiển thị tên brand.
     * Field này không bắt buộc có trong DB.
     */
    private String brandName;

    /*
     * Danh sách sản phẩm cụ thể được áp dụng.
     * Dữ liệu này sẽ được lưu ở bảng store_branddiscount_product.
     */
    private List<Integer> selectedProductIds = new ArrayList<>();

    /* ===================== BUSINESS HELPERS ===================== */

    /**
     * Kiểm tra discount có hiệu lực tại thời điểm today hay không.
     */
    public boolean isValid(LocalDate today) {
        if (!active) {
            return false;
        }

        if (today == null) {
            today = LocalDate.now();
        }

        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    public boolean isActiveNow() {
        return isValid(LocalDate.now());
    }

    public boolean isExpired(LocalDate today) {
        if (today == null) {
            today = LocalDate.now();
        }

        return endDate != null && endDate.isBefore(today);
    }

    public boolean isUpcoming(LocalDate today) {
        if (today == null) {
            today = LocalDate.now();
        }

        return startDate != null && startDate.isAfter(today);
    }

    public boolean isApplyToAllBrandProducts() {
        return SCOPE_ALL_BRAND_PRODUCTS.equals(getApplyScope());
    }

    public boolean isApplyToSelectedProducts() {
        return SCOPE_SELECTED_PRODUCTS.equals(getApplyScope());
    }

    public boolean isPercentDiscount() {
        return getDiscountType() == DiscountType.PERCENT;
    }

    public boolean isFixedDiscount() {
        return getDiscountType() == DiscountType.FIXED;
    }

    /* ===================== GET / SET ===================== */

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


    public String getApplyScope() {
        if (applyScope == null || applyScope.isBlank()) {
            return SCOPE_ALL_BRAND_PRODUCTS;
        }

        return applyScope.trim().toUpperCase();
    }

    public void setApplyScope(String applyScope) {
        if (applyScope == null || applyScope.isBlank()) {
            this.applyScope = SCOPE_ALL_BRAND_PRODUCTS;
        } else {
            this.applyScope = applyScope.trim().toUpperCase();
        }
    }


    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public void setDiscountType(String discountType) {
        if (discountType == null || discountType.isBlank()) {
            this.discountType = null;
            return;
        }

        String normalized = discountType.trim().toUpperCase();

        /*
         * Tương thích nếu DB/form cũ dùng AMOUNT.
         * Project hiện tại nên dùng FIXED cho giảm theo số tiền.
         */
        if ("AMOUNT".equals(normalized)) {
            normalized = "FIXED";
        }

        this.discountType = DiscountType.valueOf(normalized);
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName == null ? null : brandName.trim();
    }


    public List<Integer> getSelectedProductIds() {
        if (selectedProductIds == null) {
            return Collections.emptyList();
        }

        return selectedProductIds;
    }

    public void setSelectedProductIds(List<Integer> selectedProductIds) {
        if (selectedProductIds == null) {
            this.selectedProductIds = new ArrayList<>();
        } else {
            this.selectedProductIds = new ArrayList<>(selectedProductIds);
        }
    }

    public void addSelectedProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            return;
        }

        if (this.selectedProductIds == null) {
            this.selectedProductIds = new ArrayList<>();
        }

        if (!this.selectedProductIds.contains(productId)) {
            this.selectedProductIds.add(productId);
        }
    }

    public void clearSelectedProductIds() {
        if (this.selectedProductIds == null) {
            this.selectedProductIds = new ArrayList<>();
        } else {
            this.selectedProductIds.clear();
        }
    }

    @Override
    public String toString() {
        return "BrandDiscount{" +
                "id=" + id +
                ", brandId=" + brandId +
                ", applyScope='" + getApplyScope() + '\'' +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", maxDiscountAmount=" + maxDiscountAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                ", brandName='" + brandName + '\'' +
                ", selectedProductIds=" + getSelectedProductIds() +
                '}';
    }
}