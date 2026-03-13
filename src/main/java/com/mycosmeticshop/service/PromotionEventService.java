package com.mycosmeticshop.service;

import com.mycosmeticshop.model.Product;
import com.mycosmeticshop.model.PromotionEvent;
import com.mycosmeticshop.model.DiscountType;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PromotionEventService {

    /**
     * Lấy PromotionEvent phù hợp nhất cho 1 product
     * Scope: ALL / CATEGORY / BRAND
     * Ưu tiên discount_value cao nhất
     */
    public PromotionEvent findBestForProduct(Product product) {

        String sql =
            "SELECT TOP 1 * " +
            "FROM store_promotionevent " +
            "WHERE is_active = 1 " +
            "  AND start_date <= CAST(GETDATE() AS DATE) " +
            "  AND end_date   >= CAST(GETDATE() AS DATE) " +
            "  AND ( " +
            "       scope = 'ALL' " +
            "    OR (scope = 'CATEGORY' AND category_id = ?) " +
            "    OR (scope = 'BRAND' AND brand_id = ?) " +
            "  ) " +
            "ORDER BY discount_value DESC";

        Integer categoryId =
                product.getCategory() != null
                        ? product.getCategory().getId()
                        : null;

        Integer brandId =
                product.getBrand() != null
                        ? product.getBrand().getId()
                        : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // CATEGORY
            if (categoryId != null) {
                ps.setInt(1, categoryId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            // BRAND
            if (brandId != null) {
                ps.setInt(2, brandId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            PromotionEvent e = new PromotionEvent();
            e.setId(rs.getInt("id"));
            e.setName(rs.getString("name"));
            e.setScope(
                PromotionEvent.Scope.valueOf(
                    rs.getString("scope")
                )
            );
            e.setDiscountType(
                DiscountType.valueOf(
                    rs.getString("discount_type")
                )
            );
            e.setDiscountValue(rs.getBigDecimal("discount_value"));
            e.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
            e.setCategoryId(
                rs.getObject("category_id") != null
                        ? rs.getInt("category_id")
                        : null
            );
            e.setBrandId(
                rs.getObject("brand_id") != null
                        ? rs.getInt("brand_id")
                        : null
            );
            e.setStartDate(rs.getDate("start_date").toLocalDate());
            e.setEndDate(rs.getDate("end_date").toLocalDate());
            e.setActive(rs.getBoolean("is_active"));

            return e;

        } catch (SQLException ex) {
            throw new RuntimeException("PromotionEventService error", ex);
        }
    }
}
