package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.ProductDiscount;
import com.mycosmeticshop.model.DiscountType;   // 🔴 THÊM
import com.mycosmeticshop.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDiscountDAO {

    public ProductDiscount findActiveByProductId(int productId) {

        String sql =
            "SELECT TOP 1 * " +
            "FROM store_productdiscount " +
            "WHERE product_id = ? " +
            "  AND is_active = 1 " +
            "  AND start_date <= CAST(GETDATE() AS DATE) " +
            "  AND end_date   >= CAST(GETDATE() AS DATE)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            ProductDiscount d = new ProductDiscount();
            d.setId(rs.getInt("id"));
            d.setProductId(rs.getInt("product_id"));
            d.setDiscountType(
                DiscountType.valueOf(
                    rs.getString("discount_type")
                )
            );
            d.setDiscountValue(rs.getBigDecimal("discount_value"));
            d.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
            d.setStartDate(rs.getDate("start_date").toLocalDate());
            d.setEndDate(rs.getDate("end_date").toLocalDate());
            d.setActive(rs.getBoolean("is_active"));

            return d;

        } catch (SQLException e) {
            throw new RuntimeException("ProductDiscountDAO error", e);
        }
    }
}
