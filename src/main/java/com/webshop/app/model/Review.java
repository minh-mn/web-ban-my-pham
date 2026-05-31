package com.webshop.app.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Review {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private int id;
    private int productId;
    private String productCode;
    private int authorId;
    private Integer orderId;
    private Integer orderItemId;

    private String authorName;
    private String authorFullName;
    private String authorEmail;
    private String authorPhone;
    private String authorRole;
    private String authorRankCode;
    private String authorRankName;
    private Boolean authorActive;
    private LocalDateTime authorCreatedAt;

    private String productName;
    private String productSlug;
    private String productImage;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    private boolean hasEmoji;
    private int sentiment; // 1 = positive, 0 = negative

    private String status = STATUS_PENDING;
    private boolean hidden;
    private String adminNote;
    private LocalDateTime approvedAt;
    private Integer approvedBy;
    private boolean voucherAwarded;

    private int mediaCount;
    private boolean hasImage;
    private boolean hasVideo;
    private String imageUrl;
    private String videoUrl;

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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = normalize(productCode);
    }

    public String getProductDisplayCode() {
        return productCode != null ? productCode : String.valueOf(productId);
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = Math.max(authorId, 0);
    }

    public int getUserId() {
        return authorId;
    }

    public void setUserId(int userId) {
        setAuthorId(userId);
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId == null || orderId <= 0 ? null : orderId;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId == null || orderItemId <= 0 ? null : orderItemId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = normalize(authorName);
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = normalize(authorFullName);
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = normalize(authorEmail);
    }

    public String getAuthorPhone() {
        return authorPhone;
    }

    public void setAuthorPhone(String authorPhone) {
        this.authorPhone = normalize(authorPhone);
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = normalize(authorRole);
    }

    public String getAuthorRankCode() {
        return authorRankCode;
    }

    public void setAuthorRankCode(String authorRankCode) {
        this.authorRankCode = normalize(authorRankCode);
    }

    public String getAuthorRankName() {
        return authorRankName;
    }

    public void setAuthorRankName(String authorRankName) {
        this.authorRankName = normalize(authorRankName);
    }

    public Boolean getAuthorActive() {
        return authorActive;
    }

    public void setAuthorActive(Boolean authorActive) {
        this.authorActive = authorActive;
    }

    public LocalDateTime getAuthorCreatedAt() {
        return authorCreatedAt;
    }

    public void setAuthorCreatedAt(LocalDateTime authorCreatedAt) {
        this.authorCreatedAt = authorCreatedAt;
    }

    public String getAuthorDisplayName() {
        if (authorFullName != null) {
            return authorFullName;
        }
        if (authorName != null) {
            return authorName;
        }
        return authorId > 0 ? "User #" + authorId : "Khách hàng";
    }

    public String getAuthorRankDisplay() {
        if (authorRankName != null && authorRankCode != null) {
            return authorRankName + " (" + authorRankCode + ")";
        }
        if (authorRankName != null) {
            return authorRankName;
        }
        if (authorRankCode != null) {
            return authorRankCode;
        }
        return "Chưa có hạng";
    }

    public String getAuthorStatusLabel() {
        if (authorActive == null) {
            return "Không rõ";
        }
        return authorActive ? "Đang hoạt động" : "Bị khóa / Ngừng hoạt động";
    }

    public String getAuthorStatusCssClass() {
        if (authorActive == null) {
            return "admin-pill--warning";
        }
        return authorActive ? "admin-pill--ok" : "admin-pill--danger";
    }

    public Date getAuthorCreatedAtDate() {
        return toDate(authorCreatedAt);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = normalize(productName);
    }

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = normalize(productSlug);
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = normalize(productImage);
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) {
            this.rating = 1;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = normalize(comment);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHasEmoji() {
        return hasEmoji;
    }

    public boolean getHasEmoji() {
        return hasEmoji;
    }

    public void setHasEmoji(boolean hasEmoji) {
        this.hasEmoji = hasEmoji;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment <= 0 ? 0 : 1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        String value = normalize(status);
        if (value == null) {
            this.status = STATUS_PENDING;
            return;
        }

        value = value.toUpperCase();
        switch (value) {
            case STATUS_APPROVED:
            case STATUS_REJECTED:
            case STATUS_PENDING:
                this.status = value;
                break;
            default:
                this.status = STATUS_PENDING;
                break;
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = normalize(adminNote);
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy == null || approvedBy <= 0 ? null : approvedBy;
    }

    public boolean isVoucherAwarded() {
        return voucherAwarded;
    }

    public boolean getVoucherAwarded() {
        return voucherAwarded;
    }

    public void setVoucherAwarded(boolean voucherAwarded) {
        this.voucherAwarded = voucherAwarded;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = Math.max(mediaCount, 0);
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public boolean getHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = normalize(imageUrl);
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = normalize(videoUrl);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean getPending() {
        return isPending();
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(status);
    }

    public boolean getApproved() {
        return isApproved();
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(status);
    }

    public boolean getRejected() {
        return isRejected();
    }

    public String getStatusLabel() {
        if (isApproved()) {
            return hidden ? "Đã duyệt nhưng đang ẩn" : "Đã duyệt";
        }
        if (isRejected()) {
            return "Đã từ chối";
        }
        return "Chờ duyệt";
    }

    public String getStatusCssClass() {
        if (isApproved()) {
            return hidden ? "admin-pill--warning" : "admin-pill--ok";
        }
        if (isRejected()) {
            return "admin-pill--danger";
        }
        return "admin-pill--warning";
    }

    public String getSentimentLabel() {
        return sentiment == 1 ? "Tích cực" : "Tiêu cực";
    }

    public Date getCreatedAtDate() {
        return toDate(createdAt);
    }

    public Date getApprovedAtDate() {
        return toDate(approvedAt);
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
