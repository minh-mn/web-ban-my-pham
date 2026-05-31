package com.webshop.app.model;

import java.sql.Timestamp;
import java.util.Date;

public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

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
     * MEMBER/SILVER/GOLD/DIAMOND/VIP... => dùng rank do admin chọn.
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

    private String birthDate;  // yyyy-MM-dd
    private String gender;

    public User() {
        this.role = ROLE_USER;
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
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }
        return role.toUpperCase();
    }

    public void setRole(String role) {
        String value = normalizeString(role);
        if (value == null) {
            this.role = ROLE_USER;
            return;
        }

        value = value.toUpperCase();
        if (!ROLE_ADMIN.equals(value) && !ROLE_USER.equals(value)) {
            this.role = ROLE_USER;
            return;
        }

        this.role = value;
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

    public Date getCreatedAtDate() {
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = normalizeString(birthDate);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        String value = normalizeString(gender);
        this.gender = value == null ? null : value.toUpperCase();
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

    /* ================= ROLE / PERMISSION HELPERS ================= */

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public boolean getAdmin() {
        return isAdmin();
    }

    public boolean isUser() {
        return ROLE_USER.equalsIgnoreCase(getRole());
    }

    public boolean getUser() {
        return isUser();
    }

    public boolean isEnabled() {
        return active;
    }

    public boolean isLocked() {
        return !active;
    }

    public boolean getLocked() {
        return isLocked();
    }

    public String getRoleLabel() {
        if (isAdmin()) {
            return "Quản trị viên";
        }
        return "Khách hàng";
    }

    public String getRoleCssClass() {
        if (isAdmin()) {
            return "admin-pill--danger";
        }
        return "admin-pill--info";
    }

    public String getStatusLabel() {
        return active ? "Đang hoạt động" : "Đã khóa";
    }

    public String getStatusCssClass() {
        return active ? "admin-pill--ok" : "admin-pill--danger";
    }

    /*
     * Dùng cho issue 130:
     * Tài khoản ADMIN nên được bảo vệ, không cho admin khác khóa/xóa/sửa quyền tùy ý.
     */
    public boolean isProtectedAdminAccount() {
        return isAdmin();
    }

    public boolean getProtectedAdminAccount() {
        return isProtectedAdminAccount();
    }

    public boolean isNormalUserAccount() {
        return isUser();
    }

    public boolean getNormalUserAccount() {
        return isNormalUserAccount();
    }

    /* ================= DISPLAY HELPERS ================= */

    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (username != null && !username.isBlank()) {
            return username;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return id > 0 ? "User #" + id : "Người dùng";
    }

    public String getDisplayUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return id > 0 ? "user_" + id : "unknown";
    }

    public String getDisplayEmail() {
        return email != null && !email.isBlank() ? email : "Chưa cập nhật";
    }

    public String getDisplayPhone() {
        return phone != null && !phone.isBlank() ? phone : "Chưa cập nhật";
    }

    public String getDisplayFullName() {
        return fullName != null && !fullName.isBlank() ? fullName : "Chưa cập nhật";
    }

    public String getInitials() {
        String source = getDisplayName();
        if (source == null || source.isBlank()) {
            return "U";
        }

        String[] parts = source.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(parts[0].length(), 1)).toUpperCase();
        }

        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);

        return (first + last).toUpperCase();
    }

    public String getGenderLabel() {
        if (gender == null || gender.isBlank()) {
            return "Chưa cập nhật";
        }

        return switch (gender.toUpperCase()) {
            case "MALE", "NAM" -> "Nam";
            case "FEMALE", "NU", "NỮ" -> "Nữ";
            case "OTHER", "KHAC", "KHÁC" -> "Khác";
            default -> gender;
        };
    }

    /* ================= RANK HELPERS ================= */

    public boolean hasManualRank() {
        return manualRankCode != null && !manualRankCode.isBlank();
    }

    public boolean getHasManualRank() {
        return hasManualRank();
    }

    public boolean isAutoRank() {
        return !hasManualRank();
    }

    public boolean getAutoRank() {
        return isAutoRank();
    }

    /*
     * Chế độ xét rank:
     * - AUTO: hệ thống tự tính theo tổng chi tiêu.
     * - MANUAL: admin chỉ định trực tiếp.
     */
    public String getRankModeLabel() {
        return isAutoRank() ? "AUTO" : "MANUAL";
    }

    public String getRankModeText() {
        return isAutoRank() ? "Tự động theo chi tiêu" : "Admin chỉ định";
    }

    public String getRankModeCssClass() {
        return isAutoRank() ? "admin-pill--warning" : "admin-pill--info";
    }

    /*
     * Rank thật sự để hiển thị ở danh sách user.
     * Không trả về AUTO.
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

    public String getRankDisplay() {
        String code = getDisplayRankCode();
        String name = getDisplayRankName();

        if (code == null || code.isBlank()) {
            return name;
        }

        return name + " (" + code + ")";
    }

    public String getManualRankDisplay() {
        if (manualRankCode == null || manualRankCode.isBlank()) {
            return "AUTO";
        }
        return manualRankCode;
    }

    /* ================= PASSWORD / SOCIAL HELPERS ================= */

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public boolean getHasPassword() {
        return hasPassword();
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

    public String getPasswordStatusCssClass() {
        return hasPassword() ? "admin-pill--ok" : "admin-pill--warning";
    }

    public boolean hasSocialLogin() {
        return hasGoogleLogin() || hasFacebookLogin();
    }

    public boolean getHasSocialLogin() {
        return hasSocialLogin();
    }

    public boolean hasGoogleLogin() {
        return googleId != null && !googleId.isBlank();
    }

    public boolean getHasGoogleLogin() {
        return hasGoogleLogin();
    }

    public boolean hasFacebookLogin() {
        return facebookId != null && !facebookId.isBlank();
    }

    public boolean getHasFacebookLogin() {
        return hasFacebookLogin();
    }

    public String getLoginProviderLabel() {
        if (hasGoogleLogin() && hasFacebookLogin()) {
            return "Google + Facebook";
        }
        if (hasGoogleLogin()) {
            return "Google";
        }
        if (hasFacebookLogin()) {
            return "Facebook";
        }
        if (hasPassword()) {
            return "Tài khoản thường";
        }
        return "Chưa xác định";
    }

    /* ================= NORMALIZE ================= */

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
                ", birthDate='" + birthDate + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
