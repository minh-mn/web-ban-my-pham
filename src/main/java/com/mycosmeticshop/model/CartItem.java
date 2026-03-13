package com.mycosmeticshop.model;

import java.math.BigDecimal;

public class CartItem {

    // Nếu bạn KHÔNG dùng orderId trong giỏ hàng session thì có thể để,
    // nhưng không bắt buộc phải set lúc add cart.
    private int orderId;

    private int productId;
    private String title;

    private int quantity;
    private BigDecimal price;

    // ===== THÊM CHO JSP CART =====
    private String imageUrl; // cart.jsp đang gọi item.imageUrl
    private int stock;       // cart.jsp đang gọi item.stock

    // ===== BUSINESS =====
    public BigDecimal getSubtotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
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

    // ===== imageUrl =====
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ===== stock =====
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock < 0 ? 0 : stock;
    }
}
