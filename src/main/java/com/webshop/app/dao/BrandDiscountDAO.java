package com.webshop.app.dao;

import com.webshop.app.model.BrandDiscount;
import com.webshop.app.model.DiscountType;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BrandDiscountDAO {

    /**
     * Lấy BrandDiscount hợp lệ có discount_value cao nhất
     */
    public BrandDiscount findBestActiveByBrandId(int brandId) {

        String sql = """
                SELECT *
                FROM store_branddiscount
                WHERE brand_id = ?
                  AND is_active = 1
                  AND start_date <= CURDATE()
                  AND end_date >= CURDATE()
                ORDER BY discount_value DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, brandId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet, false);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "BrandDiscountDAO.findBestActiveByBrandId error", e
            );
        }
    }

    // ================= ADMIN CRUD =================

    public List<BrandDiscount> findAll(boolean joinBrand) {

        List<BrandDiscount> discounts = new ArrayList<>();

        String sql = joinBrand
                ? """
                SELECT d.*, b.name AS brand_name
                FROM store_branddiscount d
                JOIN store_brand b ON b.id = d.brand_id
                ORDER BY d.id DESC
                """
                : """
                SELECT *
                FROM store_branddiscount
                ORDER BY id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                discounts.add(mapRow(resultSet, joinBrand));
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findAll error", e);
        }

        return discounts;
    }

    public BrandDiscount findById(int id) {

        String sql = """
                SELECT *
                FROM store_branddiscount
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet, false);
            }

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.findById error", e);
        }
    }

    public void create(BrandDiscount discount) {

        String sql = """
                INSERT INTO store_branddiscount
                (
                    brand_id,
                    discount_type,
                    discount_value,
                    max_discount_amount,
                    start_date,
                    end_date,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, discount.getBrandId());

            statement.setString(
                    2,
                    discount.getDiscountType() != null
                            ? discount.getDiscountType().name()
                            : null
            );

            statement.setBigDecimal(3, discount.getDiscountValue());
            statement.setBigDecimal(4, discount.getMaxDiscountAmount());

            if (discount.getStartDate() != null) {
                statement.setDate(5, Date.valueOf(discount.getStartDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            if (discount.getEndDate() != null) {
                statement.setDate(6, Date.valueOf(discount.getEndDate()));
            } else {
                statement.setNull(6, Types.DATE);
            }

            statement.setBoolean(7, discount.isActive());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.create error", e);
        }
    }

    public void update(BrandDiscount discount) {

        String sql = """
                UPDATE store_branddiscount
                SET brand_id = ?,
                    discount_type = ?,
                    discount_value = ?,
                    max_discount_amount = ?,
                    start_date = ?,
                    end_date = ?,
                    is_active = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, discount.getBrandId());

            statement.setString(
                    2,
                    discount.getDiscountType() != null
                            ? discount.getDiscountType().name()
                            : null
            );

            statement.setBigDecimal(3, discount.getDiscountValue());
            statement.setBigDecimal(4, discount.getMaxDiscountAmount());

            if (discount.getStartDate() != null) {
                statement.setDate(5, Date.valueOf(discount.getStartDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            if (discount.getEndDate() != null) {
                statement.setDate(6, Date.valueOf(discount.getEndDate()));
            } else {
                statement.setNull(6, Types.DATE);
            }

            statement.setBoolean(7, discount.isActive());
            statement.setInt(8, discount.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.update error", e);
        }
    }

    public void delete(int id) {

        String sql = """
                DELETE FROM store_branddiscount
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.delete error", e);
        }
    }

    public void toggleActive(int id) {

        String sql = """
                UPDATE store_branddiscount
                SET is_active = CASE
                    WHEN is_active = 1 THEN 0
                    ELSE 1
                END
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("BrandDiscountDAO.toggleActive error", e);
        }
    }

    // ================= MAPPING =================

    private BrandDiscount mapRow(ResultSet resultSet, boolean hasBrandName)
            throws SQLException {

        BrandDiscount discount = new BrandDiscount();

        discount.setId(resultSet.getInt("id"));
        discount.setBrandId(resultSet.getInt("brand_id"));

        String discountType = resultSet.getString("discount_type");

        if (discountType != null && !discountType.isBlank()) {
            discount.setDiscountType(DiscountType.valueOf(discountType));
        }

        discount.setDiscountValue(
                resultSet.getBigDecimal("discount_value")
        );

        discount.setMaxDiscountAmount(
                resultSet.getBigDecimal("max_discount_amount")
        );

        Date startDate = resultSet.getDate("start_date");

        if (startDate != null) {
            discount.setStartDate(startDate.toLocalDate());
        }

        Date endDate = resultSet.getDate("end_date");

        if (endDate != null) {
            discount.setEndDate(endDate.toLocalDate());
        }

        discount.setActive(resultSet.getBoolean("is_active"));

        if (hasBrandName) {
            try {
                discount.setBrandName(resultSet.getString("brand_name"));
            } catch (SQLException ignored) {
            }
        }

        return discount;
    }
}