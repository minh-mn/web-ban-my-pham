package com.mycosmeticshop.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Order {

	private int id;
	private int userId;
	private String fullName;
	private String phone;
	private String address;

	private BigDecimal total;

	private String paymentMethod; // COD | VNPAY
	private String paymentStatus; // PENDING | PAID | CANCELED
	private String status; // processing | confirmed | shipping | completed | cancelled

	private String statusLabel; // HIỂN THỊ (VIEW)
	private String vnpTxnRef;
	private LocalDateTime createdAt;

	// ================= GET / SET =================

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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/* ===== STATUS LABEL (VIEW ONLY) ===== */

	public String getStatusLabel() {
		if (statusLabel != null && !statusLabel.isBlank())
			return statusLabel;
		return com.mycosmeticshop.model.OrderStatus.labelOf(this.status);
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public String getVnpTxnRef() {
		return vnpTxnRef;
	}

	public void setVnpTxnRef(String vnpTxnRef) {
		this.vnpTxnRef = vnpTxnRef;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// =========================================================
	// VIEW PROPERTIES FOR JSP
	// =========================================================

	/**
	 * JSP gọi: ${order.totalVnd} => Java gọi getTotalVnd() Trả chuỗi đã format kiểu
	 * VN: 1.234.567
	 */
	public String getTotalVnd() {
		if (total == null)
			return null;

		NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(0); // bỏ phần lẻ

		return nf.format(total);
	}

	/**
	 * JSP/JSTL fmt:formatDate ăn chắc với java.util.Date hơn LocalDateTime. JSP
	 * gọi: ${order.createdAtDate} => Java gọi getCreatedAtDate()
	 */
	public Date getCreatedAtDate() {
		if (createdAt == null)
			return null;
		return Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
	}
}
