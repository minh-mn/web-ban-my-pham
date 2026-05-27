package com.webshop.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromotionEvent {

    public enum Scope {
        ALL,
        CATEGORY,
        BRAND,
        PRODUCTS
    }

    private int id;
    private String name;

    /*
     * Phạm vi áp dụng:
     * - ALL: toàn cửa hàng
     * - CATEGORY: theo danh mục
     * - BRAND: theo thương hiệu
     * - PRODUCTS: chỉ áp dụng sản phẩm được chọn trong store_promotionevent_product
     */
    private Scope scope = Scope.ALL;

    /*
     * Kiểu giảm giá:
     * - PERCENT: giảm theo %
     * - FIXED/AMOUNT: giảm theo số tiền, tùy enum DiscountType hiện tại của project
     */
    private DiscountType discountType;

    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;

    private Integer categoryId;
    private Integer brandId;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active;

    /*
     * View only: dùng khi DAO join để hiển thị trên admin list.
     */
    private String brandName;
    private String categoryName;

    /*
     * Danh sách sản phẩm cụ thể được áp dụng.
     * Dữ liệu này sẽ được lưu ở bảng store_promotionevent_product.
     */
    private List<Integer> selectedProductIds = new ArrayList<>();

    /* ===================== BUSINESS HELPERS ===================== */

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

    public boolean isApplyToAll() {
        return getScope() == Scope.ALL;
    }

    public boolean isApplyToCategory() {
        return getScope() == Scope.CATEGORY;
    }

    public boolean isApplyToBrand() {
        return getScope() == Scope.BRAND;
    }

    public boolean isApplyToSelectedProducts() {
        return getScope() == Scope.PRODUCTS;
    }

    public boolean isPercentDiscount() {
        return discountType != null && "PERCENT".equalsIgnoreCase(discountType.name());
    }

    public boolean isFixedDiscount() {
        return discountType != null && !"PERCENT".equalsIgnoreCase(discountType.name());
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
        this.name = name == null ? null : name.trim();
    }


    public Scope getScope() {
        return scope == null ? Scope.ALL : scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope == null ? Scope.ALL : scope;
    }

    public void setScope(String scope) {
        if (scope == null || scope.isBlank()) {
            this.scope = Scope.ALL;
            return;
        }

        this.scope = Scope.valueOf(scope.trim().toUpperCase());
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
         * Ưu tiên dùng đúng enum hiện có.
         * Nếu project dùng FIXED thì FIXED chạy.
         * Nếu project dùng AMOUNT thì AMOUNT chạy.
         */
        try {
            this.discountType = DiscountType.valueOf(normalized);
            return;
        } catch (IllegalArgumentException ignored) {
            // Thử fallback bên dưới.
        }

        if ("AMOUNT".equals(normalized)) {
            try {
                this.discountType = DiscountType.valueOf("FIXED");
                return;
            } catch (IllegalArgumentException ignored) {
                // Nếu enum không có FIXED thì báo lỗi ở cuối.
            }
        }

        if ("FIXED".equals(normalized)) {
            try {
                this.discountType = DiscountType.valueOf("AMOUNT");
                return;
            } catch (IllegalArgumentException ignored) {
                // Nếu enum không có AMOUNT thì báo lỗi ở cuối.
            }
        }

        throw new IllegalArgumentException("DiscountType không hợp lệ: " + discountType);
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

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId > 0 ? categoryId : null;
    }


    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId > 0 ? brandId : null;
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


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName == null ? null : categoryName.trim();
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

    /* ===================== JSP HELPERS ===================== */

    public String getScopeLabel() {
        Scope currentScope = getScope();

        return switch (currentScope) {
            case ALL -> "Toàn cửa hàng";
            case CATEGORY -> "Theo danh mục";
            case BRAND -> "Theo thương hiệu";
            case PRODUCTS -> "Sản phẩm cụ thể";
        };
    }

    public String getDiscountLabel() {
        if (discountType == null || discountValue == null) {
            return "";
        }

        if (isPercentDiscount()) {
            return stripTrailingZeros(discountValue) + "%";
        }

        return stripTrailingZeros(discountValue) + "₫";
    }

    private String stripTrailingZeros(BigDecimal value) {
        try {
            return value.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return value == null ? "" : value.toPlainString();
        }
    }

    @Override
    public String toString() {
        return "PromotionEvent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", scope=" + getScope() +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", maxDiscountAmount=" + maxDiscountAmount +
                ", categoryId=" + categoryId +
                ", brandId=" + brandId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                ", brandName='" + brandName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", selectedProductIds=" + getSelectedProductIds() +
                '}';
    }
}