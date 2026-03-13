package com.mycosmeticshop.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Review {

    // ===== CORE FIELDS =====
    private int id;
    private int productId;
    private int authorId;
    private String authorName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    // ===== AI FIELDS =====
    private boolean hasEmoji;
    private int sentiment; // 1 = positive, 0 = negative

    // ================= GETTER / SETTER =================

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getAuthorId() {
        return authorId;
    }
    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
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
    public void setHasEmoji(boolean hasEmoji) {
        this.hasEmoji = hasEmoji;
    }

    public int getSentiment() {
        return sentiment;
    }
    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    // ==================================================
    // ⭐ HELPER CHO JSP (QUAN TRỌNG – FIX LỖI 500)
    // JSP: <fmt:formatDate value="${r.createdAtDate}" />
    // ==================================================
    public Date getCreatedAtDate() {
        if (this.createdAt == null) return null;

        return Date.from(
            this.createdAt
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
    }
}
