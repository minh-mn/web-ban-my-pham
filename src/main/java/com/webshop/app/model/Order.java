package com.webshop.app.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Order {

	private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");

	private int id;
	private int userId;
	private String fullName;
	private String phone;
	private String address;

	private BigDecimal total;
	private BigDecimal couponDiscount;

	private String paymentMethod; // COD | VNPAY
	private String paymentStatus; // PENDING | PAID | CANCELED
	private String status;        // processing | confirmed | shipping | completed | cancelled

	/**
	 * Nhãn trạng thái đơn hàng dùng riêng cho view.
	 * Nếu không set từ DAO thì getter sẽ tự map bằng OrderStatus.labelOf(status).
	 */
	private String statusLabel;

	private String vnpTxnRef;
	private LocalDateTime createdAt;

	// ================= SHIPPING / TRACKING =================
	private String shippingMethod;   // ECONOMY | FAST | EXPRESS
	private String shippingProvider; // GHTK | GHN | INTERNAL
	private BigDecimal shippingFee;  // phí vận chuyển thực tế
	private String shippingCode;     // mã vận đơn

	/**
	 * Trạng thái vận chuyển chuẩn:
	 * - PENDING_PICKUP: Chờ lấy hàng
	 * - DELIVERING: Đang giao
	 * - DELIVERED: Giao thành công
	 * - FAILED: Giao thất bại
	 * - CANCELED: Đã hủy vận chuyển
	 *
	 * Vẫn hỗ trợ các giá trị cũ trong DB như PENDING, CREATED, PICKING,
	 * DELIVERY_FAILED để tránh lỗi dữ liệu cũ.
	 */
	private String shippingStatus;

	private LocalDateTime shippedAt;
	private LocalDateTime deliveredAt;

	// ================= BASIC GET / SET =================

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
		this.total = safeMoney(total);
	}

	public BigDecimal getCouponDiscount() {
		return couponDiscount;
	}

	public void setCouponDiscount(BigDecimal couponDiscount) {
		this.couponDiscount = safeMoney(couponDiscount);
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = normalizeUpper(paymentMethod);
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = normalizeUpper(paymentStatus);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = normalizeLower(status);
	}

	public String getStatusLabel() {
		if (statusLabel != null && !statusLabel.isBlank()) {
			return statusLabel;
		}

		return OrderStatus.labelOf(this.status);
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public String getVnpTxnRef() {
		return vnpTxnRef;
	}

	public void setVnpTxnRef(String vnpTxnRef) {
		this.vnpTxnRef = normalizeNullable(vnpTxnRef);
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
		String value = normalizeUpper(shippingMethod);

		if (value == null) {
			this.shippingMethod = "ECONOMY";
			return;
		}

		switch (value) {
			case "FAST":
			case "EXPRESS":
			case "ECONOMY":
				this.shippingMethod = value;
				break;
			default:
				this.shippingMethod = "ECONOMY";
				break;
		}
	}

	public String getShippingProvider() {
		return shippingProvider;
	}

	public void setShippingProvider(String shippingProvider) {
		String value = normalizeUpper(shippingProvider);
		this.shippingProvider = value == null ? "INTERNAL" : value;
	}

	public BigDecimal getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = safeMoney(shippingFee);
	}

	public String getShippingCode() {
		return shippingCode;
	}

	public void setShippingCode(String shippingCode) {
		this.shippingCode = normalizeNullable(shippingCode);
	}

	/**
	 * Trả về mã tracking đã chuẩn hóa để JSP/DAO dùng thống nhất.
	 */
	public String getShippingStatus() {
		return ShippingStatus.normalizeCode(shippingStatus);
	}

	public void setShippingStatus(String shippingStatus) {
		this.shippingStatus = ShippingStatus.normalizeCode(shippingStatus);
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

	/** JSP gọi: ${order.totalVnd} */
	public String getTotalVnd() {
		return formatVnd(total);
	}

	/** JSP gọi: ${order.couponDiscountVnd} */
	public String getCouponDiscountVnd() {
		return formatVnd(couponDiscount);
	}

	/** JSP gọi: ${order.shippingFeeVnd} */
	public String getShippingFeeVnd() {
		return formatVnd(shippingFee);
	}

	/** JSP gọi: ${order.paymentMethodLabel} */
	public String getPaymentMethodLabel() {
		if (paymentMethod == null || paymentMethod.isBlank()) {
			return "Chưa xác định";
		}

		return switch (paymentMethod.toUpperCase()) {
			case "COD" -> "Thanh toán khi nhận hàng";
			case "VNPAY" -> "Thanh toán qua VNPAY";
			default -> paymentMethod;
		};
	}

	/** JSP gọi: ${order.paymentStatusLabel} */
	public String getPaymentStatusLabel() {
		if (paymentStatus == null || paymentStatus.isBlank()) {
			return "Chờ thanh toán";
		}

		return switch (paymentStatus.toUpperCase()) {
			case "PAID" -> "Đã thanh toán";
			case "CANCELED", "CANCELLED" -> "Đã hủy thanh toán";
			case "FAILED" -> "Thanh toán thất bại";
			case "PENDING" -> "Chờ thanh toán";
			default -> paymentStatus;
		};
	}

	/** JSP gọi: ${order.shippingMethodLabel} */
	public String getShippingMethodLabel() {
		if (shippingMethod == null || shippingMethod.isBlank()) {
			return "Giao hàng tiết kiệm";
		}

		return switch (shippingMethod.toUpperCase()) {
			case "FAST" -> "Giao hàng nhanh";
			case "EXPRESS" -> "Hỏa tốc";
			case "ECONOMY" -> "Giao hàng tiết kiệm";
			default -> shippingMethod;
		};
	}

	/** JSP gọi: ${order.shippingProviderLabel} */
	public String getShippingProviderLabel() {
		if (shippingProvider == null || shippingProvider.isBlank()) {
			return "Vận chuyển nội bộ";
		}

		return switch (shippingProvider.toUpperCase()) {
			case "GHTK" -> "Giao hàng tiết kiệm";
			case "GHN" -> "Giao hàng nhanh";
			case "INTERNAL" -> "Vận chuyển nội bộ";
			default -> shippingProvider;
		};
	}

	/** JSP gọi: ${order.shippingStatusLabel} */
	public String getShippingStatusLabel() {
		return ShippingStatus.labelOf(shippingStatus);
	}

	/** JSP gọi: ${order.shippingStatusCssClass} */
	public String getShippingStatusCssClass() {
		return ShippingStatus.cssClassOf(shippingStatus);
	}

	/** JSP gọi: ${order.shippingStatusStep} để vẽ timeline. */
	public int getShippingStatusStep() {
		return ShippingStatus.stepOf(shippingStatus);
	}

	/** JSP gọi: ${order.pendingPickup} */
	public boolean isPendingPickup() {
		return ShippingStatus.PENDING_PICKUP.matches(shippingStatus);
	}

	/** JSP gọi: ${order.delivering} */
	public boolean isDelivering() {
		return ShippingStatus.DELIVERING.matches(shippingStatus);
	}

	/** JSP gọi: ${order.delivered} */
	public boolean isDelivered() {
		return ShippingStatus.DELIVERED.matches(shippingStatus);
	}

	/** JSP gọi: ${order.deliveryFailed} */
	public boolean isDeliveryFailed() {
		return ShippingStatus.FAILED.matches(shippingStatus);
	}

	/** JSP gọi: ${order.shippingCanceled} */
	public boolean isShippingCanceled() {
		return ShippingStatus.CANCELED.matches(shippingStatus);
	}

	/** JSP gọi: ${order.shippingFinished} */
	public boolean isShippingFinished() {
		return ShippingStatus.fromCode(shippingStatus).isTerminal();
	}

	/** JSP/JSTL fmt:formatDate dùng Date tốt hơn LocalDateTime. */
	public Date getCreatedAtDate() {
		return toDate(createdAt);
	}

	public Date getShippedAtDate() {
		return toDate(shippedAt);
	}

	public Date getDeliveredAtDate() {
		return toDate(deliveredAt);
	}

	// =========================================================
	// HELPER METHODS
	// =========================================================

	private BigDecimal safeMoney(BigDecimal value) {
		if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}

		return value;
	}

	private String formatVnd(BigDecimal value) {
		BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;

		NumberFormat formatter = NumberFormat.getInstance(VIETNAM_LOCALE);
		formatter.setGroupingUsed(true);
		formatter.setMaximumFractionDigits(0);

		return formatter.format(safeValue);
	}

	private Date toDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}

		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private String normalizeUpper(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		return value.trim().toUpperCase(Locale.ROOT);
	}

	private String normalizeLower(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		return value.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeNullable(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		return value.trim();
	}
}
