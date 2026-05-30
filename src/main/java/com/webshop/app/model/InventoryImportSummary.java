package com.webshop.app.model;

import java.io.Serializable;
import java.time.LocalDate;

public class InventoryImportSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private int selectedMonth;
    private int selectedYear;

    private int monthlyImportQuantity;
    private int monthlyImportCount;
    private int monthlyProductCount;

    private int yearlyImportQuantity;
    private int yearlyImportCount;
    private int yearlyProductCount;

    public InventoryImportSummary() {
        LocalDate today = LocalDate.now();
        this.selectedMonth = today.getMonthValue();
        this.selectedYear = today.getYear();
    }

    public InventoryImportSummary(int selectedMonth, int selectedYear) {
        this.selectedMonth = normalizeMonth(selectedMonth);
        this.selectedYear = normalizeYear(selectedYear);
    }

    public int getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(int selectedMonth) {
        this.selectedMonth = normalizeMonth(selectedMonth);
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(int selectedYear) {
        this.selectedYear = normalizeYear(selectedYear);
    }

    public int getMonthlyImportQuantity() {
        return monthlyImportQuantity;
    }

    public void setMonthlyImportQuantity(int monthlyImportQuantity) {
        this.monthlyImportQuantity = Math.max(monthlyImportQuantity, 0);
    }

    public int getMonthlyImportCount() {
        return monthlyImportCount;
    }

    public void setMonthlyImportCount(int monthlyImportCount) {
        this.monthlyImportCount = Math.max(monthlyImportCount, 0);
    }

    public int getMonthlyProductCount() {
        return monthlyProductCount;
    }

    public void setMonthlyProductCount(int monthlyProductCount) {
        this.monthlyProductCount = Math.max(monthlyProductCount, 0);
    }

    public int getYearlyImportQuantity() {
        return yearlyImportQuantity;
    }

    public void setYearlyImportQuantity(int yearlyImportQuantity) {
        this.yearlyImportQuantity = Math.max(yearlyImportQuantity, 0);
    }

    public int getYearlyImportCount() {
        return yearlyImportCount;
    }

    public void setYearlyImportCount(int yearlyImportCount) {
        this.yearlyImportCount = Math.max(yearlyImportCount, 0);
    }

    public int getYearlyProductCount() {
        return yearlyProductCount;
    }

    public void setYearlyProductCount(int yearlyProductCount) {
        this.yearlyProductCount = Math.max(yearlyProductCount, 0);
    }

    public int getTotalImportQuantity() {
        return yearlyImportQuantity;
    }

    public int getTotalImportCount() {
        return yearlyImportCount;
    }

    public int getTotalProductCount() {
        return yearlyProductCount;
    }

    public String getSelectedPeriodText() {
        return String.format("%02d/%d", selectedMonth, selectedYear);
    }

    public String getSelectedMonthText() {
        return "Tháng " + selectedMonth;
    }

    public String getSelectedYearText() {
        return "Năm " + selectedYear;
    }

    public String getMonthlyImportQuantityText() {
        return String.valueOf(Math.max(monthlyImportQuantity, 0));
    }

    public String getMonthlyImportCountText() {
        return String.valueOf(Math.max(monthlyImportCount, 0));
    }

    public String getMonthlyProductCountText() {
        return String.valueOf(Math.max(monthlyProductCount, 0));
    }

    public String getYearlyImportQuantityText() {
        return String.valueOf(Math.max(yearlyImportQuantity, 0));
    }

    public String getYearlyImportCountText() {
        return String.valueOf(Math.max(yearlyImportCount, 0));
    }

    public String getYearlyProductCountText() {
        return String.valueOf(Math.max(yearlyProductCount, 0));
    }

    public boolean hasMonthlyImport() {
        return monthlyImportQuantity > 0 || monthlyImportCount > 0 || monthlyProductCount > 0;
    }

    public boolean getHasMonthlyImport() {
        return hasMonthlyImport();
    }

    public boolean hasYearlyImport() {
        return yearlyImportQuantity > 0 || yearlyImportCount > 0 || yearlyProductCount > 0;
    }

    public boolean getHasYearlyImport() {
        return hasYearlyImport();
    }

    public String getMonthlySummaryText() {
        if (!hasMonthlyImport()) {
            return "Chưa có nhập hàng trong " + getSelectedPeriodText();
        }

        return "Đã nhập "
                + monthlyImportQuantity
                + " sản phẩm trong "
                + monthlyImportCount
                + " lượt nhập của "
                + monthlyProductCount
                + " mặt hàng.";
    }

    public String getYearlySummaryText() {
        if (!hasYearlyImport()) {
            return "Chưa có nhập hàng trong năm " + selectedYear;
        }

        return "Năm "
                + selectedYear
                + " đã nhập "
                + yearlyImportQuantity
                + " sản phẩm trong "
                + yearlyImportCount
                + " lượt nhập.";
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
        return "InventoryImportSummary{" +
                "selectedMonth=" + selectedMonth +
                ", selectedYear=" + selectedYear +
                ", monthlyImportQuantity=" + monthlyImportQuantity +
                ", monthlyImportCount=" + monthlyImportCount +
                ", monthlyProductCount=" + monthlyProductCount +
                ", yearlyImportQuantity=" + yearlyImportQuantity +
                ", yearlyImportCount=" + yearlyImportCount +
                ", yearlyProductCount=" + yearlyProductCount +
                '}';
    }
}