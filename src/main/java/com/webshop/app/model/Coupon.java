package com.webshop.app.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Coupon {

    private static final String DEFAULT_RANK_CODE = "MEMBER";

    private int id;
    private String code;
    private int discountPercent;
    private BigDecimal maxDiscountAmount;

    private int maxUses;
    private int usedCount;

    private boolean active;

    private String type;

    private String description;

    private BigDecimal minOrderAmount;

    // Rank tối thiểu được phép dùng coupon: MEMBER, SILVER, GOLD, DIAMOND, VIP
    private String minRankCode;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private LocalDate startDate;
    private LocalDate endDate;

    /* GET / SET */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim().toUpperCase();
    }


    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }


    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }


    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }


    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public BigDecimal getMinOrderAmount() {
        return minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount == null ? BigDecimal.ZERO : minOrderAmount;
    }


    public String getMinRankCode() {
        if (minRankCode == null || minRankCode.isBlank()) {
            return DEFAULT_RANK_CODE;
        }
        return minRankCode.trim().toUpperCase();
    }

    public void setMinRankCode(String minRankCode) {
        if (minRankCode == null || minRankCode.isBlank()) {
            this.minRankCode = DEFAULT_RANK_CODE;
        } else {
            this.minRankCode = minRankCode.trim().toUpperCase();
        }
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim().toUpperCase();
    }
}