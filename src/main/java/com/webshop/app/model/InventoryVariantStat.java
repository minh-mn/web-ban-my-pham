package com.webshop.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InventoryVariantStat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_MIN_STOCK = ProductVariant.DEFAULT_MIN_STOCK;

    private int productId;
    private String productTitle;
    private String categoryName;
    private String brandName;

    private int variantId;
    private String sku;
    private String size;
    private String color;
    private String type;
    private BigDecimal basePrice;
    private BigDecimal extraPrice;
    private int stock;
    private int minStock = DEFAULT_MIN_STOCK;

    private int exportedToday;
    private int exportedThisWeek;
    private int exportedThisMonth;
    private int exportedThisYear;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = Math.max(productId, 0);
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getDisplayProductTitle() {
        if (productTitle == null || productTitle.trim().isEmpty()) {
            return "Sản phẩm #" + productId;
        }

        return productTitle.trim();
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDisplayCategoryName() {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "Chưa phân loại";
        }

        return categoryName.trim();
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getDisplayBrandName() {
        if (brandName == null || brandName.trim().isEmpty()) {
            return "Chưa có thương hiệu";
        }

        return brandName.trim();
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = Math.max(variantId, 0);
    }

    public String getSku() {
        return sku;
    }

    public String getDisplaySku() {
        if (sku == null || sku.trim().isEmpty()) {
            return "SKU-" + productId + "-" + variantId;
        }

        return sku.trim();
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public String getDisplaySize() {
        if (size == null || size.trim().isEmpty()) {
            return "Mặc định";
        }

        return size.trim();
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayColor() {
        if (color != null && !color.trim().isEmpty()) {
            return color.trim();
        }

        if (type != null && !type.trim().isEmpty()) {
            return type.trim();
        }

        return "Mặc định";
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVariantName() {
        return getDisplaySize() + " / " + getDisplayColor();
    }

    public BigDecimal getBasePrice() {
        return basePrice == null ? BigDecimal.ZERO : basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getExtraPrice() {
        return extraPrice == null ? BigDecimal.ZERO : extraPrice;
    }

    public void setExtraPrice(BigDecimal extraPrice) {
        this.extraPrice = extraPrice;
    }

    public BigDecimal getFinalPrice() {
        return getBasePrice().add(getExtraPrice());
    }

    public String getFormattedFinalPrice() {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(getFinalPrice());
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

    public int getMissingQuantity() {
        return Math.max(getMinStock() - stock, 0);
    }

    public int getExportedToday() {
        return exportedToday;
    }

    public void setExportedToday(int exportedToday) {
        this.exportedToday = Math.max(exportedToday, 0);
    }

    public int getExportedThisWeek() {
        return exportedThisWeek;
    }

    public void setExportedThisWeek(int exportedThisWeek) {
        this.exportedThisWeek = Math.max(exportedThisWeek, 0);
    }

    public int getExportedThisMonth() {
        return exportedThisMonth;
    }

    public void setExportedThisMonth(int exportedThisMonth) {
        this.exportedThisMonth = Math.max(exportedThisMonth, 0);
    }

    public int getExportedThisYear() {
        return exportedThisYear;
    }

    public void setExportedThisYear(int exportedThisYear) {
        this.exportedThisYear = Math.max(exportedThisYear, 0);
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

    public String getAlertText() {
        if (isOutOfStock()) {
            return "Đã hết hàng, cần nhập ngay";
        }

        if (isLowStock()) {
            return "Còn " + stock + ", thấp hơn mức tối thiểu " + getMinStock();
        }

        return "Đạt mức an toàn";
    }
}
