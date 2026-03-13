package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderDiscount {
	private int id;
	private String name; // <-- thêm

	private BigDecimal minOrderValue;
	private BigDecimal discountPercent;
	private BigDecimal maxDiscountAmount;
	private LocalDate startDate;
	private LocalDate endDate;
	private boolean active;

	public boolean isValid(LocalDate today, BigDecimal subtotal) {
		return active && !today.isBefore(startDate) && !today.isAfter(endDate)
				&& subtotal.compareTo(minOrderValue) >= 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getMinOrderValue() {
		return minOrderValue;
	}

	public void setMinOrderValue(BigDecimal minOrderValue) {
		this.minOrderValue = minOrderValue;
	}

	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}

	public BigDecimal getMaxDiscountAmount() {
		return maxDiscountAmount;
	}

	public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
		this.maxDiscountAmount = maxDiscountAmount;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
