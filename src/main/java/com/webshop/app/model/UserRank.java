package com.webshop.app.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class UserRank {

    private Long id;
    private String code;
    private String name;
    private BigDecimal minSpent;
    private int discountPercent;
    private String cssClass;
    private boolean active;
    private Timestamp createdAt;

    public UserRank() {
        this.code = "MEMBER";
        this.name = "Thành viên";
        this.minSpent = BigDecimal.ZERO;
        this.discountPercent = 0;
        this.cssClass = "rank-member";
        this.active = true;
    }

    public UserRank(String code,
                    String name,
                    BigDecimal minSpent,
                    int discountPercent,
                    String cssClass) {
        this.code = normalizeCode(code);
        this.name = safeText(name, "Thành viên");
        this.minSpent = normalizeMoney(minSpent);
        this.discountPercent = Math.max(0, discountPercent);
        this.cssClass = safeText(cssClass, "rank-member");
        this.active = true;
    }

    /* ================= BASIC GETTER / SETTER ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = normalizeCode(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = safeText(name, "Thành viên");
    }

    public BigDecimal getMinSpent() {
        return normalizeMoney(minSpent);
    }

    public void setMinSpent(BigDecimal minSpent) {
        this.minSpent = normalizeMoney(minSpent);
    }

    public int getDiscountPercent() {
        return Math.max(0, discountPercent);
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = Math.max(0, discountPercent);
    }

    public String getCssClass() {
        if (cssClass == null || cssClass.isBlank()) {
            return "rank-member";
        }

        return cssClass.trim();
    }

    public void setCssClass(String cssClass) {
        this.cssClass = safeText(cssClass, "rank-member");
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

    /* ================= VIEW SUPPORT ================= */

    public String getDisplayName() {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }

        if (code != null && !code.isBlank()) {
            return code.trim();
        }

        return "Thành viên";
    }

    public String getDiscountLabel() {
        int percent = getDiscountPercent();

        if (percent <= 0) {
            return "Không ưu đãi";
        }

        return "Giảm " + percent + "%";
    }

    public boolean hasDiscount() {
        return getDiscountPercent() > 0;
    }

    public boolean isDefaultRank() {
        return "MEMBER".equalsIgnoreCase(getCode());
    }

    public String getMinSpentLabel() {
        return getMinSpent().toPlainString();
    }

    /* ================= HELPER ================= */

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "MEMBER";
        }

        return value.trim().toUpperCase();
    }

    private String safeText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value;
    }
}