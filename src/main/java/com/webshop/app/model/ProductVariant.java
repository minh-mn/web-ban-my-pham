package com.webshop.app.model;

import java.math.BigDecimal;

public class ProductVariant {

    private int id;
    private int productId;
    private String size;
    private String type;
    private BigDecimal extraPrice;
    private int stock;
    private boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public BigDecimal getExtraPrice() {
        return extraPrice != null ? extraPrice : BigDecimal.ZERO;
    }

    public void setExtraPrice(BigDecimal extraPrice) {
        this.extraPrice = extraPrice;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(stock, 0);
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public String getDisplayName() {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasType = type != null && !type.isBlank();

        if (hasSize && hasType) {
            return size + " - " + type;
        }

        if (hasSize) {
            return size;
        }

        if (hasType) {
            return type;
        }

        return "Mặc định";
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }
}