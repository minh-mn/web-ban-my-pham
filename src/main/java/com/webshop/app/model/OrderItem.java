package com.webshop.app.model;

import java.math.BigDecimal;

public class OrderItem {

    private int id;
    private int orderId;
    private int productId;

    /*
     * Variant có thể null vì không phải sản phẩm nào cũng có biến thể.
     * Dùng Integer thay vì int để map được NULL từ database.
     */
    private Integer variantId;

    /*
     * Lưu snapshot variant tại thời điểm mua.
     * Không phụ thuộc vào việc sau này admin đổi/xóa variant.
     */
    private String variantName;
    private String variantSize;
    private String variantType;

    /*
     * Giá 1 sản phẩm tại thời điểm mua.
     * Nếu sản phẩm có variant, price nên là giá cuối cùng đã bao gồm extraPrice của variant.
     */
    private BigDecimal price;

    private int quantity;

    /*
     * VIEW FIELDS để JSP hiển thị.
     */
    private String productName;
    private String imageUrl;

    /* ================= BUSINESS ================= */

    public BigDecimal getSubtotal() {
        if (price == null) {
            return BigDecimal.ZERO;
        }

        if (quantity <= 0) {
            return BigDecimal.ZERO;
        }

        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean hasVariant() {
        return variantId != null
                || notBlank(variantName)
                || notBlank(variantSize)
                || notBlank(variantType);
    }

    public String getVariantDisplayName() {
        StringBuilder builder = new StringBuilder();

        if (notBlank(variantName)) {
            builder.append(variantName.trim());
        }

        if (notBlank(variantSize)) {
            if (builder.length() > 0) {
                builder.append(" - ");
            }
            builder.append("Size: ").append(variantSize.trim());
        }

        if (notBlank(variantType)) {
            if (builder.length() > 0) {
                builder.append(" - ");
            }
            builder.append(variantType.trim());
        }

        return builder.toString();
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /* ================= GETTERS & SETTERS ================= */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


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


    public Integer getVariantId() {
        return variantId;
    }

    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }


    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = normalizeNullableText(variantName);
    }


    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = normalizeNullableText(variantSize);
    }


    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = normalizeNullableText(variantType);
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            this.price = BigDecimal.ZERO;
        } else {
            this.price = price;
        }
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = normalizeNullableText(productName);
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = normalizeNullableText(imageUrl);
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}