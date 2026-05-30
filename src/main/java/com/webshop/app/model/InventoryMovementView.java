package com.webshop.app.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class InventoryMovementView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private int productId;
    private String productTitle;
    private String movementType;
    private int quantity;
    private Integer beforeStock;
    private Integer afterStock;
    private String note;
    private String createdByName;
    private Timestamp createdAt;

    public InventoryMovementView() {
    }

    public InventoryMovementView(
            Integer id,
            int productId,
            String productTitle,
            String movementType,
            int quantity,
            Integer beforeStock,
            Integer afterStock,
            String note,
            String createdByName,
            Timestamp createdAt
    ) {
        this.id = id;
        this.productId = Math.max(productId, 0);
        this.productTitle = productTitle;
        this.movementType = movementType;
        this.quantity = Math.max(quantity, 0);
        this.beforeStock = beforeStock;
        this.afterStock = afterStock;
        this.note = note;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        if (productTitle == null || productTitle.trim().isEmpty()) {
            return "Sản phẩm #" + productId;
        }

        return productTitle.trim();
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getMovementType() {
        return movementType;
    }

    public String getNormalizedMovementType() {
        if (movementType == null || movementType.trim().isEmpty()) {
            return "";
        }

        return movementType.trim().toUpperCase();
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public Integer getBeforeStock() {
        return beforeStock;
    }

    public void setBeforeStock(Integer beforeStock) {
        this.beforeStock = beforeStock;
    }

    public Integer getAfterStock() {
        return afterStock;
    }

    public void setAfterStock(Integer afterStock) {
        this.afterStock = afterStock;
    }

    public String getNote() {
        return note;
    }

    public String getDisplayNote() {
        if (note == null || note.trim().isEmpty()) {
            return "Không có ghi chú";
        }

        return note.trim();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getDisplayCreatedByName() {
        if (createdByName == null || createdByName.trim().isEmpty()) {
            return "Hệ thống";
        }

        return createdByName.trim();
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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

    public String getFormattedDate() {
        if (createdAt == null) {
            return "";
        }

        return new SimpleDateFormat("dd/MM/yyyy").format(createdAt);
    }

    public String getFormattedTime() {
        if (createdAt == null) {
            return "";
        }

        return new SimpleDateFormat("HH:mm").format(createdAt);
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isImportMovement() {
        return "IN".equals(getNormalizedMovementType());
    }

    public boolean isExportMovement() {
        return "OUT".equals(getNormalizedMovementType());
    }

    public boolean isAdjustmentMovement() {
        return "ADJUST".equals(getNormalizedMovementType())
                || "ADJUSTMENT".equals(getNormalizedMovementType());
    }

    public boolean isReturnMovement() {
        return "RETURN".equals(getNormalizedMovementType());
    }

    public String getMovementTypeLabel() {
        String type = getNormalizedMovementType();

        return switch (type) {
            case "IN" -> "Nhập kho";
            case "OUT" -> "Xuất kho";
            case "ADJUST", "ADJUSTMENT" -> "Điều chỉnh";
            case "RETURN" -> "Hoàn kho";
            default -> "Khác";
        };
    }

    public String getMovementTypeClass() {
        String type = getNormalizedMovementType();

        return switch (type) {
            case "IN", "RETURN" -> "movement-in";
            case "OUT" -> "movement-out";
            case "ADJUST", "ADJUSTMENT" -> "movement-adjust";
            default -> "movement-other";
        };
    }

    public String getQuantityText() {
        if (isImportMovement() || isReturnMovement()) {
            return "+" + quantity;
        }

        if (isExportMovement()) {
            return "-" + quantity;
        }

        return String.valueOf(quantity);
    }

    public String getStockChangeText() {
        if (beforeStock == null && afterStock == null) {
            return "Không có dữ liệu";
        }

        String before = beforeStock == null ? "?" : String.valueOf(beforeStock);
        String after = afterStock == null ? "?" : String.valueOf(afterStock);

        return before + " → " + after;
    }

    public String getBeforeStockText() {
        return beforeStock == null ? "-" : String.valueOf(beforeStock);
    }

    public String getAfterStockText() {
        return afterStock == null ? "-" : String.valueOf(afterStock);
    }

    public boolean hasStockSnapshot() {
        return beforeStock != null || afterStock != null;
    }

    @Override
    public String toString() {
        return "InventoryMovementView{" +
                "id=" + id +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", movementType='" + movementType + '\'' +
                ", quantity=" + quantity +
                ", beforeStock=" + beforeStock +
                ", afterStock=" + afterStock +
                ", note='" + note + '\'' +
                ", createdByName='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}