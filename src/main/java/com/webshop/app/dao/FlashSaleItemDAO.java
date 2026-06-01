package com.webshop.app.dao;

import com.webshop.app.model.FlashSaleItem;
import com.webshop.app.model.Product;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleItemDAO {

    private static final int DEFAULT_MAX_QUANTITY_PER_USER = 2;

    public List<FlashSaleItem> findByFlashSale(int flashSaleId) {
        List<FlashSaleItem> list = new ArrayList<>();

        if (flashSaleId <= 0) {
            return list;
        }

        String sql = """
            SELECT
                fsi.id AS item_id,
                fsi.flash_sale_id,
                fsi.flash_price,
                fsi.quantity,
                fsi.sold_quantity,
                COALESCE(fsi.max_quantity_per_user, 2) AS max_quantity_per_user,
                p.id AS product_id,
                p.title,
                p.slug,
                p.description,
                p.price,
                p.discount_percent,
                p.stock,
                COALESCE(NULLIF(TRIM(p.image), ''), first_img.image) AS product_image,
                p.is_active,
                p.category_id,
                p.brand_id
            FROM flash_sale_items fsi
            JOIN store_product p ON p.id = fsi.product_id
            LEFT JOIN (
                SELECT spi.product_id, spi.image
                FROM store_productimage spi
                INNER JOIN (
                    SELECT product_id, MIN(id) AS min_id
                    FROM store_productimage
                    GROUP BY product_id
                ) x ON x.product_id = spi.product_id AND x.min_id = spi.id
            ) first_img ON first_img.product_id = p.id
            WHERE fsi.flash_sale_id = ?
              AND p.is_active = 1
            ORDER BY fsi.id ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, flashSaleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("FlashSaleItemDAO.findByFlashSale error", e);
        }

        return list;
    }

    /*
     * Issue 139:
     * Lấy Flash Sale item đang chạy theo productId để Cart/Checkout kiểm tra giới hạn mua.
     */
    public FlashSaleItem findActiveByProductId(int productId) {
        if (productId <= 0) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection()) {
            return findActiveByProductId(conn, productId, false);
        } catch (SQLException e) {
            throw new RuntimeException("FlashSaleItemDAO.findActiveByProductId error", e);
        }
    }

    public FlashSaleItem findActiveByProductId(Connection conn, int productId) throws SQLException {
        return findActiveByProductId(conn, productId, false);
    }

    /*
     * Dùng trong transaction checkout nếu cần khóa dòng Flash Sale item
     * để tránh oversell khi nhiều user đặt hàng cùng lúc.
     */
    public FlashSaleItem lockActiveByProductId(Connection conn, int productId) throws SQLException {
        return findActiveByProductId(conn, productId, true);
    }

    private FlashSaleItem findActiveByProductId(Connection conn,
                                                int productId,
                                                boolean forUpdate) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (productId <= 0) {
            return null;
        }

        String sql = """
            SELECT
                fsi.id AS item_id,
                fsi.flash_sale_id,
                fsi.flash_price,
                fsi.quantity,
                fsi.sold_quantity,
                COALESCE(fsi.max_quantity_per_user, 2) AS max_quantity_per_user,
                p.id AS product_id,
                p.title,
                p.slug,
                p.description,
                p.price,
                p.discount_percent,
                p.stock,
                COALESCE(NULLIF(TRIM(p.image), ''), first_img.image) AS product_image,
                p.is_active,
                p.category_id,
                p.brand_id
            FROM flash_sale_items fsi
            JOIN flash_sales fs ON fs.id = fsi.flash_sale_id
            JOIN store_product p ON p.id = fsi.product_id
            LEFT JOIN (
                SELECT spi.product_id, spi.image
                FROM store_productimage spi
                INNER JOIN (
                    SELECT product_id, MIN(id) AS min_id
                    FROM store_productimage
                    GROUP BY product_id
                ) x ON x.product_id = spi.product_id AND x.min_id = spi.id
            ) first_img ON first_img.product_id = p.id
            WHERE fsi.product_id = ?
              AND fs.active = 1
              AND NOW() BETWEEN fs.start_time AND fs.end_time
              AND p.is_active = 1
            ORDER BY fs.end_time ASC, fsi.id ASC
            LIMIT 1
        """ + (forUpdate ? " FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapItem(rs);
                }
            }
        }

        return null;
    }

    /*
     * Issue 139:
     * Đếm số lượng user đã mua sản phẩm Flash Sale này trong đúng khung giờ Flash Sale.
     * Không tính đơn đã hủy hoặc thanh toán thất bại/hủy.
     */
    public int countPurchasedQuantityForUser(int userId, int flashSaleId, int productId) {
        try (Connection conn = DBConnection.getConnection()) {
            return countPurchasedQuantityForUser(conn, userId, flashSaleId, productId);
        } catch (SQLException e) {
            throw new RuntimeException("FlashSaleItemDAO.countPurchasedQuantityForUser error", e);
        }
    }

    public int countPurchasedQuantityForUser(Connection conn,
                                             int userId,
                                             int flashSaleId,
                                             int productId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (userId <= 0 || flashSaleId <= 0 || productId <= 0) {
            return 0;
        }

        String sql = """
            SELECT COALESCE(SUM(oi.quantity), 0) AS purchased_qty
            FROM store_orderitem oi
            JOIN store_order o ON o.id = oi.order_id
            JOIN flash_sales fs ON fs.id = ?
            WHERE o.user_id = ?
              AND oi.product_id = ?
              AND o.created_at BETWEEN fs.start_time AND fs.end_time
              AND LOWER(COALESCE(o.status, 'processing')) NOT IN ('cancelled', 'canceled')
              AND UPPER(COALESCE(o.payment_status, 'PENDING')) NOT IN ('FAILED', 'CANCELED', 'CANCELLED')
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flashSaleId);
            ps.setInt(2, userId);
            ps.setInt(3, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.max(rs.getInt("purchased_qty"), 0) : 0;
            }
        }
    }

    /*
     * Issue 139:
     * Sau khi checkout trừ kho thành công, cập nhật số lượng đã bán của Flash Sale item.
     */
    public int increaseSoldQuantityIfRunning(Connection conn, int productId, int quantity) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection must not be null");
        }

        if (productId <= 0 || quantity <= 0) {
            return 0;
        }

        String sql = """
            UPDATE flash_sale_items fsi
            JOIN flash_sales fs ON fs.id = fsi.flash_sale_id
            SET fsi.sold_quantity = LEAST(fsi.quantity, fsi.sold_quantity + ?)
            WHERE fsi.product_id = ?
              AND fs.active = 1
              AND NOW() BETWEEN fs.start_time AND fs.end_time
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate();
        }
    }

    public void insert(FlashSaleItem item) {
        if (item == null || item.getProduct() == null || item.getProduct().getId() <= 0) {
            throw new IllegalArgumentException("FlashSaleItem product is required");
        }

        String sql = """
            INSERT INTO flash_sale_items
            (flash_sale_id, product_id, flash_price, quantity, sold_quantity, max_quantity_per_user)
            VALUES (?,?,?,?,?,?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getFlashSaleId());
            ps.setLong(2, item.getProduct().getId());
            ps.setDouble(3, item.getFlashPrice());
            ps.setInt(4, item.getQuantity());
            ps.setInt(5, item.getSoldQuantity());
            ps.setInt(6, safeLimit(item.getMaxQuantityPerUser()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("FlashSaleItemDAO.insert error", e);
        }
    }

    public void delete(int id) {
        if (id <= 0) {
            return;
        }

        String sql = "DELETE FROM flash_sale_items WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("FlashSaleItemDAO.delete error", e);
        }
    }

    private FlashSaleItem mapItem(ResultSet rs) throws SQLException {
        FlashSaleItem item = new FlashSaleItem();

        item.setId(rs.getInt("item_id"));
        item.setFlashSaleId(rs.getInt("flash_sale_id"));
        item.setFlashPrice(rs.getDouble("flash_price"));
        item.setQuantity(rs.getInt("quantity"));
        item.setSoldQuantity(rs.getInt("sold_quantity"));
        item.setMaxQuantityPerUser(safeLimit(rs.getInt("max_quantity_per_user")));

        Product product = new Product();
        product.setId(rs.getInt("product_id"));
        product.setTitle(rs.getString("title"));
        product.setSlug(rs.getString("slug"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setDiscountPercent(rs.getInt("discount_percent"));
        product.setStock(rs.getInt("stock"));
        product.setImage(rs.getString("product_image"));
        product.setActive(rs.getBoolean("is_active"));
        product.setCategoryId(rs.getLong("category_id"));
        product.setBrandId(rs.getLong("brand_id"));

        /*
         * Giúp JSP/Product card dùng đúng giá Flash Sale và % đã bán.
         */
        product.setFinalPrice(java.math.BigDecimal.valueOf(item.getFlashPrice()));
        product.setSoldQuantity(item.getSoldQuantity());

        item.setProduct(product);
        return item;
    }

    private int safeLimit(int value) {
        return value > 0 ? value : DEFAULT_MAX_QUANTITY_PER_USER;
    }
}
