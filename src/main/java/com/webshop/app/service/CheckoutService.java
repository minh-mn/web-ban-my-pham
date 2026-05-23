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

	private static final BigDecimal FREE_SHIP_THRESHOLD = new BigDecimal("500000");

	private static final String DEFAULT_RANK_CODE = "MEMBER";

	private static final String SHIPPING_ECONOMY = "ECONOMY";
	private static final String SHIPPING_FAST = "FAST";
	private static final String SHIPPING_EXPRESS = "EXPRESS";

	private final OrderDAO orderDAO = new OrderDAO();
	private final OrderItemDAO itemDAO = new OrderItemDAO();
	private final CouponDAO couponDAO = new CouponDAO();
	private final CouponService couponService = new CouponService();
	private final UserRankService userRankService = new UserRankService();

	/*
	 * Giữ method cũ để các chỗ khác đang gọi checkout 7 tham số không bị lỗi.
	 */
	public int checkout(int userId,
	                    Map<String, CartItem> cart,
	                    String fullName,
	                    String phone,
	                    String address,
	                    String paymentMethod,
	                    String couponCode) {

		return checkout(
				userId,
				cart,
				fullName,
				phone,
				address,
				paymentMethod,
				couponCode,
				SHIPPING_ECONOMY,
				BigDecimal.ZERO,
				null
		);
	}

	/*
	 * Method mới dùng cho checkout có phương thức vận chuyển, phí ship và freeship.
	 */
	public int checkout(int userId,
	                    Map<String, CartItem> cart,
	                    String fullName,
	                    String phone,
	                    String address,
	                    String paymentMethod,
	                    String couponCode,
	                    String shippingMethod,
	                    BigDecimal submittedShippingFee,
	                    String province) {

		if (userId <= 0) {
			throw new IllegalArgumentException("Invalid userId");
		}

		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Cart is empty");
		}

		if (paymentMethod == null || paymentMethod.isBlank()) {
			paymentMethod = "COD";
		}

		shippingMethod = normalizeShippingMethod(shippingMethod);

		boolean isCod = "COD".equalsIgnoreCase(paymentMethod);
		boolean isVnp = "VNPAY".equalsIgnoreCase(paymentMethod);

		BigDecimal subtotal = calculateCartSubtotal(cart);
		String userRankCode = resolveUserRankCode(userId);

		/*
		 * =========================
		 * COUPON DISCOUNT
		 * =========================
		 * Coupon được validate theo:
		 * - active / ngày hiệu lực / lượt dùng
		 * - min_order_amount
		 * - min_rank_code
		 */
		Coupon coupon = null;
		BigDecimal couponDiscount = BigDecimal.ZERO;

		if (couponCode != null && !couponCode.isBlank()) {
			coupon = validateCouponForCheckoutOrThrow(couponCode, subtotal, userRankCode);
			couponDiscount = couponService.calculateDiscount(coupon, subtotal);
		}

		couponDiscount = money0(couponDiscount);

		/*
		 * =========================
		 * AMOUNT AFTER COUPON
		 * =========================
		 * Freeship được xét theo tổng sau voucher/coupon.
		 */
		BigDecimal amountAfterCoupon = subtotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		/*
		 * =========================
		 * RANK DISCOUNT
		 * =========================
		 * Rank discount được tính sau coupon.
		 */
		BigDecimal rankDiscount = calculateRankDiscount(userId, amountAfterCoupon);

		BigDecimal amountAfterAllDiscounts = amountAfterCoupon.subtract(rankDiscount);

		if (amountAfterAllDiscounts.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterAllDiscounts = BigDecimal.ZERO;
		}

		/*
		 * =========================
		 * SHIPPING FEE + FREESHIP
		 * =========================
		 * Backend tự tính lại phí ship, không tin hoàn toàn dữ liệu từ client.
		 */
		BigDecimal shippingFee = calculateShippingFee(
				shippingMethod,
				province,
				amountAfterCoupon,
				submittedShippingFee
		);

		BigDecimal total = amountAfterAllDiscounts.add(shippingFee);
		total = money0(total);

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			if (!existsUsersId(conn, userId)) {
				conn.rollback();
				throw new IllegalStateException("Invalid session userId (not found in users): " + userId);
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
							throw new RuntimeException("Không đủ tồn kho cho sản phẩm ID " + item.getProductId());
						}
					}
				}
			}

			/*
			 * =========================
			 * CREATE ORDER
			 * =========================
			 */
			Order order = new Order();
			order.setUserId(userId);
			order.setFullName(fullName);
			order.setPhone(phone);
			order.setAddress(address);
			order.setTotal(total);
			order.setCouponDiscount(couponDiscount);
			order.setPaymentMethod(paymentMethod);

			/*
			 * =========================
			 * SAVE SHIPPING INFO
			 * =========================
			 */
			order.setShippingMethod(shippingMethod);
			order.setShippingProvider(resolveShippingProvider(shippingMethod));
			order.setShippingFee(shippingFee);
			order.setShippingCode(null);
			order.setShippingStatus("PENDING");

			if (isCod) {
				order.setPaymentStatus("PENDING");
				order.setStatus("confirmed");
			} else if (isVnp) {
				order.setPaymentStatus("PENDING");
				order.setStatus("processing");
			} else {
				order.setPaymentStatus("PENDING");
				order.setStatus("processing");
			}

			int orderId = orderDAO.create(conn, order);

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

	public void finalizeVnpayPaid(int orderId, Map<String, CartItem> cart, String couponCode) {

		if (orderId <= 0) {
			throw new IllegalArgumentException("Invalid orderId");
		}

		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Cart is empty");
		}

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			Order order;

			try {
				order = orderDAO.findById(conn, orderId);
			} catch (Exception ignore) {
				order = orderDAO.findById(orderId);
			}

			if (order == null) {
				conn.rollback();
				throw new RuntimeException("Order not found: " + orderId);
			}

			boolean hasItems = itemDAO.existsByOrderId(conn, orderId);

			if (hasItems) {
				if (!"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
					updatePaidStatus(conn, orderId, order.getVnpTxnRef());
					conn.commit();
					return;
				}

				conn.rollback();
				return;
			}

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

			if (couponCode != null && !couponCode.isBlank()) {
				Coupon coupon = couponDAO.findByCode(couponCode.trim());

				if (coupon != null) {
					couponDAO.increaseUsedCount(conn, coupon.getId());
				}
			}

			updatePaidStatus(conn, orderId, order.getVnpTxnRef());

			conn.commit();

		} catch (Exception e) {
			throw new RuntimeException("Finalize VNPAY failed", e);
		}
	}

	/*
	 * =========================
	 * DISCOUNT PREVIEW METHODS
	 * =========================
	 */

	/*
	 * Method cũ: giữ lại để tránh lỗi các file khác đang gọi.
	 * Vì không có userId nên mặc định rank là MEMBER.
	 */
	public BigDecimal calculateCouponDiscount(String couponCode, BigDecimal subTotal) {
		return calculateCouponDiscountByRank(couponCode, subTotal, DEFAULT_RANK_CODE);
	}

	/*
	 * Method mới: preview coupon discount theo userId.
	 * Dùng cho CheckoutServlet hoặc AjaxApplyCouponServlet để tính đúng theo rank user.
	 */
	public BigDecimal calculateCouponDiscount(int userId, String couponCode, BigDecimal subTotal) {

		if (userId <= 0) {
			return BigDecimal.ZERO;
		}

		String userRankCode = resolveUserRankCode(userId);

		return calculateCouponDiscountByRank(couponCode, subTotal, userRankCode);
	}

	public BigDecimal calculateCouponDiscountByRank(
			String couponCode,
			BigDecimal subTotal,
			String userRankCode
	) {

		BigDecimal safeSubTotal = money0(subTotal);

		if (couponCode == null || couponCode.isBlank()) {
			return BigDecimal.ZERO;
		}

		if (safeSubTotal.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		Coupon coupon = couponService.validateCoupon(couponCode, safeSubTotal, userRankCode);

		if (coupon == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal discount = couponService.calculateDiscount(coupon, safeSubTotal);

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
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}

	public BigDecimal calculateTotalAfterCouponAndRank(int userId,
	                                                   BigDecimal subTotal,
	                                                   String couponCode) {

		BigDecimal safeSubTotal = money0(subTotal);

		BigDecimal couponDiscount = calculateCouponDiscount(userId, couponCode, safeSubTotal);
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

	public BigDecimal calculateTotalAfterDiscountsAndShipping(int userId,
	                                                          BigDecimal subTotal,
	                                                          String couponCode,
	                                                          String shippingMethod,
	                                                          String province) {

		BigDecimal safeSubTotal = money0(subTotal);

		BigDecimal couponDiscount = calculateCouponDiscount(userId, couponCode, safeSubTotal);
		BigDecimal amountAfterCoupon = safeSubTotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		BigDecimal rankDiscount = calculateRankDiscount(userId, amountAfterCoupon);
		BigDecimal amountAfterAllDiscounts = amountAfterCoupon.subtract(rankDiscount);

		if (amountAfterAllDiscounts.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterAllDiscounts = BigDecimal.ZERO;
		}

		BigDecimal shippingFee = calculateShippingFee(
				shippingMethod,
				province,
				amountAfterCoupon,
				BigDecimal.ZERO
		);

		return money0(amountAfterAllDiscounts.add(shippingFee));
	}

	/*
	 * =========================
	 * COUPON VALIDATION HELPERS
	 * =========================
	 */

	private Coupon validateCouponForCheckoutOrThrow(
			String couponCode,
			BigDecimal subtotal,
			String userRankCode
	) {

		Coupon coupon = couponService.validateCoupon(couponCode, subtotal, userRankCode);

		if (coupon != null) {
			return coupon;
		}

		Coupon existingCoupon = couponDAO.findByCode(couponCode.trim());
		String message = couponService.buildCouponErrorMessage(
				existingCoupon,
				subtotal,
				userRankCode
		);

		if (message == null || message.isBlank()) {
			message = "Mã giảm giá không hợp lệ hoặc không đủ điều kiện áp dụng.";
		}

		throw new IllegalArgumentException(message);
	}

	/*
	 * =========================
	 * USER RANK HELPERS
	 * =========================
	 */

	private String resolveUserRankCode(int userId) {

		if (userId <= 0) {
			return DEFAULT_RANK_CODE;
		}

		try (Connection conn = DBConnection.getConnection()) {
			return resolveUserRankCode(conn, userId);
		} catch (Exception e) {
			e.printStackTrace();
			return DEFAULT_RANK_CODE;
		}
	}

	private String resolveUserRankCode(Connection conn, int userId) {

		String manualRankCode = findManualRankCode(conn, userId);

		if (manualRankCode != null && !manualRankCode.isBlank()) {
			return normalizeRankCode(manualRankCode);
		}

		BigDecimal totalSpent = findCompletedOrderTotal(conn, userId);
		String rankBySpent = findRankCodeByTotalSpent(conn, totalSpent);

		return normalizeRankCode(rankBySpent);
	}

	private String findManualRankCode(Connection conn, int userId) {

		String sql = """
                SELECT manual_rank_code
                FROM users
                WHERE id = ?
                LIMIT 1
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("manual_rank_code");
				}
			}

		} catch (Exception e) {
			// Nếu database cũ chưa có manual_rank_code thì fallback về rank theo chi tiêu.
			return null;
		}

		return null;
	}

	private BigDecimal findCompletedOrderTotal(Connection conn, int userId) {

		String sql = """
                SELECT COALESCE(SUM(total), 0) AS total_spent
                FROM store_order
                WHERE user_id = ?
                  AND LOWER(status) = 'completed'
                  AND (
                        payment_status IS NULL
                        OR UPPER(payment_status) IN ('PAID', 'PENDING')
                  )
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					BigDecimal totalSpent = rs.getBigDecimal("total_spent");
					return totalSpent == null ? BigDecimal.ZERO : totalSpent;
				}
			}

		} catch (Exception e) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.ZERO;
	}

	private String findRankCodeByTotalSpent(Connection conn, BigDecimal totalSpent) {

		String sql = """
                SELECT code
                FROM store_rank
                WHERE is_active = 1
                  AND min_spent <= ?
                ORDER BY priority DESC, min_spent DESC
                LIMIT 1
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, money0(totalSpent));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("code");
				}
			}

		} catch (Exception e) {
			return DEFAULT_RANK_CODE;
		}

		return DEFAULT_RANK_CODE;
	}

	private String normalizeRankCode(String rankCode) {

		if (rankCode == null || rankCode.isBlank()) {
			return DEFAULT_RANK_CODE;
		}

		String normalized = rankCode.trim().toUpperCase();

		return switch (normalized) {
			case "MEMBER", "SILVER", "GOLD", "DIAMOND", "VIP" -> normalized;
			default -> DEFAULT_RANK_CODE;
		};
	}

	/*
	 * =========================
	 * SHIPPING HELPERS
	 * =========================
	 */

	private String normalizeShippingMethod(String shippingMethod) {

		if (shippingMethod == null || shippingMethod.isBlank()) {
			return SHIPPING_ECONOMY;
		}

		String method = shippingMethod.trim().toUpperCase();

		if (SHIPPING_FAST.equals(method)
				|| SHIPPING_EXPRESS.equals(method)
				|| SHIPPING_ECONOMY.equals(method)) {
			return method;
		}

		return SHIPPING_ECONOMY;
	}

	private String resolveShippingProvider(String shippingMethod) {
		String method = normalizeShippingMethod(shippingMethod);

		if (SHIPPING_FAST.equals(method)) {
			return "GHN";
		}

		if (SHIPPING_EXPRESS.equals(method)) {
			return "INTERNAL";
		}

		return "GHTK";
	}

	private BigDecimal calculateShippingFee(String shippingMethod,
	                                        String province,
	                                        BigDecimal amountAfterCoupon,
	                                        BigDecimal submittedShippingFee) {

		BigDecimal safeAmountAfterCoupon = money0(amountAfterCoupon);

		if (safeAmountAfterCoupon.compareTo(FREE_SHIP_THRESHOLD) >= 0) {
			return BigDecimal.ZERO;
		}

		String method = normalizeShippingMethod(shippingMethod);

		if (province == null || province.isBlank()) {
			return money0(submittedShippingFee);
		}

		BigDecimal baseFee;

		switch (method) {
			case SHIPPING_FAST:
				baseFee = new BigDecimal("35000");
				break;
			case SHIPPING_EXPRESS:
				baseFee = new BigDecimal("50000");
				break;
			case SHIPPING_ECONOMY:
			default:
				baseFee = new BigDecimal("20000");
				break;
		}

		BigDecimal areaExtraFee = isHcmCity(province)
				? BigDecimal.ZERO
				: new BigDecimal("15000");

		return money0(baseFee.add(areaExtraFee));
	}

	private boolean isHcmCity(String province) {

		if (province == null || province.isBlank()) {
			return false;
		}

		String value = province.trim().toLowerCase();

		return value.contains("hồ chí minh")
				|| value.contains("ho chi minh")
				|| value.contains("tp. hcm")
				|| value.contains("tphcm")
				|| value.contains("thành phố hồ chí minh");
	}

	/*
	 * =========================
	 * ORDER ITEM / STOCK HELPERS
	 * =========================
	 */

	private void createOrderItemsAndUpdateStock(Connection conn,
	                                            int orderId,
	                                            Map<String, CartItem> cart) throws Exception {

		String updateStockSql = "UPDATE store_product SET stock = stock - ? WHERE id = ?";

		for (CartItem item : cart.values()) {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrderId(orderId);
			orderItem.setProductId(item.getProductId());
			orderItem.setPrice(item.getPrice());
			orderItem.setQuantity(item.getQuantity());

			itemDAO.create(conn, orderItem);

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

	private BigDecimal calculateCartSubtotal(Map<String, CartItem> cart) {

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