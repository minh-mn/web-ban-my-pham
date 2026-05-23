package com.webshop.app.model;

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
	private BigDecimal couponDiscount;

	private String paymentMethod; // COD | VNPAY
	private String paymentStatus; // PENDING | PAID | CANCELED
	private String status; // processing | confirmed | shipping | completed | cancelled

	private String statusLabel; // HIỂN THỊ (VIEW)
	private String vnpTxnRef;
	private LocalDateTime createdAt;

	// ================= SHIPPING =================
	private String shippingMethod;   // ECONOMY | FAST | EXPRESS
	private String shippingProvider; // GHTK | GHN | INTERNAL
	private BigDecimal shippingFee;  // phí vận chuyển thực tế
	private String shippingCode;     // mã vận đơn
	private String shippingStatus;   // PENDING | CREATED | PICKING | DELIVERING | DELIVERED
	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;

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

	public BigDecimal getCouponDiscount() {
		return couponDiscount;
	}

	public void setCouponDiscount(BigDecimal couponDiscount) {
		this.couponDiscount = couponDiscount;
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
		if (statusLabel != null && !statusLabel.isBlank()) {
			return statusLabel;
		}

		return com.webshop.app.model.OrderStatus.labelOf(this.status);
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

	// ================= SHIPPING GET / SET =================

	public String getShippingMethod() {
		return shippingMethod;
	}

	public void setShippingMethod(String shippingMethod) {
		this.shippingMethod = shippingMethod;
	}

	public String getShippingProvider() {
		return shippingProvider;
	}

	public void setShippingProvider(String shippingProvider) {
		this.shippingProvider = shippingProvider;
	}

	public BigDecimal getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}

	public String getShippingCode() {
		return shippingCode;
	}

	public void setShippingCode(String shippingCode) {
		this.shippingCode = shippingCode;
	}

	public String getShippingStatus() {
		return shippingStatus;
	}

	public void setShippingStatus(String shippingStatus) {
		this.shippingStatus = shippingStatus;
	}

	public LocalDateTime getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	// =========================================================
	// VIEW PROPERTIES FOR JSP
	// =========================================================

	/**
	 * JSP gọi: ${order.totalVnd}
	 */
	public String getTotalVnd() {
		return formatVnd(total);
	}

	/**
	 * JSP gọi: ${order.couponDiscountVnd}
	 */
	public String getCouponDiscountVnd() {
		return formatVnd(couponDiscount);
	}

	/**
	 * JSP gọi: ${order.shippingFeeVnd}
	 */
	public String getShippingFeeVnd() {
		return formatVnd(shippingFee);
	}

	/**
	 * JSP gọi: ${order.shippingMethodLabel}
	 */
	public String getShippingMethodLabel() {
		if (shippingMethod == null || shippingMethod.isBlank()) {
			return "Giao hàng tiết kiệm";
		}

		switch (shippingMethod.toUpperCase()) {
			case "FAST":
				return "Giao hàng nhanh";
			case "EXPRESS":
				return "Hỏa tốc";
			case "ECONOMY":
			default:
				return "Giao hàng tiết kiệm";
		}
	}

	/**
	 * JSP gọi: ${order.shippingProviderLabel}
	 */
	public String getShippingProviderLabel() {
		if (shippingProvider == null || shippingProvider.isBlank()) {
			return "Nội bộ";
		}

		switch (shippingProvider.toUpperCase()) {
			case "GHTK":
				return "Giao hàng tiết kiệm";
			case "GHN":
				return "Giao hàng nhanh";
			case "INTERNAL":
				return "Vận chuyển nội bộ";
			default:
				return shippingProvider;
		}
	}

	/**
	 * JSP gọi: ${order.shippingStatusLabel}
	 */
	public String getShippingStatusLabel() {
		if (shippingStatus == null || shippingStatus.isBlank()) {
			return "Chờ tạo vận đơn";
		}

		switch (shippingStatus.toUpperCase()) {
			case "CREATED":
				return "Đã tạo vận đơn";
			case "PICKING":
				return "Đang lấy hàng";
			case "DELIVERING":
				return "Đang giao hàng";
			case "DELIVERED":
				return "Đã giao hàng";
			case "CANCELED":
				return "Đã hủy vận chuyển";
			case "PENDING":
			default:
				return "Chờ tạo vận đơn";
		}
	}

	/**
	 * JSP/JSTL fmt:formatDate dùng Date tốt hơn LocalDateTime.
	 * JSP gọi: ${order.createdAtDate}
	 */
	public Date getCreatedAtDate() {
		return toDate(createdAt);
	}

	/**
	 * JSP gọi: ${order.shippedAtDate}
	 */
	public Date getShippedAtDate() {
		return toDate(shippedAt);
	}

	/**
	 * JSP gọi: ${order.deliveredAtDate}
	 */
	public Date getDeliveredAtDate() {
		return toDate(deliveredAt);
	}

	private String formatVnd(BigDecimal value) {
		if (value == null) {
			return null;
		}

		NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(0);

		return nf.format(value);
	}

	private Date toDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}

		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
}