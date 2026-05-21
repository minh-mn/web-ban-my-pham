package com.webshop.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.OrderDAO;
import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.Order;
import com.webshop.app.model.OrderItem;
import com.webshop.app.utils.DBConnection;

public class CheckoutService {

	private final OrderDAO orderDAO = new OrderDAO();
	private final OrderItemDAO itemDAO = new OrderItemDAO();
	private final CouponDAO couponDAO = new CouponDAO();
	private final CouponService couponService = new CouponService();
	private final UserRankService userRankService = new UserRankService();

	public int checkout(int userId,
						Map<Integer, CartItem> cart,
						String fullName,
						String phone,
						String address,
						String paymentMethod,
						String couponCode) {

		if (userId <= 0) {
			throw new IllegalArgumentException("Invalid userId");
		}

		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Cart is empty");
		}

		if (paymentMethod == null || paymentMethod.isBlank()) {
			paymentMethod = "COD";
		}

		boolean isCod = "COD".equalsIgnoreCase(paymentMethod);
		boolean isVnp = "VNPAY".equalsIgnoreCase(paymentMethod);

		BigDecimal subtotal = calculateCartSubtotal(cart);

		/*
		 * =========================
		 * COUPON DISCOUNT
		 * =========================
		 */
		Coupon coupon = null;
		BigDecimal couponDiscount = BigDecimal.ZERO;

		if (couponCode != null && !couponCode.isBlank()) {
			coupon = couponService.validateCoupon(couponCode, subtotal);

			if (coupon != null) {
				couponDiscount = couponService.calculateDiscount(coupon, subtotal);
			}
		}

		couponDiscount = money0(couponDiscount);

		/*
		 * =========================
		 * RANK DISCOUNT
		 * =========================
		 * Rank discount được tính sau coupon:
		 * subtotal - couponDiscount = amountAfterCoupon
		 * rankDiscount = amountAfterCoupon * rankPercent
		 */
		BigDecimal amountAfterCoupon = subtotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		BigDecimal rankDiscount = calculateRankDiscount(userId, amountAfterCoupon);

