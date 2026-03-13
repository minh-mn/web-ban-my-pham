package com.mycosmeticshop.model;

import java.math.BigDecimal;

public class OrderItem {

    private int id;
    private int orderId;
    private int productId;

    // giá 1 sản phẩm tại thời điểm mua
    private BigDecimal price;

    private int quantity;

    // ===== VIEW FIELDS để JSP hiển thị (thiếu nên gây lỗi 500) =====
    private String productName;
    private String imageUrl;

    // ===== BUSINESS =====
    public BigDecimal getSubtotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // ===== GETTERS & SETTERS =====
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ===== NEW: cho JSP =====
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
