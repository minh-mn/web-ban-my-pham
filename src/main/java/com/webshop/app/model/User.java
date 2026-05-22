package com.webshop.app.model;

import java.sql.Timestamp;

public class User {

    private int id;
    private String username;
    private String password;   // Lưu hash (BCrypt)
    private String fullName;
    private String email;
    private String phone;
    private String role;       // ADMIN | USER
    private boolean active;
    private Timestamp createdAt;
    private String googleId;
    private String facebookId;
    private String birthDate;  // Thêm trường ngày sinh (String định dạng yyyy-MM-dd)
    private String gender;     // Thêm trường giới tính

    /* ================= GETTER / SETTER ================= */

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getFacebookId() { return facebookId; }
    public void setFacebookId(String facebookId) { this.facebookId = facebookId; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    /* ================= BUSINESS LOGIC ================= */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
