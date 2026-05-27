package com.webshop.app.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Coupon {

    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_BRAND = "BRAND";
    public static final String SCOPE_PRODUCTS = "PRODUCTS";

    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private int id;

    private String code;

    /*
     * Giữ discountPercent để tương thích với logic cũ.
     * Các phần mới sẽ ưu tiên dùng discountType + discountValue.
     */
    private int discountPercent;

    private DiscountType discountType = DiscountType.PERCENT;

    private BigDecimal discountValue = BigDecimal.ZERO;

    private BigDecimal maxDiscountAmount;

    private int maxUses;

    private int usedCount;

    private boolean active;

    private String type;

    private String description;

    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /*
     * Phạm vi áp dụng coupon:
     * - ALL: áp dụng toàn bộ giỏ hàng
     * - BRAND: áp dụng sản phẩm thuộc brandId
     * - PRODUCTS: áp dụng sản phẩm được chọn trong store_coupon_product
     */
    private String applyScope = SCOPE_ALL;

    private Integer brandId;

    private String brandName;

    private List<Integer> selectedProductIds = new ArrayList<>();

    /*
     * Giữ applicableProducts để tương thích với code/JSP cũ nếu còn dùng.
     * Sau này nên thay bằng selectedProductIds + store_coupon_product.
     */
    private String applicableProducts;

    // Rank tối thiểu được phép dùng coupon: MEMBER, SILVER, GOLD, DIAMOND, VIP
    private String minRankCode = DEFAULT_RANK_CODE;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private LocalDate startDate;

    private LocalDate endDate;

    /* ===================== GET / SET ===================== */

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
        this.code = code == null ? null : code.trim().toUpperCase();
    }


    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;

        /*
         * Đồng bộ dữ liệu cũ sang discountValue nếu coupon đang dùng giảm theo phần trăm.
         */
        if (this.discountType == null || this.discountType == DiscountType.PERCENT) {
            this.discountType = DiscountType.PERCENT;
            this.discountValue = BigDecimal.valueOf(discountPercent);
        }
    }


    public DiscountType getDiscountType() {
        return discountType == null ? DiscountType.PERCENT : discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType == null ? DiscountType.PERCENT : discountType;
    }

    public void setDiscountType(String discountType) {
        if (discountType == null || discountType.isBlank()) {
            this.discountType = DiscountType.PERCENT;
            return;
        }

        String normalized = discountType.trim().toUpperCase();

        /*
         * Tương thích nếu form/database cũ dùng AMOUNT.
         * Project hiện tại đang dùng FIXED cho giảm theo số tiền.
         */
        if ("AMOUNT".equals(normalized)) {
            normalized = "FIXED";
        }

        this.discountType = DiscountType.valueOf(normalized);
    }


    public BigDecimal getDiscountValue() {
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            if (discountPercent > 0) {
                return BigDecimal.valueOf(discountPercent);
            }
            return BigDecimal.ZERO;
        }

        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue == null ? BigDecimal.ZERO : discountValue;

        /*
         * Đồng bộ ngược lại discountPercent để code cũ vẫn chạy được.
         */
        if (getDiscountType() == DiscountType.PERCENT) {
            this.discountPercent = this.discountValue.intValue();
        }
    }


    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }


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


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }


    public BigDecimal getMinOrderAmount() {
        return minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }


    public String getApplyScope() {
        if (applyScope == null || applyScope.isBlank()) {
            return SCOPE_ALL;
        }

        return applyScope.trim().toUpperCase();
    }

    public void setApplyScope(String applyScope) {
        if (applyScope == null || applyScope.isBlank()) {
            this.applyScope = SCOPE_ALL;
        } else {
            this.applyScope = applyScope.trim().toUpperCase();
        }
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


    public String getApplicableProducts() {
        return applicableProducts;
    }

    public void setApplicableProducts(String applicableProducts) {
        this.applicableProducts = applicableProducts;
    }


    public String getMinRankCode() {
        if (minRankCode == null || minRankCode.isBlank()) {
            return DEFAULT_RANK_CODE;
        }

        return minRankCode.trim().toUpperCase();
    }

    public void setMinRankCode(String minRankCode) {
        if (minRankCode == null || minRankCode.isBlank()) {
            this.minRankCode = DEFAULT_RANK_CODE;
        } else {
            this.minRankCode = minRankCode.trim().toUpperCase();
        }
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim().toUpperCase();
    }

    /* ===================== HELPER METHODS ===================== */

    public boolean isApplyToAll() {
        return SCOPE_ALL.equals(getApplyScope());
    }

    public boolean isApplyToBrand() {
        return SCOPE_BRAND.equals(getApplyScope());
    }

    public boolean isApplyToSelectedProducts() {
        return SCOPE_PRODUCTS.equals(getApplyScope());
    }

    public boolean isPercentDiscount() {
        return getDiscountType() == DiscountType.PERCENT;
    }

    public boolean isFixedDiscount() {
        return getDiscountType() == DiscountType.FIXED;
    }

    public boolean hasUsageLimit() {
        return maxUses > 0;
    }

    public boolean isUsageLimitReached() {
        return hasUsageLimit() && usedCount >= maxUses;
    }

    public boolean hasDateRange() {
        return startDate != null || endDate != null;
    }

    public boolean isExpired(LocalDate now) {
        if (now == null) {
            now = LocalDate.now();
        }

        return endDate != null && endDate.isBefore(now);
    }

    public boolean isUpcoming(LocalDate now) {
        if (now == null) {
            now = LocalDate.now();
        }

        return startDate != null && startDate.isAfter(now);
    }

    public boolean isActiveNow() {
        LocalDate now = LocalDate.now();

        return active
                && !isExpired(now)
                && !isUpcoming(now)
                && !isUsageLimitReached();
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", discountPercent=" + discountPercent +
                ", discountType=" + getDiscountType() +
                ", discountValue=" + getDiscountValue() +
                ", maxDiscountAmount=" + maxDiscountAmount +
                ", maxUses=" + maxUses +
                ", usedCount=" + usedCount +
                ", active=" + active +
                ", type='" + type + '\'' +
                ", applyScope='" + getApplyScope() + '\'' +
                ", brandId=" + brandId +
                ", minRankCode='" + getMinRankCode() + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}