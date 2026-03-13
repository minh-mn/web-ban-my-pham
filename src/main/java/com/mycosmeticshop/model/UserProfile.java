package com.mycosmeticshop.model;

import java.math.BigDecimal;

public class UserProfile {
	private int userId;
	private String rank; // BRONZE | SILVER | GOLD | DIAMOND
	private int totalOrders;
	private BigDecimal totalSpent;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public int getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	public BigDecimal getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}

}
