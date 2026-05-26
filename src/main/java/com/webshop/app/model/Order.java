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

	private BigDecimal total = BigDecimal.ZERO;
	private BigDecimal couponDiscount = BigDecimal.ZERO;

	private String paymentMethod; // COD | VNPAY
	private String paymentStatus; // PENDING | PAID | CANCELED | CANCELLED | FAILED
	private String status;        // processing | confirmed | shipping | completed | cancelled

	/**
	 * Nhãn trạng thái đơn hàng dùng riêng cho view.
	 * Nếu không set từ DAO thì getter sẽ tự map bằng OrderStatus.labelOf(status).
	 */
	private String statusLabel;

	private String vnpTxnRef;
	private LocalDateTime createdAt;

	/**
	 * Coupon đã áp dụng cho đơn hàng.
	 * Dùng để finalize VNPay/retry mà không phụ thuộc session VNP_COUPON.
	 */
	private Integer couponId;

	/**
	 * Với VNPay, order item được lưu trước nhưng kho chỉ được trừ sau khi thanh toán thành công.
	 * Cờ này ngăn trừ kho/tăng used_count voucher nhiều lần khi VNPay return/IPN gọi lại.
	 */
	private boolean stockDeducted;

	private int paymentAttemptCount;
	private String lastPaymentError;

	// ================= SHIPPING / TRACKING =================

	private String shippingMethod;   // ECONOMY | FAST | EXPRESS
	private String shippingProvider; // GHTK | GHN | INTERNAL
	private BigDecimal shippingFee = BigDecimal.ZERO;
	private String shippingCode;

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
		this.id = Math.max(id, 0);
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = Math.max(userId, 0);
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = normalizeNullable(fullName);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = normalizeNullable(phone);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = normalizeNullable(address);
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
		this.statusLabel = normalizeNullable(statusLabel);
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

	public Integer getCouponId() {
		return couponId;
	}

	public void setCouponId(Integer couponId) {
		if (couponId == null || couponId <= 0) {
			this.couponId = null;
		} else {
			this.couponId = couponId;
		}
	}

	public boolean isStockDeducted() {
		return stockDeducted;
	}

	public boolean getStockDeducted() {
		return stockDeducted;
	}

	public void setStockDeducted(boolean stockDeducted) {
		this.stockDeducted = stockDeducted;
	}

	public int getPaymentAttemptCount() {
		return paymentAttemptCount;
	}

	public void setPaymentAttemptCount(int paymentAttemptCount) {
		this.paymentAttemptCount = Math.max(paymentAttemptCount, 0);
	}

	public String getLastPaymentError() {
		return lastPaymentError;
	}

	public void setLastPaymentError(String lastPaymentError) {
		this.lastPaymentError = normalizeNullable(lastPaymentError);
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
	 * JSP gọi: ${order.paymentMethodLabel}
	 */
	public String getPaymentMethodLabel() {
		if (paymentMethod == null || paymentMethod.isBlank()) {
			return "Chưa xác định";
		}

		return switch (paymentMethod.toUpperCase(Locale.ROOT)) {
			case "COD" -> "Thanh toán khi nhận hàng";
			case "VNPAY" -> "Thanh toán qua VNPAY";
			default -> paymentMethod;
		};
	}

	/**
	 * JSP gọi: ${order.paymentStatusLabel}
	 */
	public String getPaymentStatusLabel() {
		if (paymentStatus == null || paymentStatus.isBlank()) {
			return "Chờ thanh toán";
		}

		return switch (paymentStatus.toUpperCase(Locale.ROOT)) {
			case "PAID" -> "Đã thanh toán";
			case "CANCELED", "CANCELLED" -> "Đã hủy thanh toán";
			case "FAILED" -> "Thanh toán thất bại";
			case "PENDING" -> "Chờ thanh toán";
			default -> paymentStatus;
		};
	}

	/**
	 * JSP gọi: ${order.retryPaymentAvailable}
	 *
	 * Điều kiện được thanh toán lại:
	 * - Đơn thanh toán bằng VNPay
	 * - Chưa thanh toán thành công
	 * - Đơn chưa bị hủy
	 * - Đơn chưa hoàn tất
	 */
	public boolean isRetryPaymentAvailable() {
		boolean isVnpay = "VNPAY".equalsIgnoreCase(paymentMethod);
		boolean paid = "PAID".equalsIgnoreCase(paymentStatus);

		boolean cancelled = "cancelled".equalsIgnoreCase(status)
				|| "canceled".equalsIgnoreCase(status)
				|| "CANCELLED".equalsIgnoreCase(status)
				|| "CANCELED".equalsIgnoreCase(status);

		boolean completed = "completed".equalsIgnoreCase(status)
				|| "COMPLETED".equalsIgnoreCase(status);

		return isVnpay && !paid && !cancelled && !completed;
	}

	/**
	 * Thêm getter dạng get... để Tomcat/JSP EL nhận property chắc chắn.
	 * JSP vẫn gọi: ${order.retryPaymentAvailable}
	 */
	public boolean getRetryPaymentAvailable() {
		return isRetryPaymentAvailable();
	}

	/**
	 * JSP gọi: ${order.canRetryPayment}
	 * Getter phụ để nếu sau này muốn đổi JSP thành ${order.canRetryPayment}.
	 */
	public boolean isCanRetryPayment() {
		return isRetryPaymentAvailable();
	}

	public boolean getCanRetryPayment() {
		return isRetryPaymentAvailable();
	}

	/**
	 * JSP gọi: ${order.shippingMethodLabel}
	 */
	public String getShippingMethodLabel() {
		if (shippingMethod == null || shippingMethod.isBlank()) {
			return "Giao hàng tiết kiệm";
		}

		return switch (shippingMethod.toUpperCase(Locale.ROOT)) {
			case "FAST" -> "Giao hàng nhanh";
			case "EXPRESS" -> "Hỏa tốc";
			case "ECONOMY" -> "Giao hàng tiết kiệm";
			default -> shippingMethod;
		};
	}

	/**
	 * JSP gọi: ${order.shippingProviderLabel}
	 */
	public String getShippingProviderLabel() {
		if (shippingProvider == null || shippingProvider.isBlank()) {
			return "Vận chuyển nội bộ";
		}

		return switch (shippingProvider.toUpperCase(Locale.ROOT)) {
			case "GHTK" -> "Giao hàng tiết kiệm";
			case "GHN" -> "Giao hàng nhanh";
			case "INTERNAL" -> "Vận chuyển nội bộ";
			default -> shippingProvider;
		};
	}

	/**
	 * JSP gọi: ${order.shippingStatusLabel}
	 */
	public String getShippingStatusLabel() {
		return ShippingStatus.labelOf(shippingStatus);
	}

	/**
	 * JSP gọi: ${order.shippingStatusCssClass}
	 */
	public String getShippingStatusCssClass() {
		return ShippingStatus.cssClassOf(shippingStatus);
	}

	/**
	 * JSP gọi: ${order.shippingStatusStep} để vẽ timeline.
	 */
	public int getShippingStatusStep() {
		return ShippingStatus.stepOf(shippingStatus);
	}

	/**
	 * JSP gọi: ${order.pendingPickup}
	 */
	public boolean isPendingPickup() {
		return ShippingStatus.PENDING_PICKUP.matches(shippingStatus);
	}

	public boolean getPendingPickup() {
		return isPendingPickup();
	}

	/**
	 * JSP gọi: ${order.delivering}
	 */
	public boolean isDelivering() {
		return ShippingStatus.DELIVERING.matches(shippingStatus);
	}

	public boolean getDelivering() {
		return isDelivering();
	}

	/**
	 * JSP gọi: ${order.delivered}
	 */
	public boolean isDelivered() {
		return ShippingStatus.DELIVERED.matches(shippingStatus);
	}

	public boolean getDelivered() {
		return isDelivered();
	}

	/**
	 * JSP gọi: ${order.deliveryFailed}
	 */
	public boolean isDeliveryFailed() {
		return ShippingStatus.FAILED.matches(shippingStatus);
	}

	public boolean getDeliveryFailed() {
		return isDeliveryFailed();
	}

	/**
	 * JSP gọi: ${order.shippingCanceled}
	 */
	public boolean isShippingCanceled() {
		return ShippingStatus.CANCELED.matches(shippingStatus);
	}

	public boolean getShippingCanceled() {
		return isShippingCanceled();
	}

	/**
	 * JSP gọi: ${order.shippingFinished}
	 */
	public boolean isShippingFinished() {
		return ShippingStatus.fromCode(shippingStatus).isTerminal();
	}

	public boolean getShippingFinished() {
		return isShippingFinished();
	}

	/**
	 * JSP/JSTL fmt:formatDate dùng Date tốt hơn LocalDateTime.
	 */
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