package com.webshop.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InventoryProductStat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int LOW_STOCK_THRESHOLD = 10;

    private int id;
    private String title;
    private String categoryName;
    private String brandName;
    private int stock;
    private BigDecimal price;

    private int exportedToday;
    private int exportedThisWeek;
    private int exportedThisMonth;
    private int exportedThisYear;

    public InventoryProductStat() {
    }

    public InventoryProductStat(
            int id,
            String title,
            String categoryName,
            String brandName,
            int stock,
            BigDecimal price,
            int exportedToday,
            int exportedThisWeek,
            int exportedThisMonth,
            int exportedThisYear
    ) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.stock = Math.max(stock, 0);
        this.price = price;
        this.exportedToday = Math.max(exportedToday, 0);
        this.exportedThisWeek = Math.max(exportedThisWeek, 0);
        this.exportedThisMonth = Math.max(exportedThisMonth, 0);
        this.exportedThisYear = Math.max(exportedThisYear, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayTitle() {
        if (title == null || title.trim().isEmpty()) {
            return "Sản phẩm #" + id;
        }

        return title.trim();
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(stock, 0);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSafePrice() {
        return price == null ? BigDecimal.ZERO : price;
    }

    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(getSafePrice()) + " đ";
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
        return stock > 0 && stock < LOW_STOCK_THRESHOLD;
    }

    public boolean isNormalStock() {
        return stock >= LOW_STOCK_THRESHOLD;
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
        if (isOutOfStock()) {
            return "Hết hàng";
        }

        if (isLowStock()) {
            return "Sắp hết hàng";
        }

        return "Còn hàng";
    }

    public String getStockStatusClass() {
        if (isOutOfStock()) {
            return "stock-out";
        }

        if (isLowStock()) {
            return "stock-low";
        }

        return "stock-normal";
    }

    public String getStockWarningMessage() {
        if (isOutOfStock()) {
            return "Sản phẩm đã hết hàng, cần nhập thêm ngay.";
        }

        if (isLowStock()) {
            return "Sản phẩm sắp hết hàng, tồn kho dưới " + LOW_STOCK_THRESHOLD + ".";
        }

        return "Tồn kho ổn định.";
    }

    public int getTotalExported() {
        return exportedThisYear;
    }

    @Override
    public String toString() {
        return "InventoryProductStat{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", stock=" + stock +
                ", price=" + price +
                ", exportedToday=" + exportedToday +
                ", exportedThisWeek=" + exportedThisWeek +
                ", exportedThisMonth=" + exportedThisMonth +
                ", exportedThisYear=" + exportedThisYear +
                '}';
    }
}