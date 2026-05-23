package com.webshop.app.service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

		/*
		 * Rank hiện tại của user.
		 * Ưu tiên users.manual_rank_code nếu admin đã gán thủ công.
		 * Nếu không có manual rank thì tự tính theo tổng chi tiêu.
		 */
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
		 * Quan trọng: tính theo userRankCode đã xét manual_rank_code.
		 */
		BigDecimal rankDiscount = calculateRankDiscountByRankCode(userRankCode, amountAfterCoupon);

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
			 * Nếu item có variantId:
			 * - khóa store_product_variant
			 * - kiểm tra stock của variant
			 *
			 * Nếu item không có variantId:
			 * - khóa store_product
			 * - kiểm tra stock của product
			 */
			lockAndValidateStock(conn, cart);

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

			/*
			 * VNPAY finalize cũng phải kiểm tra đúng variant stock.
			 */
			lockAndValidateStock(conn, cart);

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

	/*
	 * Method public cũ: giữ lại để các file khác đang gọi không lỗi.
	 * Nay đã tính discount theo manual_rank_code nếu admin có gán.
	 */
	public BigDecimal calculateRankDiscount(int userId, BigDecimal amountAfterCoupon) {

		if (userId <= 0) {
			return BigDecimal.ZERO;
		}

		String userRankCode = resolveUserRankCode(userId);

		return calculateRankDiscountByRankCode(userRankCode, amountAfterCoupon);
	}

	public BigDecimal calculateTotalAfterCouponAndRank(int userId,
	                                                   BigDecimal subTotal,
	                                                   String couponCode) {

		BigDecimal safeSubTotal = money0(subTotal);
		String userRankCode = resolveUserRankCode(userId);

		BigDecimal couponDiscount = calculateCouponDiscountByRank(couponCode, safeSubTotal, userRankCode);
		BigDecimal amountAfterCoupon = safeSubTotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		BigDecimal rankDiscount = calculateRankDiscountByRankCode(userRankCode, amountAfterCoupon);
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
		String userRankCode = resolveUserRankCode(userId);

		BigDecimal couponDiscount = calculateCouponDiscountByRank(couponCode, safeSubTotal, userRankCode);
		BigDecimal amountAfterCoupon = safeSubTotal.subtract(couponDiscount);

		if (amountAfterCoupon.compareTo(BigDecimal.ZERO) < 0) {
			amountAfterCoupon = BigDecimal.ZERO;
		}

		BigDecimal rankDiscount = calculateRankDiscountByRankCode(userRankCode, amountAfterCoupon);
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

	public String resolveEffectiveRankCode(int userId) {
		return resolveUserRankCode(userId);
	}

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

		/*
		 * Ưu tiên 1:
		 * Rank admin gán thủ công trong users.manual_rank_code.
		 */
		String manualRankCode = findManualRankCode(conn, userId);

		if (manualRankCode != null && !manualRankCode.isBlank()) {
			return normalizeRankCode(manualRankCode);
		}

		/*
		 * Ưu tiên 2:
		 * Nếu không có manual rank thì tự tính theo tổng chi tiêu.
		 */
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
			/*
			 * Nếu database cũ chưa có manual_rank_code
			 * thì fallback về rank theo chi tiêu.
			 */
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

	private BigDecimal findRankDiscountPercentByCode(String rankCode) {

		String sql = """
                SELECT discount_percent
                FROM store_rank
                WHERE code = ?
                  AND is_active = 1
                LIMIT 1
                """;

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, normalizeRankCode(rankCode));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					BigDecimal percent = rs.getBigDecimal("discount_percent");
					return percent == null ? BigDecimal.ZERO : percent;
				}
			}

		} catch (Exception e) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal calculateRankDiscountByRankCode(String rankCode, BigDecimal amountAfterCoupon) {

		BigDecimal safeAmount = money0(amountAfterCoupon);

		if (safeAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal discountPercent = findRankDiscountPercentByCode(rankCode);

		if (discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal discount = safeAmount
				.multiply(discountPercent)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		if (discount.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}

		if (discount.compareTo(safeAmount) > 0) {
			return safeAmount;
		}

		return money0(discount);
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

	private void lockAndValidateStock(Connection conn,
	                                  Map<String, CartItem> cart) throws Exception {

		for (CartItem item : cart.values()) {
			if (item == null) {
				continue;
			}

			int quantity = Math.max(item.getQuantity(), 0);

			if (quantity <= 0) {
				throw new RuntimeException("Số lượng sản phẩm không hợp lệ.");
			}

			Integer variantId = getCartItemVariantId(item);

			if (variantId != null && variantId > 0) {
				lockVariantAndValidateStock(conn, item, variantId, quantity);
			} else {
				lockProductAndValidateStock(conn, item.getProductId(), quantity);
			}
		}
	}

	private void createOrderItemsAndUpdateStock(Connection conn,
	                                            int orderId,
	                                            Map<String, CartItem> cart) throws Exception {

		for (CartItem item : cart.values()) {
			if (item == null) {
				continue;
			}

			int quantity = Math.max(item.getQuantity(), 0);

			if (quantity <= 0) {
				throw new RuntimeException("Số lượng sản phẩm không hợp lệ.");
			}

			Integer variantId = getCartItemVariantId(item);
			VariantSnapshot variantSnapshot = null;

			if (variantId != null && variantId > 0) {
				variantSnapshot = lockVariantAndValidateStock(conn, item, variantId, quantity);
			}

			OrderItem orderItem = new OrderItem();
			orderItem.setOrderId(orderId);
			orderItem.setProductId(item.getProductId());
			orderItem.setPrice(item.getPrice());
			orderItem.setQuantity(quantity);

			if (variantSnapshot != null) {
				orderItem.setVariantId(variantSnapshot.variantId);
				orderItem.setVariantName(variantSnapshot.variantName);
				orderItem.setVariantSize(variantSnapshot.variantSize);
				orderItem.setVariantType(variantSnapshot.variantType);
			}

			itemDAO.create(conn, orderItem);

			if (variantSnapshot != null) {
				updateVariantStock(conn, item.getProductId(), variantSnapshot.variantId, quantity);
				updateProductStockAfterVariantSold(conn, item.getProductId(), quantity);
			} else {
				updateProductStock(conn, item.getProductId(), quantity);
			}
		}
	}

	private void lockProductAndValidateStock(Connection conn,
	                                         int productId,
	                                         int quantity) throws Exception {

		String sql = """
                SELECT stock
                FROM store_product
                WHERE id = ?
                FOR UPDATE
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new RuntimeException("Không tìm thấy sản phẩm ID " + productId);
				}

				int stock = rs.getInt("stock");

				if (stock < quantity) {
					throw new RuntimeException("Không đủ tồn kho cho sản phẩm ID " + productId);
				}
			}
		}
	}

	private VariantSnapshot lockVariantAndValidateStock(Connection conn,
	                                                    CartItem item,
	                                                    int variantId,
	                                                    int quantity) throws Exception {

		String sql = """
                SELECT *
                FROM store_product_variant
                WHERE id = ?
                  AND product_id = ?
                FOR UPDATE
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, variantId);
			ps.setInt(2, item.getProductId());

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					throw new RuntimeException(
							"Không tìm thấy biến thể ID " + variantId
									+ " của sản phẩm ID " + item.getProductId()
					);
				}

				Integer stock = getIntegerByColumns(rs, "stock", "quantity");

				if (stock == null) {
					throw new RuntimeException(
							"Bảng store_product_variant thiếu cột stock hoặc quantity."
					);
				}

				if (stock < quantity) {
					throw new RuntimeException(
							"Không đủ tồn kho cho biến thể ID " + variantId
									+ " của sản phẩm ID " + item.getProductId()
					);
				}

				VariantSnapshot snapshot = new VariantSnapshot();
				snapshot.variantId = variantId;

				/*
				 * Ưu tiên thông tin từ CartItem vì đó là thông tin user đã chọn.
				 * Nếu CartItem không có getter variant thì fallback sang database.
				 */
				snapshot.variantName = firstNotBlank(
						getCartItemString(item, "getVariantName"),
						getStringByColumns(rs, "variant_name", "name", "title")
				);

				snapshot.variantSize = firstNotBlank(
						getCartItemString(item, "getVariantSize"),
						getStringByColumns(rs, "variant_size", "size")
				);

				snapshot.variantType = firstNotBlank(
						getCartItemString(item, "getVariantType"),
						getStringByColumns(rs, "variant_type", "type")
				);

				return snapshot;
			}
		}
	}

	private void updateVariantStock(Connection conn,
	                                int productId,
	                                int variantId,
	                                int quantity) throws Exception {

		String sql = """
                UPDATE store_product_variant
                SET stock = stock - ?
                WHERE id = ?
                  AND product_id = ?
                  AND stock >= ?
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, quantity);
			ps.setInt(2, variantId);
			ps.setInt(3, productId);
			ps.setInt(4, quantity);

			int updated = ps.executeUpdate();

			if (updated <= 0) {
				throw new RuntimeException(
						"Không thể trừ tồn kho biến thể ID " + variantId
								+ " của sản phẩm ID " + productId
				);
			}
		}
	}

	private void updateProductStock(Connection conn,
	                                int productId,
	                                int quantity) throws Exception {

		String sql = """
                UPDATE store_product
                SET stock = stock - ?
                WHERE id = ?
                  AND stock >= ?
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, quantity);
			ps.setInt(2, productId);
			ps.setInt(3, quantity);

			int updated = ps.executeUpdate();

			if (updated <= 0) {
				throw new RuntimeException("Không thể trừ tồn kho sản phẩm ID " + productId);
			}
		}
	}

	/*
	 * Khi bán variant, vẫn cập nhật stock tổng ở store_product để trang danh sách sản phẩm
	 * không bị lệch tồn kho tổng. Không dùng điều kiện stock >= quantity vì tồn kho tổng
	 * đôi khi không đồng bộ chính xác với tổng variant.
	 */
	private void updateProductStockAfterVariantSold(Connection conn,
	                                                int productId,
	                                                int quantity) throws Exception {

		String sql = """
                UPDATE store_product
                SET stock = CASE
                    WHEN stock >= ? THEN stock - ?
                    ELSE 0
                END
                WHERE id = ?
                """;

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, quantity);
			ps.setInt(2, quantity);
			ps.setInt(3, productId);

			int updated = ps.executeUpdate();

			if (updated <= 0) {
				throw new RuntimeException("Không thể cập nhật tồn kho tổng của sản phẩm ID " + productId);
			}
		}
	}

	private Integer getCartItemVariantId(CartItem item) {
		Object value = invokeGetter(item, "getVariantId");

		if (value == null) {
			return null;
		}

		if (value instanceof Integer integerValue) {
			return integerValue > 0 ? integerValue : null;
		}

		if (value instanceof Number numberValue) {
			int intValue = numberValue.intValue();
			return intValue > 0 ? intValue : null;
		}

		try {
			int intValue = Integer.parseInt(value.toString().trim());
			return intValue > 0 ? intValue : null;
		} catch (Exception e) {
			return null;
		}
	}

	private String getCartItemString(CartItem item, String getterName) {
		Object value = invokeGetter(item, getterName);

		if (value == null) {
			return null;
		}

		String text = value.toString().trim();

		return text.isEmpty() ? null : text;
	}

	private Object invokeGetter(Object target, String getterName) {
		if (target == null || getterName == null || getterName.isBlank()) {
			return null;
		}

		try {
			Method method = target.getClass().getMethod(getterName);
			return method.invoke(target);
		} catch (Exception e) {
			return null;
		}
	}

	private String firstNotBlank(String first, String second) {
		if (first != null && !first.trim().isEmpty()) {
			return first.trim();
		}

		if (second != null && !second.trim().isEmpty()) {
			return second.trim();
		}

		return null;
	}

	private Integer getIntegerByColumns(ResultSet rs, String... columnNames) {
		for (String columnName : columnNames) {
			try {
				if (!hasColumn(rs, columnName)) {
					continue;
				}

				int value = rs.getInt(columnName);

				if (rs.wasNull()) {
					continue;
				}

				return value;

			} catch (Exception ignored) {
			}
		}

		return null;
	}

	private String getStringByColumns(ResultSet rs, String... columnNames) {
		for (String columnName : columnNames) {
			try {
				if (!hasColumn(rs, columnName)) {
					continue;
				}

				String value = rs.getString(columnName);

				if (value != null && !value.trim().isEmpty()) {
					return value.trim();
				}

			} catch (Exception ignored) {
			}
		}

		return null;
	}

	private boolean hasColumn(ResultSet rs, String columnName) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))
						|| columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
					return true;
				}
			}

		} catch (Exception ignored) {
		}

		return false;
	}

	private static class VariantSnapshot {
		private int variantId;
		private String variantName;
		private String variantSize;
		private String variantType;
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