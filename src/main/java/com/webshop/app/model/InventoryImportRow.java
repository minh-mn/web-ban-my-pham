package com.webshop.app.model;

import java.io.Serializable;

public class InventoryImportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private int rowNumber;
    private int productId;
    private String productTitle;
    private String categoryName;
    private String brandName;
    private int currentStock;
    private int importQuantity;
    private String note;

    public InventoryImportRow() {
    }

    public InventoryImportRow(int productId, int importQuantity, String note) {
        this.productId = Math.max(productId, 0);
        this.importQuantity = Math.max(importQuantity, 0);
        this.note = normalizeText(note);
    }

    public InventoryImportRow(
            int rowNumber,
            int productId,
            String productTitle,
            String categoryName,
            String brandName,
            int currentStock,
            int importQuantity,
            String note
    ) {
        this.rowNumber = Math.max(rowNumber, 0);
        this.productId = Math.max(productId, 0);
        this.productTitle = normalizeText(productTitle);
        this.categoryName = normalizeText(categoryName);
        this.brandName = normalizeText(brandName);
        this.currentStock = Math.max(currentStock, 0);
        this.importQuantity = Math.max(importQuantity, 0);
        this.note = normalizeText(note);
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = Math.max(rowNumber, 0);
    }

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
        if (productTitle == null || productTitle.isBlank()) {
            if (productId > 0) {
                return "Sản phẩm #" + productId;
            }

            return "Chưa xác định";
        }

        return productTitle.trim();
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = normalizeText(productTitle);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDisplayCategoryName() {
        if (categoryName == null || categoryName.isBlank()) {
            return "Chưa phân loại";
        }

        return categoryName.trim();
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = normalizeText(categoryName);
    }

    public String getBrandName() {
        return brandName;
    }

    public String getDisplayBrandName() {
        if (brandName == null || brandName.isBlank()) {
            return "Chưa có thương hiệu";
        }

        return brandName.trim();
    }

    public void setBrandName(String brandName) {
        this.brandName = normalizeText(brandName);
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = Math.max(currentStock, 0);
    }

    public int getImportQuantity() {
        return importQuantity;
    }

    public void setImportQuantity(int importQuantity) {
        this.importQuantity = Math.max(importQuantity, 0);
    }

    public String getNote() {
        return note;
    }

    public String getDisplayNote() {
        if (note == null || note.isBlank()) {
            return "";
        }

        return note.trim();
    }

    public void setNote(String note) {
        this.note = normalizeText(note);
    }

    public int getAfterStock() {
        return currentStock + importQuantity;
    }

    public boolean isValid() {
        return productId > 0 && importQuantity > 0;
    }

    public boolean getValid() {
        return isValid();
    }

    public String getValidationMessage() {
        if (productId <= 0) {
            return "Mã sản phẩm không hợp lệ.";
        }

        if (importQuantity <= 0) {
            return "Số lượng nhập phải lớn hơn 0.";
        }

        return "Hợp lệ";
    }

    public String getExcelStatusText() {
        return isValid() ? "Sẵn sàng nhập kho" : getValidationMessage();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String toString() {
        return "InventoryImportRow{" +
                "rowNumber=" + rowNumber +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", currentStock=" + currentStock +
                ", importQuantity=" + importQuantity +
                ", note='" + note + '\'' +
                '}';
    }
}