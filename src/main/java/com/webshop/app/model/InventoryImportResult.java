package com.webshop.app.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class InventoryImportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int rowNumber;
    private int productId;
    private String productTitle;

    private Integer beforeStock;
    private int importQuantity;
    private Integer afterStock;

    private boolean success;
    private String statusMessage;
    private String note;

    private Timestamp processedAt;

    public InventoryImportResult() {
        this.processedAt = new Timestamp(System.currentTimeMillis());
    }

    public InventoryImportResult(
            int rowNumber,
            int productId,
            String productTitle,
            Integer beforeStock,
            int importQuantity,
            Integer afterStock,
            boolean success,
            String statusMessage,
            String note
    ) {
        this.rowNumber = Math.max(rowNumber, 0);
        this.productId = Math.max(productId, 0);
        this.productTitle = normalizeText(productTitle);
        this.beforeStock = beforeStock;
        this.importQuantity = Math.max(importQuantity, 0);
        this.afterStock = afterStock;
        this.success = success;
        this.statusMessage = normalizeText(statusMessage);
        this.note = normalizeText(note);
        this.processedAt = new Timestamp(System.currentTimeMillis());
    }

    public static InventoryImportResult success(
            int rowNumber,
            int productId,
            String productTitle,
            int beforeStock,
            int importQuantity,
            int afterStock,
            String note
    ) {
        return new InventoryImportResult(
                rowNumber,
                productId,
                productTitle,
                beforeStock,
                importQuantity,
                afterStock,
                true,
                "Thành công",
                note
        );
    }

    public static InventoryImportResult error(
            int rowNumber,
            int productId,
            String productTitle,
            Integer beforeStock,
            int importQuantity,
            Integer afterStock,
            String statusMessage,
            String note
    ) {
        return new InventoryImportResult(
                rowNumber,
                productId,
                productTitle,
                beforeStock,
                Math.max(importQuantity, 0),
                afterStock,
                false,
                statusMessage,
                note
        );
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

    public Integer getBeforeStock() {
        return beforeStock;
    }

    public String getBeforeStockText() {
        return beforeStock == null ? "-" : String.valueOf(Math.max(beforeStock, 0));
    }

    public void setBeforeStock(Integer beforeStock) {
        this.beforeStock = beforeStock;
    }

    public int getImportQuantity() {
        return importQuantity;
    }

    public void setImportQuantity(int importQuantity) {
        this.importQuantity = Math.max(importQuantity, 0);
    }

    public Integer getAfterStock() {
        return afterStock;
    }

    public String getAfterStockText() {
        return afterStock == null ? "-" : String.valueOf(Math.max(afterStock, 0));
    }

    public void setAfterStock(Integer afterStock) {
        this.afterStock = afterStock;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isError() {
        return !success;
    }

    public boolean getError() {
        return isError();
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getDisplayStatusMessage() {
        if (statusMessage == null || statusMessage.isBlank()) {
            return success ? "Thành công" : "Có lỗi xảy ra";
        }

        return statusMessage.trim();
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = normalizeText(statusMessage);
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

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    public String getFormattedProcessedAt() {
        if (processedAt == null) {
            return "";
        }

        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(processedAt);
    }

    public String getStatusLabel() {
        return success ? "Thành công" : "Lỗi";
    }

    public String getStatusClass() {
        return success ? "import-success" : "import-error";
    }

    public String getQuantityText() {
        return "+" + Math.max(importQuantity, 0);
    }

    public String getStockChangeText() {
        return getBeforeStockText() + " → " + getAfterStockText();
    }

    public boolean hasStockSnapshot() {
        return beforeStock != null || afterStock != null;
    }

    public boolean isValidResult() {
        return productId > 0 && importQuantity > 0 && success;
    }

    public boolean getValidResult() {
        return isValidResult();
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
        return "InventoryImportResult{" +
                "rowNumber=" + rowNumber +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", beforeStock=" + beforeStock +
                ", importQuantity=" + importQuantity +
                ", afterStock=" + afterStock +
                ", success=" + success +
                ", statusMessage='" + statusMessage + '\'' +
                ", note='" + note + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}