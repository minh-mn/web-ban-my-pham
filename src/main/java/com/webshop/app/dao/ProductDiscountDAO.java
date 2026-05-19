package com.webshop.app.dao;

import com.webshop.app.model.DiscountType;
import com.webshop.app.model.ProductDiscount;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDiscountDAO {

    public ProductDiscount findActiveByProductId(int productId) {

        String sql =
                "SELECT * " +
                        "FROM store_productdiscount " +
                        "WHERE product_id = ? " +
                        "  AND is_active = 1 " +
                        "  AND start_date <= CURDATE() " +
                        "  AND end_date >= CURDATE() " +
                        "ORDER BY discount_value DESC " +
                        "LIMIT 1";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                ProductDiscount d = new ProductDiscount();

                d.setId(rs.getInt("id"));
                d.setProductId(rs.getInt("product_id"));
                d.setDiscountType(
                        DiscountType.valueOf(rs.getString("discount_type"))
                );
                d.setDiscountValue(rs.getBigDecimal("discount_value"));
                d.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));

                if (rs.getDate("start_date") != null) {
                    d.setStartDate(rs.getDate("start_date").toLocalDate());
                }

                if (rs.getDate("end_date") != null) {
                    d.setEndDate(rs.getDate("end_date").toLocalDate());
                }

                d.setActive(rs.getBoolean("is_active"));

                return d;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductDiscountDAO.findActiveByProductId error", e);
        }
    }
}