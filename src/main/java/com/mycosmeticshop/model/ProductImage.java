package com.mycosmeticshop.model;

public class ProductImage {

    private long id;          // PK (bigint)
    private String image;     // varchar(100) - đường dẫn ảnh
    private int order;        // thứ tự hiển thị
    private int productId;    // FK -> product.id

    // ================= GETTER / SETTER =================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /** Đường dẫn ảnh lưu trong DB */
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /** Thứ tự hiển thị ảnh (order ASC) */
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    /* ================= HELPER FOR JSP ================= */

    /** ✅ JSP: ${img.imageUrl} */
    public String getImageUrl() {
        return image;
    }
}
