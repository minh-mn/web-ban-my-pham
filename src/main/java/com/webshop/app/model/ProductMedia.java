package com.webshop.app.model;

import java.sql.Timestamp;

public class ProductMedia {

    private int id;
    private int productId;
    private String mediaUrl;
    private String mediaType;
    private int sortOrder;
    private Timestamp createdAt;

    public ProductMedia() {
    }

    public ProductMedia(int id, int productId, String mediaUrl, String mediaType, int sortOrder, Timestamp createdAt) {
        this.id = id;
        this.productId = productId;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public ProductMedia(int productId, String mediaUrl, String mediaType, int sortOrder) {
        this.productId = productId;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.sortOrder = sortOrder;
    }

    // ================= GETTER / SETTER =================

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

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl == null ? null : mediaUrl.trim();
    }

    /*
     * Alias cho JSP nếu muốn gọi media.url
     */
    public String getUrl() {
        return mediaUrl;
    }

    public void setUrl(String url) {
        setMediaUrl(url);
    }

    public String getMediaType() {
        return normalizeMediaType(mediaType);
    }

    public void setMediaType(String mediaType) {
        this.mediaType = normalizeMediaType(mediaType);
    }

    /*
     * Alias cho JSP nếu muốn gọi media.type
     */
    public String getType() {
        return getMediaType();
    }

    public void setType(String type) {
        setMediaType(type);
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = Math.max(sortOrder, 0);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // ================= HELPER FOR JSP =================

    public boolean isImage() {
        return "IMAGE".equalsIgnoreCase(getMediaType());
    }

    public boolean getImage() {
        return isImage();
    }

    public boolean isVideo() {
        return "VIDEO".equalsIgnoreCase(getMediaType());
    }

    public boolean getVideo() {
        return isVideo();
    }

    public boolean isValid() {
        return productId > 0
                && mediaUrl != null
                && !mediaUrl.isBlank()
                && ("IMAGE".equalsIgnoreCase(getMediaType())
                || "VIDEO".equalsIgnoreCase(getMediaType()));
    }

    public boolean getValid() {
        return isValid();
    }

    public String getDisplayTypeLabel() {
        if (isVideo()) {
            return "Video";
        }

        return "Hình ảnh";
    }

    public String getCssClass() {
        if (isVideo()) {
            return "product-media-video";
        }

        return "product-media-image";
    }

    private String normalizeMediaType(String value) {
        if (value == null || value.isBlank()) {
            return "IMAGE";
        }

        String normalized = value.trim().toUpperCase();

        if ("VIDEO".equals(normalized)) {
            return "VIDEO";
        }

        return "IMAGE";
    }

    @Override
    public String toString() {
        return "ProductMedia{" +
                "id=" + id +
                ", productId=" + productId +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + getMediaType() + '\'' +
                ", sortOrder=" + sortOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}