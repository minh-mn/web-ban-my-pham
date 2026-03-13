package com.mycosmeticshop.model;

import java.time.LocalDateTime;

public class PasswordResetToken {

    private int id;
    private int userId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean used;

    /**
     * Token hợp lệ khi:
     * - chưa bị dùng (used = false)
     * - chưa hết hạn (now < expiresAt)
     */
    public boolean isValid(LocalDateTime now) {
        if (used) return false;
        if (now == null || expiresAt == null) return false;
        return now.isBefore(expiresAt);
    }

    // ===== GETTER / SETTER =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