		BigDecimal total = amountAfterCoupon.subtract(rankDiscount);

		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		}

		total = money0(total);

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			// Kiểm tra userId phải tồn tại trong bảng users(id)
			if (!existsUsersId(conn, userId)) {
				conn.rollback();
				throw new IllegalStateException("Invalid session userId (not found in users): " + userId);
			}

			/*
			 * =========================
			 * LOCK & CHECK STOCK
			 * =========================
			 * MySQL dùng FOR UPDATE để khóa dòng sản phẩm trong transaction.
			 */
			String lockSql = "SELECT stock FROM store_product WHERE id = ? FOR UPDATE";

			for (CartItem item : cart.values()) {
				try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
					ps.setInt(1, item.getProductId());

					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next() || rs.getInt("stock") < item.getQuantity()) {
							conn.rollback();
							throw new RuntimeException("Không đủ tồn kho cho sản phẩm ID " + item.getProductId());
						}
					}
				}
			}

			/*
			 * =========================
			 * CREATE ORDER
			 * =========================
			 * Lưu ý:
			 * - total đã bao gồm coupon discount và rank discount.
			 * - Nếu muốn lưu riêng rank_discount/coupon_discount,
			 *   cần thêm cột trong store_order và setter trong Order model.
			 */
			Order o = new Order();
			o.setUserId(userId);
			o.setFullName(fullName);
			o.setPhone(phone);
			o.setAddress(address);
			o.setTotal(total);
			o.setPaymentMethod(paymentMethod);

			if (isCod) {
				o.setPaymentStatus("PENDING");
				o.setStatus("confirmed");
			} else if (isVnp) {
				o.setPaymentStatus("PENDING");
				o.setStatus("processing");
			} else {
				o.setPaymentStatus("PENDING");
				o.setStatus("processing");
			}

			int orderId = orderDAO.create(conn, o);

			/*
			 * =========================
			 * COD FINALIZE IMMEDIATELY
			 * =========================
			 */
			if (isCod) {
				createOrderItemsAndUpdateStock(conn, orderId, cart);

				if (coupon != null) {
					couponDAO.increaseUsedCount(conn, coupon.getId());
				}
			}

			conn.commit();
			return orderId;

		} catch (Exception e) {
			throw new RuntimeException("Checkout failed", e);
		}
	}

	public void finalizeVnpayPaid(int orderId, Map<Integer, CartItem> cart, String couponCode) {

		if (orderId <= 0) {
			throw new IllegalArgumentException("Invalid orderId");
		}

		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Cart is empty");
		}

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			Order o;

			try {
				o = orderDAO.findById(conn, orderId);
			} catch (Exception ignore) {
				o = orderDAO.findById(orderId);
			}

			if (o == null) {
				conn.rollback();
				throw new RuntimeException("Order not found: " + orderId);
			}

			/*
			 * =========================
			 * IDEMPOTENT CHECK
			 * =========================
			 * Nếu order item đã tồn tại, không insert lại để tránh trừ kho 2 lần.
			 */
			boolean hasItems = itemDAO.existsByOrderId(conn, orderId);

			if (hasItems) {
				if (!"PAID".equalsIgnoreCase(o.getPaymentStatus())) {
					updatePaidStatus(conn, orderId, o.getVnpTxnRef());
					conn.commit();
					return;
				}

				conn.rollback();
				return;
			}

			/*
			 * =========================
			 * LOCK & CHECK STOCK
			 * =========================
			 */
			String lockSql = "SELECT stock FROM store_product WHERE id = ? FOR UPDATE";

			for (CartItem item : cart.values()) {
				try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
					ps.setInt(1, item.getProductId());

					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next() || rs.getInt("stock") < item.getQuantity()) {
							conn.rollback();
							throw new RuntimeException(
									"Không đủ tồn kho khi finalize VNPAY cho sản phẩm ID " + item.getProductId()
							);
						}
					}
				}
			}

			createOrderItemsAndUpdateStock(conn, orderId, cart);

			/*
			 * =========================
			 * UPDATE COUPON USED COUNT
			 * =========================
			 */
			if (couponCode != null && !couponCode.isBlank()) {
				Coupon coupon = couponDAO.findByCode(couponCode.trim());

				if (coupon != null) {
					couponDAO.increaseUsedCount(conn, coupon.getId());
				}
			}

			updatePaidStatus(conn, orderId, o.getVnpTxnRef());

			conn.commit();

		} catch (Exception e) {
			throw new RuntimeException("Finalize VNPAY failed", e);
		}
	}

	/*
	 * =========================
	 * DISCOUNT PREVIEW METHODS
	 * =========================
	 * Các method này dùng cho servlet/JSP checkout preview nếu cần hiển thị:
	 * - giảm giá coupon
	 * - giảm giá rank
	 * - tổng tiền sau giảm
	 */

	public BigDecimal calculateCouponDiscount(String couponCode, BigDecimal subTotal) {

		BigDecimal safeSubTotal = money0(subTotal);

		if (couponCode == null || couponCode.isBlank()) {
			return BigDecimal.ZERO;
		}

		if (safeSubTotal.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		Coupon coupon = couponService.validateCoupon(couponCode, safeSubTotal);

		if (coupon == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal discount = calculateDiscountFromCoupon(coupon, safeSubTotal);

		if (discount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		return money0(discount);
	}

	public BigDecimal calculateRankDiscount(int userId, BigDecimal amountAfterCoupon) {

		if (userId <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal safeAmount = money0(amountAfterCoupon);

		if (safeAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		try {
			return money0(userRankService.calculateRankDiscountAmount(userId, safeAmount));
		} catch (RuntimeException e) {
			/*
			 * Nếu bảng store_rank chưa migrate hoặc có lỗi dữ liệu rank,
			 * không làm hỏng luồng checkout.
			 */
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}

	public BigDecimal calculateTotalAfterCouponAndRank(int userId,
													   BigDecimal subTotal,
													   String couponCode) {

		BigDecimal safeSubTotal = money0(subTotal);

		BigDecimal couponDiscount = calculateCouponDiscount(couponCode, safeSubTotal);
		BigDecimal amountAfterCoupon = safeSubTotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		BigDecimal rankDiscount = calculateRankDiscount(userId, amountAfterCoupon);
		BigDecimal total = amountAfterCoupon.subtract(rankDiscount);

		if (total.compareTo(BigDecimal.ZERO) < 0) {
			total = BigDecimal.ZERO;
		}

		return money0(total);
	}

	private BigDecimal calculateDiscountFromCoupon(Coupon coupon, BigDecimal subTotal) {

		if (coupon == null || subTotal == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal percent = BigDecimal.valueOf(coupon.getDiscountPercent());

		BigDecimal discount = subTotal
				.multiply(percent)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		if (coupon.getMaxDiscountAmount() != null) {
			discount = discount.min(coupon.getMaxDiscountAmount());
		}

		if (discount.compareTo(BigDecimal.ZERO) < 0) {
			discount = BigDecimal.ZERO;
		}

		return discount;
	}

	/*
	 * =========================
	 * ORDER ITEM / STOCK HELPERS
	 * =========================
	 */

	private void createOrderItemsAndUpdateStock(Connection conn,
												int orderId,
												Map<Integer, CartItem> cart) throws Exception {

		String updateStockSql = "UPDATE store_product SET stock = stock - ? WHERE id = ?";

		for (CartItem item : cart.values()) {
			OrderItem oi = new OrderItem();
			oi.setOrderId(orderId);
			oi.setProductId(item.getProductId());
			oi.setPrice(item.getPrice());
			oi.setQuantity(item.getQuantity());

			itemDAO.create(conn, oi);

			try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
				ps.setInt(1, item.getQuantity());
				ps.setInt(2, item.getProductId());
				ps.executeUpdate();
			}
		}
	}

	private void updatePaidStatus(Connection conn, int orderId, String txnRef) {

		try {
			orderDAO.updatePaymentStatus(conn, orderId, "PAID", "confirmed", txnRef);
		} catch (Exception ignore) {
			orderDAO.updatePaymentStatus(orderId, "PAID", "confirmed", txnRef);
		}
	}

	private BigDecimal calculateCartSubtotal(Map<Integer, CartItem> cart) {

		if (cart == null || cart.isEmpty()) {
			return BigDecimal.ZERO;
		}

		return cart.values()
				.stream()
				.map(CartItem::getSubtotal)
				.filter(x -> x != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(0, RoundingMode.HALF_UP);
	}

	private BigDecimal money0(BigDecimal value) {

		if (value == null) {
			return BigDecimal.ZERO;
		}

		if (value.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}

		return value.setScale(0, RoundingMode.HALF_UP);
	}

	private boolean existsUsersId(Connection conn, int userId) {

		String sql = "SELECT 1 FROM users WHERE id = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}

		} catch (Exception e) {
			throw new RuntimeException("Không thể kiểm tra user_id trong bảng users", e);
		}
	}
}