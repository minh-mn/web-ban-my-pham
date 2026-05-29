package com.webshop.app.model;

import java.sql.Timestamp;

public class ProductMedia {

    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VIDEO = "VIDEO";

    private int id;
    private int productId;
    private String mediaUrl;
    private String mediaType;
    private int sortOrder;
    private Timestamp createdAt;

    public ProductMedia() {
    }

    public ProductMedia(int id,
                        int productId,
                        String mediaUrl,
                        String mediaType,
                        int sortOrder,
                        Timestamp createdAt) {
        this.id = Math.max(id, 0);
        this.productId = Math.max(productId, 0);
        this.mediaUrl = normalizeText(mediaUrl);
        this.mediaType = normalizeMediaType(mediaType);
        this.sortOrder = Math.max(sortOrder, 0);
        this.createdAt = createdAt;
    }

    public ProductMedia(int productId,
                        String mediaUrl,
                        String mediaType,
                        int sortOrder) {
        this.productId = Math.max(productId, 0);
        this.mediaUrl = normalizeText(mediaUrl);
        this.mediaType = normalizeMediaType(mediaType);
        this.sortOrder = Math.max(sortOrder, 0);
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
        this.mediaUrl = normalizeText(mediaUrl);
    }

    /*
     * Alias cho JSP nếu muốn gọi:
     * ${media.url}
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
     * Alias cho JSP nếu muốn gọi:
     * ${media.type}
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
        return TYPE_IMAGE.equalsIgnoreCase(getMediaType());
    }

    /*
     * Cho JSP EL:
     * ${media.image}
     */
    public boolean getImage() {
        return isImage();
    }

    public boolean isVideo() {
        return TYPE_VIDEO.equalsIgnoreCase(getMediaType());
    }

    /*
     * Cho JSP EL:
     * ${media.video}
     */
    public boolean getVideo() {
        return isVideo();
    }

    public boolean isValid() {
        return productId > 0
                && mediaUrl != null
                && !mediaUrl.isBlank()
                && (TYPE_IMAGE.equalsIgnoreCase(getMediaType())
                || TYPE_VIDEO.equalsIgnoreCase(getMediaType()));
    }

    /*
     * Cho JSP EL:
     * ${media.valid}
     */
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

    /*
     * Dùng nếu cần kiểm tra URL có thuộc thư mục upload product media không.
     * Không xóa file ở model, chỉ hỗ trợ kiểm tra hiển thị/logic.
     */
    public boolean isProductMediaUploadUrl() {
        return mediaUrl != null
                && mediaUrl.startsWith("/uploads/product/media/");
    }

    public boolean getProductMediaUploadUrl() {
        return isProductMediaUploadUrl();
    }

    private String normalizeMediaType(String value) {
        if (value == null || value.isBlank()) {
            return TYPE_IMAGE;
        }

        String normalized = value.trim().toUpperCase();

        if (TYPE_VIDEO.equals(normalized)) {
            return TYPE_VIDEO;
        }

        return TYPE_IMAGE;
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