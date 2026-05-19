package com.webshop.app.service;

import com.webshop.app.model.DiscountType;
import com.webshop.app.model.Product;
import com.webshop.app.model.PromotionEvent;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PromotionEventService {

    public PromotionEvent findBestForProduct(Product product) {

        String sql =
                "SELECT * " +
                        "FROM store_promotionevent " +
                        "WHERE is_active = 1 " +
                        "  AND start_date <= CURDATE() " +
                        "  AND end_date >= CURDATE() " +
                        "  AND ( " +
                        "       scope = 'ALL' " +
                        "    OR (scope = 'CATEGORY' AND category_id = ?) " +
                        "    OR (scope = 'BRAND' AND brand_id = ?) " +
                        "  ) " +
                        "ORDER BY discount_value DESC " +
                        "LIMIT 1";

        Integer categoryId = product.getCategory() != null
                ? product.getCategory().getId()
                : null;

        Integer brandId = product.getBrand() != null
                ? product.getBrand().getId()
                : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (categoryId != null) {
                ps.setInt(1, categoryId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (brandId != null) {
                ps.setInt(2, brandId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapRow(rs);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("PromotionEventService error", ex);
        }
    }

    private PromotionEvent mapRow(ResultSet rs) throws SQLException {
        PromotionEvent event = new PromotionEvent();

        event.setId(rs.getInt("id"));
        event.setName(rs.getString("name"));

        event.setScope(
                PromotionEvent.Scope.valueOf(rs.getString("scope"))
        );

        event.setDiscountType(
                DiscountType.valueOf(rs.getString("discount_type"))
        );

        event.setDiscountValue(rs.getBigDecimal("discount_value"));
        event.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));

        event.setCategoryId(
                rs.getObject("category_id") != null
                        ? rs.getInt("category_id")
                        : null
        );

        event.setBrandId(
                rs.getObject("brand_id") != null
                        ? rs.getInt("brand_id")
                        : null
        );

        if (rs.getDate("start_date") != null) {
            event.setStartDate(rs.getDate("start_date").toLocalDate());
        }

        if (rs.getDate("end_date") != null) {
            event.setEndDate(rs.getDate("end_date").toLocalDate());
        }

        event.setActive(rs.getBoolean("is_active"));

        return event;
    }
}