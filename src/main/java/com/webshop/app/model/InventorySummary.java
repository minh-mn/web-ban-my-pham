package com.webshop.app.model;

import java.io.Serializable;

public class InventorySummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private int productCount;
    private int totalStock;
    private int outOfStockCount;
    private int lowStockCount;
    private int normalStockCount;

    private int exportedToday;
    private int exportedThisWeek;
    private int exportedThisMonth;
    private int exportedThisYear;

    public InventorySummary() {
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = Math.max(productCount, 0);
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = Math.max(totalStock, 0);
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = Math.max(outOfStockCount, 0);
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = Math.max(lowStockCount, 0);
    }

    public int getNormalStockCount() {
        return normalStockCount;
    }

    public void setNormalStockCount(int normalStockCount) {
        this.normalStockCount = Math.max(normalStockCount, 0);
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

    public boolean hasLowStock() {
        return lowStockCount > 0;
    }

    public boolean hasOutOfStock() {
        return outOfStockCount > 0;
    }

    public int getAlertCount() {
        return lowStockCount + outOfStockCount;
    }

    public int getAvailableProductCount() {
        return productCount - outOfStockCount;
    }

    @Override
    public String toString() {
        return "InventorySummary{" +
                "productCount=" + productCount +
                ", totalStock=" + totalStock +
                ", outOfStockCount=" + outOfStockCount +
                ", lowStockCount=" + lowStockCount +
                ", normalStockCount=" + normalStockCount +
                ", exportedToday=" + exportedToday +
                ", exportedThisWeek=" + exportedThisWeek +
                ", exportedThisMonth=" + exportedThisMonth +
                ", exportedThisYear=" + exportedThisYear +
                '}';
    }
}
