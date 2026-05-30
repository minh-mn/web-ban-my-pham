package com.webshop.app.model;

import java.io.Serializable;
import java.time.LocalDate;

public class InventoryImportMonthStat implements Serializable {

    private static final long serialVersionUID = 1L;

    private int month;
    private int year;

    private int importQuantity;
    private int importCount;
    private int productCount;

    public InventoryImportMonthStat() {
        LocalDate today = LocalDate.now();
        this.month = today.getMonthValue();
        this.year = today.getYear();
    }

    public InventoryImportMonthStat(int month, int year) {
        this.month = normalizeMonth(month);
        this.year = normalizeYear(year);
    }

    public InventoryImportMonthStat(
            int month,
            int year,
            int importQuantity,
            int importCount,
            int productCount
    ) {
        this.month = normalizeMonth(month);
        this.year = normalizeYear(year);
        this.importQuantity = Math.max(importQuantity, 0);
        this.importCount = Math.max(importCount, 0);
        this.productCount = Math.max(productCount, 0);
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = normalizeMonth(month);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = normalizeYear(year);
    }

    public int getImportQuantity() {
        return importQuantity;
    }

    public void setImportQuantity(int importQuantity) {
        this.importQuantity = Math.max(importQuantity, 0);
    }

    public int getImportCount() {
        return importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = Math.max(importCount, 0);
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = Math.max(productCount, 0);
    }

    public String getMonthLabel() {
        return "Tháng " + month;
    }

    public String getChartLabel() {
        return String.format("%02d/%d", month, year);
    }

    public String getShortChartLabel() {
        return String.format("%02d", month);
    }

    public String getPeriodText() {
        return String.format("%02d/%d", month, year);
    }

    public String getImportQuantityText() {
        return String.valueOf(Math.max(importQuantity, 0));
    }

    public String getImportCountText() {
        return String.valueOf(Math.max(importCount, 0));
    }

    public String getProductCountText() {
        return String.valueOf(Math.max(productCount, 0));
    }

    public boolean hasImportData() {
        return importQuantity > 0 || importCount > 0 || productCount > 0;
    }

    public boolean getHasImportData() {
        return hasImportData();
    }

    public String getSummaryText() {
        if (!hasImportData()) {
            return "Chưa có nhập hàng trong " + getPeriodText();
        }

        return "Đã nhập "
                + importQuantity
                + " sản phẩm trong "
                + importCount
                + " lượt nhập của "
                + productCount
                + " mặt hàng.";
    }

    private int normalizeMonth(int month) {
        if (month < 1 || month > 12) {
            return LocalDate.now().getMonthValue();
        }

        return month;
    }

    private int normalizeYear(int year) {
        if (year < 2000) {
            return LocalDate.now().getYear();
        }

        return year;
    }

    @Override
    public String toString() {
        return "InventoryImportMonthStat{" +
                "month=" + month +
                ", year=" + year +
                ", importQuantity=" + importQuantity +
                ", importCount=" + importCount +
                ", productCount=" + productCount +
                '}';
    }
}