package com.webshop.app.dao;

import com.webshop.app.model.*;
import com.webshop.app.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlashSaleItemDAO extends DBConnection {

    public List<FlashSaleItem> findByFlashSale(int flashSaleId) {
        List<FlashSaleItem> list = new ArrayList<>();

        // Sử dụng alias để phân biệt rõ cột id của bảng nào
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
            p.image,
            p.is_active,
            p.category_id,
            p.brand_id
        FROM flash_sale_items fsi
        JOIN store_product p ON p.id = fsi.product_id
        WHERE fsi.flash_sale_id = ?
    """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, flashSaleId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FlashSaleItem item = new FlashSaleItem();

                // Lấy id của FlashSaleItem qua alias
                item.setId(rs.getInt("item_id"));
                item.setFlashSaleId(rs.getInt("flash_sale_id"));
                item.setFlashPrice(rs.getDouble("flash_price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setSoldQuantity(rs.getInt("sold_quantity"));

                Product p = new Product();

                // Lấy id của Product qua alias
                p.setId(rs.getInt("product_id"));
                p.setTitle(rs.getString("title"));
                p.setSlug(rs.getString("slug"));
                p.setDescription(rs.getString("description"));

                // Dùng getBigDecimal cho chính xác kiểu dữ liệu (nếu model dùng BigDecimal)
                p.setPrice(rs.getBigDecimal("price"));

                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setStock(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                p.setActive(rs.getBoolean("is_active")); // Đảm bảo tên cột trong DB khớp với "is_active"
                p.setCategoryId(rs.getLong("category_id"));
                p.setBrandId(rs.getLong("brand_id"));

                item.setProduct(p);
                list.add(item);
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
                     getConnection().prepareStatement(
                             "DELETE FROM flash_sale_items WHERE id=?"
                     )) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}