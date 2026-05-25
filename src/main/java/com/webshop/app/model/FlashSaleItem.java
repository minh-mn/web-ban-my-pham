package com.webshop.app.model;

import com.webshop.app.model.Product;

public class FlashSaleItem {

    private int id;

    private int flashSaleId;

    private Product product; // map từ store_product

    private double flashPrice;

    private int quantity;

    private int soldQuantity;

    // ===== helper =====

    public int getRemainQuantity() {
        return quantity - soldQuantity;
    }

    public int getSoldPercent() {
        if (quantity == 0) return 0;
        return (soldQuantity * 100) / quantity;
    }

    // getter setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(int flashSaleId) {
        this.flashSaleId = flashSaleId;
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
        this.flashPrice = flashPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
}