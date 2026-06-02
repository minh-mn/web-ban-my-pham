package com.webshop.app.model;

import java.math.BigDecimal;

public class ProductVariant {

    public static final int DEFAULT_MIN_STOCK = 5;

    private int id;
    private int productId;
    private String sku;
    private String size;
    private String color;
    private String type;
    private BigDecimal extraPrice;
    private int stock;
    private int minStock = DEFAULT_MIN_STOCK;
    private boolean active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = Math.max(productId, 0);
    }

    public String getSku() {
        return sku;
    }

    public String getDisplaySku() {
        if (sku == null || sku.isBlank()) {
            return "SKU-" + productId + "-" + id;
        }

        return sku.trim();
    }

    public void setSku(String sku) {
        this.sku = normalize(sku);
    }

    public String getSize() {
        return size;
    }

    public String getDisplaySize() {
        return size == null || size.isBlank() ? "Mặc định" : size.trim();
    }

    public void setSize(String size) {
        this.size = normalize(size);
    }

    public String getColor() {
        return color;
    }

    public String getDisplayColor() {
        if (color != null && !color.isBlank()) {
            return color.trim();
        }

        if (type != null && !type.isBlank()) {
            return type.trim();
        }

        return "Mặc định";
    }

    public void setColor(String color) {
        this.color = normalize(color);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = normalize(type);
    }

    public BigDecimal getExtraPrice() {
        return extraPrice != null ? extraPrice : BigDecimal.ZERO;
    }

    public void setExtraPrice(BigDecimal extraPrice) {
        this.extraPrice = extraPrice == null ? BigDecimal.ZERO : extraPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(stock, 0);
    }

    public int getMinStock() {
        return minStock <= 0 ? DEFAULT_MIN_STOCK : minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock <= 0 ? DEFAULT_MIN_STOCK : minStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasColor = color != null && !color.isBlank();
        boolean hasType = type != null && !type.isBlank();

        if (hasSize && hasColor) {
            return size.trim() + " - " + color.trim();
        }

        if (hasSize && hasType) {
            return size.trim() + " - " + type.trim();
        }

        if (hasSize) {
            return size.trim();
        }

        if (hasColor) {
            return color.trim();
        }

        if (hasType) {
            return type.trim();
        }

        return "Mặc định";
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }

    public boolean isLowStock() {
        return stock > 0 && stock < getMinStock();
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return "out";
        }

        if (isLowStock()) {
            return "low";
        }

        return "normal";
    }

    public String getStockStatusLabel() {
        return switch (getStockStatus()) {
            case "out" -> "Hết hàng";
            case "low" -> "Sắp hết";
            default -> "Còn hàng";
        };
    }

    public String getStockStatusClass() {
        return switch (getStockStatus()) {
            case "out" -> "stock-out";
            case "low" -> "stock-low";
            default -> "stock-normal";
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
