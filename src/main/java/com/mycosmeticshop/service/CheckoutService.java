package com.mycosmeticshop.service;

import com.mycosmeticshop.dao.CouponDAO;
import com.mycosmeticshop.dao.OrderDAO;
import com.mycosmeticshop.dao.OrderItemDAO;
import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.model.Coupon;
import com.mycosmeticshop.model.Order;
import com.mycosmeticshop.model.OrderItem;
import com.mycosmeticshop.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Map;

public class CheckoutService {

	private final OrderDAO orderDAO = new OrderDAO();
	private final OrderItemDAO itemDAO = new OrderItemDAO();
	private final CouponDAO couponDAO = new CouponDAO();

	public int checkout(int userId, Map<Integer, CartItem> cart, String fullName, String phone, String address,
			String paymentMethod, String couponCode) {

		if (userId <= 0)
			throw new IllegalArgumentException("Invalid userId");
		if (cart == null || cart.isEmpty())
			throw new IllegalArgumentException("Cart is empty");
		if (paymentMethod == null || paymentMethod.isBlank())
			paymentMethod = "COD";

		boolean isCod = "COD".equalsIgnoreCase(paymentMethod);
		boolean isVnp = "VNPAY".equalsIgnoreCase(paymentMethod);

		BigDecimal subtotal = cart.values().stream().map(CartItem::getSubtotal).filter(x -> x != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal total = subtotal;

		Coupon coupon = null;
		if (couponCode != null && !couponCode.isBlank()) {
			coupon = couponDAO.findByCode(couponCode.trim());
			if (coupon != null && coupon.isValid(LocalDate.now())) {
				BigDecimal discount = calculateDiscountFromCoupon(coupon, subtotal);
				total = total.subtract(discount);
			}
		}

		total = total.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			// ✅ Bảo vệ FK: userId phải tồn tại trong dbo.users(id)
			if (!existsUsersId(conn, userId)) {
				conn.rollback();
				throw new IllegalStateException("Invalid session userId (not found in dbo.users): " + userId);
			}

			// 1) Lock & check stock
			String lockSql = "SELECT stock FROM store_product WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
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

			// 2) Create order
			Order o = new Order();
			o.setUserId(userId);
			o.setFullName(fullName);
			o.setPhone(phone);
			o.setAddress(address);
			o.setTotal(total);
			o.setPaymentMethod(paymentMethod);

			if (isCod) {
				o.setPaymentStatus("PENDING");
				o.setStatus("CONFIRMED");
			} else if (isVnp) {
				o.setPaymentStatus("PENDING");
				o.setStatus("PENDING");
			} else {
				o.setPaymentStatus("PENDING");
				o.setStatus("PENDING");
			}

			int orderId = orderDAO.create(conn, o);

			// 3) COD: items + stock - + coupon used
			if (isCod) {
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

		if (orderId <= 0)
			throw new IllegalArgumentException("Invalid orderId");
		if (cart == null || cart.isEmpty())
			throw new IllegalArgumentException("Cart is empty");

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			// ✅ Dùng cùng transaction (nếu bạn đã thêm overload
			// OrderDAO.findById(conn,...))
			Order o;
			try {
				o = orderDAO.findById(conn, orderId);
			} catch (Exception ignore) {
				// fallback nếu bạn chưa kịp thêm overload
				o = orderDAO.findById(orderId);
			}

			if (o == null) {
				conn.rollback();
				throw new RuntimeException("Order not found: " + orderId);
			}

			// ✅ IDempotent: nếu đã có items thì coi như finalize xong (kể cả PAID đến từ
			// IPN trước đó)
			boolean hasItems = itemDAO.existsByOrderId(conn, orderId);

			if (hasItems) {
				// nếu đã có items, đảm bảo trạng thái cuối cùng đúng
				// (trong trường hợp items có nhưng status chưa cập nhật)
				if (!"PAID".equalsIgnoreCase(o.getPaymentStatus())) {
					try {
						orderDAO.updatePaymentStatus(conn, orderId, "PAID", "CONFIRMED", o.getVnpTxnRef());
					} catch (Exception ignore) {
						orderDAO.updatePaymentStatus(orderId, "PAID", "CONFIRMED", o.getVnpTxnRef());
					}
					conn.commit();
					return;
				}

				conn.rollback();
				return;
			}

			// ===== LOCK & CHECK STOCK =====
			String lockSql = "SELECT stock FROM store_product WITH (UPDLOCK, ROWLOCK) WHERE id = ?";

			for (CartItem item : cart.values()) {
				try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
					ps.setInt(1, item.getProductId());
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next() || rs.getInt("stock") < item.getQuantity()) {
							conn.rollback();
							throw new RuntimeException(
									"Không đủ tồn kho khi finalize VNPAY cho sản phẩm ID " + item.getProductId());
						}
					}
				}
			}

			String updateStockSql = "UPDATE store_product SET stock = stock - ? WHERE id = ?";

			// ===== INSERT ITEMS + UPDATE STOCK =====
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

			// ===== COUPON USED =====
			if (couponCode != null && !couponCode.isBlank()) {
				Coupon coupon = couponDAO.findByCode(couponCode.trim());
				if (coupon != null && coupon.isValid(LocalDate.now())) {
					couponDAO.increaseUsedCount(conn, coupon.getId());
				}
			}

			// ===== UPDATE ORDER PAID/CONFIRMED =====
			try {
				orderDAO.updatePaymentStatus(conn, orderId, "PAID", "CONFIRMED", o.getVnpTxnRef());
			} catch (Exception ignore) {
				orderDAO.updatePaymentStatus(orderId, "PAID", "CONFIRMED", o.getVnpTxnRef());
			}

			conn.commit();

		} catch (Exception e) {
			throw new RuntimeException("Finalize VNPAY failed", e);
		}
	}

	public BigDecimal calculateCouponDiscount(String couponCode, BigDecimal subTotal) {

		if (couponCode == null || couponCode.isBlank())
			return null;
		if (subTotal == null || subTotal.compareTo(BigDecimal.ZERO) <= 0)
			return null;

		Coupon coupon = couponDAO.findByCode(couponCode.trim());
		if (coupon == null)
			return null;
		if (!coupon.isValid(LocalDate.now()))
			return null;

		BigDecimal discount = calculateDiscountFromCoupon(coupon, subTotal);
		if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0)
			return null;

		return discount.setScale(0, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateDiscountFromCoupon(Coupon coupon, BigDecimal subTotal) {

		if (coupon == null || subTotal == null)
			return BigDecimal.ZERO;

		BigDecimal percent = BigDecimal.valueOf(coupon.getDiscountPercent());

		BigDecimal discount = subTotal.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		if (coupon.getMaxDiscountAmount() != null) {
			discount = discount.min(coupon.getMaxDiscountAmount());
		}

		if (discount.compareTo(BigDecimal.ZERO) < 0)
			discount = BigDecimal.ZERO;
		return discount;
	}

	private boolean existsUsersId(Connection conn, int userId) {
		String sql = "SELECT 1 FROM dbo.users WHERE id = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			return false;
		}
	}
}
