package com.webshop.app.model;

import java.time.LocalDateTime;

public class UserPointTransaction {
    private long id;
    private int userId;
    private Long orderId;
    private Long reviewId;
    private int points;
    private String type; // EARN | USE | REFUND | EXPIRE
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
}
