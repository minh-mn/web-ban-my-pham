package com.webshop.app.model;

import java.time.LocalDateTime;

public class ReviewMedia {
    private long id;
    private long reviewId;
    private String mediaType; // IMAGE | VIDEO
    private String mediaUrl;
    private String originalName;
    private long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getReviewId() { return reviewId; }
    public void setReviewId(long reviewId) { this.reviewId = reviewId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isImage() {
        return "IMAGE".equalsIgnoreCase(mediaType);
    }

    public boolean isVideo() {
        return "VIDEO".equalsIgnoreCase(mediaType);
    }
}
