package com.webshop.app.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class InventoryImportHistoryView implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int productId;

    private String productTitle;
    private String categoryName;
    private String brandName;

    private int quantity;
    private Integer beforeStock;
    private Integer afterStock;

    private String referenceType;
    private String note;
    private String createdByName;
    private Timestamp createdAt;

    public InventoryImportHistoryView() {
    }

    public InventoryImportHistoryView(
            int id,
            int productId,
            String productTitle,
            String categoryName,
            String brandName,
            int quantity,
            Integer beforeStock,
            Integer afterStock,
            String referenceType,
            String note,
            String createdByName,
            Timestamp createdAt
    ) {
        this.id = Math.max(id, 0);
        this.productId = Math.max(productId, 0);
        this.productTitle = normalizeText(productTitle);
        this.categoryName = normalizeText(categoryName);
        this.brandName = normalizeText(brandName);
        this.quantity = Math.max(quantity, 0);
        this.beforeStock = beforeStock;
        this.afterStock = afterStock;
        this.referenceType = normalizeText(referenceType);
        this.note = normalizeText(note);
        this.createdByName = normalizeText(createdByName);
        this.createdAt = createdAt;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public String getQuantityText() {
        return "+" + Math.max(quantity, 0);
    }

    public Integer getBeforeStock() {
        return beforeStock;
    }

    public String getBeforeStockText() {
        if (beforeStock == null) {
            return "-";
        }

        return String.valueOf(Math.max(beforeStock, 0));
    }

    public void setBeforeStock(Integer beforeStock) {
        this.beforeStock = beforeStock;
    }

    public Integer getAfterStock() {
        return afterStock;
    }

    public String getAfterStockText() {
        if (afterStock == null) {
            return "-";
        }

        return String.valueOf(Math.max(afterStock, 0));
    }

    public void setAfterStock(Integer afterStock) {
        this.afterStock = afterStock;
    }

    public String getStockChangeText() {
        return getBeforeStockText() + " → " + getAfterStockText();
    }

    public boolean hasStockSnapshot() {
        return beforeStock != null || afterStock != null;
    }

    public boolean getHasStockSnapshot() {
        return hasStockSnapshot();
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getDisplayReferenceType() {
        if (referenceType == null || referenceType.isBlank()) {
            return "Không xác định";
        }

        return referenceType.trim();
    }

    public String getReferenceTypeLabel() {
        if (referenceType == null || referenceType.isBlank()) {
            return "Không xác định";
        }

        return switch (referenceType.trim().toUpperCase()) {
            case "MANUAL" -> "Nhập thủ công";
            case "EXCEL_IMPORT" -> "Nhập từ Excel";
            case "RETURN" -> "Hoàn kho";
            case "ADJUSTMENT" -> "Điều chỉnh";
            default -> referenceType.trim();
        };
    }

    public String getReferenceTypeClass() {
        if (referenceType == null || referenceType.isBlank()) {
            return "import-method-other";
        }

        return switch (referenceType.trim().toUpperCase()) {
            case "MANUAL" -> "import-method-manual";
            case "EXCEL_IMPORT" -> "import-method-excel";
            case "RETURN" -> "import-method-return";
            case "ADJUSTMENT" -> "import-method-adjust";
            default -> "import-method-other";
        };
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = normalizeText(referenceType);
    }

    public String getNote() {
        return note;
    }

    public String getDisplayNote() {
        if (note == null || note.isBlank()) {
            return "Không có ghi chú";
        }

        return note.trim();
    }

    public void setNote(String note) {
        this.note = normalizeText(note);
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getDisplayCreatedByName() {
        if (createdByName == null || createdByName.isBlank()) {
            return "Admin";
        }

        return createdByName.trim();
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = normalizeText(createdByName);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "";
        }

        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
    }

    public String getCreatedDateText() {
        if (createdAt == null) {
            return "";
        }

        return new SimpleDateFormat("dd/MM/yyyy").format(createdAt);
    }

    public String getCreatedTimeText() {
        if (createdAt == null) {
            return "";
        }

        return new SimpleDateFormat("HH:mm").format(createdAt);
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isManualImport() {
        return "MANUAL".equalsIgnoreCase(getDisplayReferenceType());
    }

    public boolean getManualImport() {
        return isManualImport();
    }

    public boolean isExcelImport() {
        return "EXCEL_IMPORT".equalsIgnoreCase(getDisplayReferenceType());
    }

    public boolean getExcelImport() {
        return isExcelImport();
    }

    public boolean isReturnImport() {
        return "RETURN".equalsIgnoreCase(getDisplayReferenceType());
    }

    public boolean getReturnImport() {
        return isReturnImport();
    }

    public boolean isValidHistoryRow() {
        return productId > 0 && quantity > 0;
    }

    public boolean getValidHistoryRow() {
        return isValidHistoryRow();
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
        return "InventoryImportHistoryView{" +
                "id=" + id +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", quantity=" + quantity +
                ", beforeStock=" + beforeStock +
                ", afterStock=" + afterStock +
                ", referenceType='" + referenceType + '\'' +
                ", note='" + note + '\'' +
                ", createdByName='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}