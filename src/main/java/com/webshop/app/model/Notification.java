package com.webshop.app.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Notification {

    private long id;
    private int userId;
    private Long orderId;
    private String type;
    private String title;
    private String message;
    private String targetUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = Math.max(id, 0);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = Math.max(userId, 0);
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId == null || orderId <= 0 ? null : orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = normalizeUpper(type, "SYSTEM");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalizeNullable(title);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = normalizeNullable(message);
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = normalizeNullable(targetUrl);
    }

    public boolean isRead() {
        return read;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Date getCreatedAtDate() {
        return toDate(createdAt);
    }

    public Date getReadAtDate() {
        return toDate(readAt);
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String normalizeUpper(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
