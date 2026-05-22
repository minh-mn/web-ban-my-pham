package com.webshop.app.model;

import java.sql.Timestamp;

public class User {

    private int id;
    private String username;

    // Lưu HASH BCrypt, không phải mật khẩu gốc
    private String password;

    private String fullName;
    private String email;
    private String phone;

    // ADMIN | USER
    private String role;

    private boolean active;
    private Timestamp createdAt;

    private String googleId;
    private String facebookId;

    /*
     * Rank do admin chỉ định trực tiếp.
     *
     * null / blank => AUTO, hệ thống tự tính rank theo tổng chi tiêu.
     * MEMBER/GOLD/VIP... => dùng rank do admin chọn.
     */
    private String manualRankCode;

    /*
     * Rank hiện tại thật sự dùng để hiển thị ở Admin List.
     *
     * Nếu manualRankCode != null:
     * - currentRankCode thường chính là manualRankCode.
     *
     * Nếu manualRankCode == null:
     * - currentRankCode là rank hệ thống tính theo tổng chi tiêu.
     */
    private String currentRankCode;
    private String currentRankName;

    public User() {
        this.role = "USER";
        this.active = true;
        this.manualRankCode = null;
        this.currentRankCode = "MEMBER";
        this.currentRankName = "Thành viên";
    }

    /* ================= GETTER / SETTER ================= */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizeString(username);
    }

    /*
     * Chỉ dùng trong tầng DAO/Auth để kiểm tra BCrypt.
     * Không render password trực tiếp ra JSP.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = normalizeString(password);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = normalizeString(fullName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String value = normalizeString(email);
        this.email = value == null ? null : value.toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalizeString(phone);
    }

    public String getRole() {
        return role == null || role.isBlank() ? "USER" : role;
    }

    public void setRole(String role) {
        String value = normalizeString(role);

        if (value == null) {
            this.role = "USER";
            return;
        }

        this.role = value.toUpperCase();
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = normalizeString(googleId);
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = normalizeString(facebookId);
    }

    public String getManualRankCode() {
        return manualRankCode;
    }

    public void setManualRankCode(String manualRankCode) {
        String value = normalizeString(manualRankCode);

        if (value == null || "AUTO".equalsIgnoreCase(value)) {
            this.manualRankCode = null;
            return;
        }

        this.manualRankCode = value.toUpperCase();
    }

    public String getCurrentRankCode() {
        return currentRankCode;
    }

    public void setCurrentRankCode(String currentRankCode) {
        String value = normalizeString(currentRankCode);

        if (value == null) {
            this.currentRankCode = "MEMBER";
            return;
        }

        this.currentRankCode = value.toUpperCase();
    }

    public String getCurrentRankName() {
        return currentRankName;
    }

    public void setCurrentRankName(String currentRankName) {
        String value = normalizeString(currentRankName);

        if (value == null) {
            this.currentRankName = "Thành viên";
            return;
        }

        this.currentRankName = value;
    }

    /* ================= BUSINESS LOGIC ================= */

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(getRole());
    }

    public boolean isEnabled() {
        return active;
    }

    public boolean hasManualRank() {
        return manualRankCode != null && !manualRankCode.isBlank();
    }

    public boolean isAutoRank() {
        return !hasManualRank();
    }

    /*
     * Đây là chế độ xét rank:
     * - AUTO: hệ thống tự tính theo tổng chi tiêu.
     * - MANUAL: admin chỉ định trực tiếp.
     */
    public String getRankModeLabel() {
        return isAutoRank() ? "AUTO" : "MANUAL";
    }

    /*
     * Đây là rank thật sự để hiển thị ở danh sách user.
     * Không trả về AUTO nữa.
     */
    public String getDisplayRankCode() {
        if (currentRankCode != null && !currentRankCode.isBlank()) {
            return currentRankCode;
        }

        if (manualRankCode != null && !manualRankCode.isBlank()) {
            return manualRankCode;
        }

        return "MEMBER";
    }

    public String getDisplayRankName() {
        if (currentRankName != null && !currentRankName.isBlank()) {
            return currentRankName;
        }

        return "Thành viên";
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    /*
     * Dùng cho JSP nếu cần hiển thị trạng thái mật khẩu.
     * Không trả về mật khẩu thật hoặc hash đầy đủ.
     */
    public String getPasswordMasked() {
        if (!hasPassword()) {
            return "Chưa có mật khẩu";
        }

        return "••••••••";
    }

    public String getPasswordStatusLabel() {
        if (!hasPassword()) {
            return "Chưa thiết lập";
        }

        return "Đã mã hóa";
    }

    public boolean hasSocialLogin() {
        return hasGoogleLogin() || hasFacebookLogin();
    }

    public boolean hasGoogleLogin() {
        return googleId != null && !googleId.isBlank();
    }

    public boolean hasFacebookLogin() {
        return facebookId != null && !facebookId.isBlank();
    }

    private static String normalizeString(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", googleId='" + googleId + '\'' +
                ", facebookId='" + facebookId + '\'' +
                ", manualRankCode='" + manualRankCode + '\'' +
                ", currentRankCode='" + currentRankCode + '\'' +
                ", currentRankName='" + currentRankName + '\'' +
                '}';
    }
}