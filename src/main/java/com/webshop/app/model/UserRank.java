package com.webshop.app.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

public class UserRank {

    private Long id;
    private String code;
    private String name;
    private BigDecimal minSpent;
    private Integer discountPercent;
    private String cssClass;
    private Boolean active;
    private Timestamp createdAt;

    public UserRank() {
    }

    public UserRank(Long id,
                    String code,
                    String name,
                    BigDecimal minSpent,
                    Integer discountPercent,
                    String cssClass,
                    Boolean active,
                    Timestamp createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.minSpent = minSpent;
        this.discountPercent = discountPercent;
        this.cssClass = cssClass;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UserRank(String code,
                    String name,
                    BigDecimal minSpent,
                    Integer discountPercent,
                    String cssClass) {
        this.code = code;
        this.name = name;
        this.minSpent = minSpent;
        this.discountPercent = discountPercent;
        this.cssClass = cssClass;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = normalizeString(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeString(name);
    }

    public BigDecimal getMinSpent() {
        return safeMoney(minSpent);
    }

    public void setMinSpent(BigDecimal minSpent) {
        this.minSpent = safeMoney(minSpent);
    }

    public Integer getDiscountPercent() {
        return discountPercent == null ? 0 : discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        if (discountPercent == null) {
            this.discountPercent = 0;
            return;
        }

        if (discountPercent < 0) {
            this.discountPercent = 0;
            return;
        }

        if (discountPercent > 100) {
            this.discountPercent = 100;
            return;
        }

        this.discountPercent = discountPercent;
    }

    public String getCssClass() {
        return cssClass == null || cssClass.isBlank() ? "rank-member" : cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = normalizeString(cssClass);
    }

    public Boolean getActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active != null && active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasDiscount() {
        return getDiscountPercent() > 0;
    }

    public String getDisplayName() {
        if (name == null || name.isBlank()) {
            return "Thành viên";
        }

        return name;
    }

    public String getDiscountLabel() {
        if (!hasDiscount()) {
            return "Không có ưu đãi";
        }

        return getDiscountPercent() + "%";
    }

    public BigDecimal calculateDiscountAmount(BigDecimal amount) {
        BigDecimal safeAmount = safeMoney(amount);

        if (safeAmount.compareTo(BigDecimal.ZERO) <= 0 || getDiscountPercent() <= 0) {
            return BigDecimal.ZERO;
        }

        return safeAmount
                .multiply(BigDecimal.valueOf(getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private static BigDecimal safeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
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
        return "UserRank{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", minSpent=" + minSpent +
                ", discountPercent=" + discountPercent +
                ", cssClass='" + cssClass + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}