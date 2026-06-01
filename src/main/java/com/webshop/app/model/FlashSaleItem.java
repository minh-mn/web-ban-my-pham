package com.webshop.app.model;

public class FlashSaleItem {

    public static final int DEFAULT_MAX_QUANTITY_PER_USER = 2;

    private int id;

    private int flashSaleId;

    private Product product; // map từ store_product

    private double flashPrice;

    /*
     * Tổng số lượng sản phẩm được phân bổ cho khung Flash Sale.
     */
    private int quantity;

    /*
     * Số lượng đã bán trong khung Flash Sale.
     */
    private int soldQuantity;

    /*
     * Issue 139:
     * Giới hạn số lượng tối đa mỗi khách được mua cho sản phẩm này
     * trong khung giờ Flash Sale.
     */
    private int maxQuantityPerUser = DEFAULT_MAX_QUANTITY_PER_USER;

    // ===== HELPER =====

    public int getRemainQuantity() {
        return Math.max(0, quantity - soldQuantity);
    }

    public int getRemainingQuantity() {
        return getRemainQuantity();
    }

    public boolean isSoldOut() {
        return getRemainQuantity() <= 0;
    }

    public boolean getSoldOut() {
        return isSoldOut();
    }

    public int getSoldPercent() {
        if (quantity <= 0) {
            return 0;
        }

        int percent = (int) Math.round((soldQuantity * 100.0) / quantity);
        return Math.max(0, Math.min(percent, 100));
    }

    public int getSafeMaxQuantityPerUser() {
        return maxQuantityPerUser > 0 ? maxQuantityPerUser : DEFAULT_MAX_QUANTITY_PER_USER;
    }

    public boolean hasPurchaseLimit() {
        return getSafeMaxQuantityPerUser() > 0;
    }

    public boolean getHasPurchaseLimit() {
        return hasPurchaseLimit();
    }

    public String getPurchaseLimitLabel() {
        return "Giới hạn " + getSafeMaxQuantityPerUser() + " sản phẩm / khách";
    }

    public int getProductId() {
        return product == null ? 0 : product.getId();
    }

    // ===== GETTER / SETTER =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public int getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(int flashSaleId) {
        this.flashSaleId = Math.max(flashSaleId, 0);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getFlashPrice() {
        return flashPrice;
    }

    public void setFlashPrice(double flashPrice) {
        this.flashPrice = Math.max(flashPrice, 0);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = Math.max(soldQuantity, 0);
    }

    public int getMaxQuantityPerUser() {
        return getSafeMaxQuantityPerUser();
    }

    public void setMaxQuantityPerUser(int maxQuantityPerUser) {
        this.maxQuantityPerUser = maxQuantityPerUser > 0
                ? maxQuantityPerUser
                : DEFAULT_MAX_QUANTITY_PER_USER;
    }

    @Override
    public String toString() {
        return "FlashSaleItem{" +
                "id=" + id +
                ", flashSaleId=" + flashSaleId +
                ", productId=" + getProductId() +
                ", flashPrice=" + flashPrice +
                ", quantity=" + quantity +
                ", soldQuantity=" + soldQuantity +
                ", maxQuantityPerUser=" + getSafeMaxQuantityPerUser() +
                '}';
    }
}
