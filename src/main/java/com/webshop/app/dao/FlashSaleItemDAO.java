package com.webshop.app.dao;

import com.webshop.app.model.FlashSaleItem;
import com.webshop.app.model.Product;
import com.webshop.app.utils.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleItemDAO extends DBConnection {

    public List<FlashSaleItem> findByFlashSale(int flashSaleId) {
        List<FlashSaleItem> list = new ArrayList<>();

        String sql = """
            SELECT
                fsi.id AS item_id,
                fsi.flash_sale_id,
                fsi.flash_price,
                fsi.quantity,
                fsi.sold_quantity,
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

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, flashSaleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FlashSaleItem item = new FlashSaleItem();
                    item.setId(rs.getInt("item_id"));
                    item.setFlashSaleId(rs.getInt("flash_sale_id"));
                    item.setFlashPrice(rs.getDouble("flash_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSoldQuantity(rs.getInt("sold_quantity"));

                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setTitle(rs.getString("title"));
                    p.setSlug(rs.getString("slug"));
                    p.setDescription(rs.getString("description"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setDiscountPercent(rs.getInt("discount_percent"));
                    p.setStock(rs.getInt("stock"));
                    p.setImage(rs.getString("product_image"));
                    p.setActive(rs.getBoolean("is_active"));
                    p.setCategoryId(rs.getLong("category_id"));
                    p.setBrandId(rs.getLong("brand_id"));

                    item.setProduct(p);
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insert(FlashSaleItem i) {
        String sql = """
            INSERT INTO flash_sale_items
            (flash_sale_id, product_id, flash_price, quantity, sold_quantity)
            VALUES (?,?,?,?,?)
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, i.getFlashSaleId());
            ps.setLong(2, i.getProduct().getId());
            ps.setDouble(3, i.getFlashPrice());
            ps.setInt(4, i.getQuantity());
            ps.setInt(5, i.getSoldQuantity());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps =
                     getConnection().prepareStatement("DELETE FROM flash_sale_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
