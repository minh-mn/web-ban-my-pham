package com.webshop.app.model;

import java.math.BigDecimal;

public class CartItem {

    private int orderId;

    private int productId;
    private String title;

    private int quantity;

    /*
     * price = giá đang bán hiện tại.
     * Nếu sản phẩm có giảm giá thì price là giá sau giảm.
     */
    private BigDecimal price;

    /*
     * originalPrice = giá gốc trước giảm.
     * Dùng để hiển thị giá gốc gạch ngang trong giỏ hàng.
     */
    private BigDecimal originalPrice;

    private String imageUrl;
    private int stock;

    // ===== VARIANT FIELDS =====
    // cartKey giúp phân biệt cùng 1 sản phẩm nhưng chọn size/loại khác nhau.
    // Ví dụ: 51:2, 51:3, 51:0
    private String cartKey;
    private int variantId;
    private String variantSize;
    private String variantType;
    private String variantName;
    private BigDecimal variantExtraPrice;

    // ===== BUSINESS =====

    /**
     * Tổng tiền theo giá đang bán.
     * Nếu sản phẩm có giảm giá thì đây là tổng tiền sau giảm.
     */
    public BigDecimal getSubtotal() {
        return getSafePrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Tổng tiền theo giá gốc.
     * Dùng để hiện giá gốc gạch ngang ở cột tạm tính.
     */
    public BigDecimal getOriginalSubtotal() {
        return getSafeOriginalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Kiểm tra sản phẩm có đang được giảm giá hay không.
     */
    public boolean isDiscounted() {
        return originalPrice != null
                && price != null
                && originalPrice.compareTo(price) > 0;
    }

    public boolean isHasVariant() {
        return variantId > 0;
    }

    public String getVariantDisplayName() {
        if (variantName != null && !variantName.isBlank()) {
            return variantName;
        }

        boolean hasSize = variantSize != null && !variantSize.isBlank();
        boolean hasType = variantType != null && !variantType.isBlank();

        if (hasSize && hasType) {
            return variantSize + " - " + variantType;
        }

        if (hasSize) {
            return variantSize;
        }

        if (hasType) {
            return variantType;
        }

        return "Mặc định";
    }

    private BigDecimal getSafePrice() {
        return price != null ? price : BigDecimal.ZERO;
    }

    private BigDecimal getSafeOriginalPrice() {
        /*
         * Nếu chưa có originalPrice thì lấy price hiện tại.
         * Như vậy JSP gọi originalSubtotal sẽ không bị lỗi.
         */
        if (originalPrice != null) {
            return originalPrice;
        }

        return getSafePrice();
    }

    // ===== GET / SET =====

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = Math.max(orderId, 0);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = Math.max(productId, 0);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public BigDecimal getPrice() {
        return getSafePrice();
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public BigDecimal getOriginalPrice() {
        return getSafeOriginalPrice();
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(stock, 0);
    }

    public String getCartKey() {
        return cartKey;
    }

    public void setCartKey(String cartKey) {
        this.cartKey = cartKey;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = Math.max(variantId, 0);
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getVariantExtraPrice() {
        return variantExtraPrice != null ? variantExtraPrice : BigDecimal.ZERO;
    }

    public void setVariantExtraPrice(BigDecimal variantExtraPrice) {
        this.variantExtraPrice = variantExtraPrice;
    }
}