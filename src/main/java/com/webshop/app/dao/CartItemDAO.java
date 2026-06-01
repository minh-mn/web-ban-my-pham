package com.webshop.app.dao;

import com.webshop.app.model.CartItem;
import com.webshop.app.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartItemDAO {

    /**
     * Lấy giỏ hàng đã lưu của user từ database và dựng lại CartItem đầy đủ để JSP hiển thị.
     * Giá, tên, ảnh, tồn kho luôn lấy theo dữ liệu sản phẩm hiện tại để tránh lưu giá cũ trong DB.
     */
    public Map<String, CartItem> findByUserId(int userId) {
        Map<String, CartItem> cart = new LinkedHashMap<>();

        if (userId <= 0) {
            return cart;
        }

        String sql =
                "SELECT ci.product_id, COALESCE(ci.variant_id, 0) AS variant_id, ci.quantity, " +
                        "p.title, p.price, p.discount_percent, p.stock AS product_stock, p.image, " +
                        "v.id AS v_id, v.size AS v_size, v.type AS v_type, " +
                        "v.extra_price AS v_extra_price, v.stock AS v_stock, v.active AS v_active " +
                        "FROM cart_items ci " +
                        "JOIN store_product p ON p.id = ci.product_id " +
                        "LEFT JOIN store_product_variant v " +
                        "ON v.id = ci.variant_id AND v.product_id = ci.product_id " +
                        "WHERE ci.user_id = ? AND p.is_active = 1 " +
                        "ORDER BY ci.updated_at DESC, ci.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = mapCartItem(rs);

                    if (item == null) {
                        continue;
                    }

                    cart.put(item.getCartKey(), item);
                }
            }

            return cart;

        } catch (SQLException e) {
            throw new RuntimeException("CartItemDAO.findByUserId error", e);
        }
    }

    /**
     * Ghi lại toàn bộ giỏ hàng session của user xuống database.
     * Cách làm delete + insert giúp database luôn giống session sau khi tăng/giảm/xóa/đổi biến thể.
     */
    public void replaceByUserId(int userId, Map<String, CartItem> cart) {
        if (userId <= 0) {
            return;
        }

        String deleteSql = "DELETE FROM cart_items WHERE user_id = ?";
        String insertSql =
                "INSERT INTO cart_items (user_id, product_id, variant_id, quantity, updated_at) " +
                        "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setInt(1, userId);
                deletePs.executeUpdate();
            }

            if (cart != null && !cart.isEmpty()) {
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    for (CartItem item : cart.values()) {
                        if (!isValidItem(item)) {
                            continue;
                        }

                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, item.getProductId());
                        insertPs.setLong(3, Math.max(item.getVariantId(), 0));
                        insertPs.setInt(4, Math.max(item.getQuantity(), 1));
                        insertPs.addBatch();
                    }

                    insertPs.executeBatch();
                }
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("CartItemDAO.replaceByUserId error", e);
        }
    }

    /**
     * Lưu/cập nhật một dòng giỏ hàng.
     * Có thể dùng nếu sau này muốn tối ưu thay vì replace toàn bộ.
     */
    public void upsertItem(int userId, CartItem item) {
        if (userId <= 0 || !isValidItem(item)) {
            return;
        }

        String sql =
                "INSERT INTO cart_items (user_id, product_id, variant_id, quantity, updated_at) " +
                        "VALUES (?, ?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), updated_at = NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, item.getProductId());
            ps.setLong(3, Math.max(item.getVariantId(), 0));
            ps.setInt(4, Math.max(item.getQuantity(), 1));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CartItemDAO.upsertItem error", e);
        }
    }

    public void deleteItem(int userId, int productId, int variantId) {
        if (userId <= 0 || productId <= 0) {
            return;
        }

        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ? AND variant_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setLong(3, Math.max(variantId, 0));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CartItemDAO.deleteItem error", e);
        }
    }

    public void deleteByKeys(int userId, Collection<String> keys) {
        if (userId <= 0 || keys == null || keys.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ? AND variant_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (String key : keys) {
                CartKey cartKey = parseCartKey(key);

                if (cartKey == null) {
                    continue;
                }

                ps.setInt(1, userId);
                ps.setInt(2, cartKey.productId);
                ps.setLong(3, cartKey.variantId);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("CartItemDAO.deleteByKeys error", e);
        }
    }

    public void clearByUserId(int userId) {
        if (userId <= 0) {
            return;
        }

        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CartItemDAO.clearByUserId error", e);
        }
    }

    private CartItem mapCartItem(ResultSet rs) throws SQLException {
        int productId = rs.getInt("product_id");
        int variantId = rs.getInt("variant_id");
        int savedQuantity = rs.getInt("quantity");

        boolean hasVariant = variantId > 0;

        if (hasVariant) {
            Object realVariantId = rs.getObject("v_id");
            boolean variantActive = rs.getBoolean("v_active");

            if (realVariantId == null || !variantActive) {
                return null;
            }
        }

        int stock = hasVariant ? rs.getInt("v_stock") : rs.getInt("product_stock");

        if (stock <= 0) {
            return null;
        }

        int quantity = Math.max(savedQuantity, 1);

        if (quantity > stock) {
            quantity = stock;
        }

        BigDecimal productPrice = safeMoney(rs.getBigDecimal("price"));
        BigDecimal finalProductPrice = calculateFinalPrice(
                productPrice,
                rs.getInt("discount_percent")
        );
        BigDecimal variantExtraPrice = hasVariant
                ? safeMoney(rs.getBigDecimal("v_extra_price"))
                : BigDecimal.ZERO;

        BigDecimal originalUnitPrice = productPrice.add(variantExtraPrice);
        BigDecimal finalUnitPrice = finalProductPrice.add(variantExtraPrice);

        CartItem item = new CartItem();
        item.setCartKey(buildCartKey(productId, variantId));
        item.setProductId(productId);
        item.setTitle(rs.getString("title"));
        item.setQuantity(quantity);
        item.setPrice(finalUnitPrice);
        item.setOriginalPrice(originalUnitPrice);
        item.setImageUrl(rs.getString("image"));
        item.setStock(stock);

        if (hasVariant) {
            String size = rs.getString("v_size");
            String type = rs.getString("v_type");

            item.setVariantId(variantId);
            item.setVariantSize(size);
            item.setVariantType(type);
            item.setVariantName(buildVariantDisplayName(size, type));
            item.setVariantExtraPrice(variantExtraPrice);
        }

        return item;
    }

    private boolean isValidItem(CartItem item) {
        return item != null
                && item.getProductId() > 0
                && item.getQuantity() > 0;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal calculateFinalPrice(BigDecimal price, int discountPercent) {
        BigDecimal safePrice = safeMoney(price);

        if (discountPercent <= 0) {
            return safePrice;
        }

        if (discountPercent >= 100) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = BigDecimal.valueOf(100 - discountPercent);
        return safePrice.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String buildVariantDisplayName(String size, String type) {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasType = type != null && !type.isBlank();

        if (hasSize && hasType) {
            return size + " - " + type;
        }

        if (hasSize) {
            return size;
        }

        if (hasType) {
            return type;
        }

        return "Mặc định";
    }

    private String buildCartKey(int productId, int variantId) {
        return productId + ":" + Math.max(variantId, 0);
    }

    private CartKey parseCartKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String[] parts = key.trim().split(":");

        if (parts.length == 0) {
            return null;
        }

        try {
            int productId = Integer.parseInt(parts[0]);
            int variantId = 0;

            if (parts.length > 1) {
                variantId = Integer.parseInt(parts[1]);
            }

            if (productId <= 0) {
                return null;
            }

            return new CartKey(productId, Math.max(variantId, 0));

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class CartKey {
        private final int productId;
        private final int variantId;

        private CartKey(int productId, int variantId) {
            this.productId = productId;
            this.variantId = variantId;
        }
    }
}