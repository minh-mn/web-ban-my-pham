package com.webshop.app.model;

import java.math.BigDecimal;

public class CartItem {

    private int orderId;

    private int productId;
    private String title;

    private int quantity;
    private BigDecimal price;

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
    public BigDecimal getSubtotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
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

        if (hasSize && hasType) return variantSize + " - " + variantType;
        if (hasSize) return variantSize;
        if (hasType) return variantType;
        return "Mặc định";
    }

    // ===== GET / SET =====
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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
        this.quantity = quantity < 0 ? 0 : quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
        this.stock = stock < 0 ? 0 : stock;
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