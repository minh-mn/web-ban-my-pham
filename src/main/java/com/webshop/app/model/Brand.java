package com.webshop.app.model;

public class Brand {

    private int id;
    private String name;
    private int productCount;
    private String image;

    public Brand() {
    }

    public Brand(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public Brand(int id, String name, String image, int productCount) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.productCount = productCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }


    /*
     * Logo thương hiệu.
     *
     * Database: store_brand.image
     * Giá trị lưu dạng:
     * /uploads/brand/ten-file-logo.png
     */
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /*
     * Alias để tương thích nếu JSP/Servlet dùng imageUrl.
     * Không tạo thêm field mới, vẫn dùng chung biến image.
     */
    public String getImageUrl() {
        return image;
    }

    public void setImageUrl(String imageUrl) {
        this.image = imageUrl;
    }

    public boolean hasImage() {
        return image != null && !image.trim().isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }
}