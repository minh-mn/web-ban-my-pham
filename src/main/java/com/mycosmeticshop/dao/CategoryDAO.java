package com.mycosmeticshop.dao;

import com.mycosmeticshop.model.Category;
import com.mycosmeticshop.utils.DBConnection;

import java.sql.*;
import java.util.*;

public class CategoryDAO {

    /* =====================================================
       FRONTEND: CÂY DANH MỤC (CHA + CON, ACTIVE)
    ===================================================== */

    /** Lấy danh mục cha (active) + danh mục con (active) + productCount */
    public List<Category> findParents() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        List<Category> parents = new ArrayList<>();

        String sql =
            "SELECT id, name, slug, is_active " +
            "FROM store_category " +
            "WHERE parent_id IS NULL AND is_active = 1 " +
            "ORDER BY name ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category parent = new Category();
                int parentId = rs.getInt("id");

                parent.setId(parentId);
                parent.setName(rs.getString("name"));
                parent.setSlug(rs.getString("slug"));
                parent.setParentId(null);
                parent.setActive(true);

                List<Category> children = findChildren(parentId, countMap);
                parent.setChildren(children);

                int sum = 0;
                for (Category ch : children) {
                    sum += ch.getProductCount();
                }
                parent.setProductCount(sum);

                parents.add(parent);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findParents error", e);
        }

        return parents;
    }

    /** Lấy danh mục con (active) theo parent */
    private List<Category> findChildren(int parentId, Map<Integer, Integer> countMap) {

        List<Category> children = new ArrayList<>();

        String sql =
            "SELECT id, name, slug, is_active " +
            "FROM store_category " +
            "WHERE parent_id = ? AND is_active = 1 " +
            "ORDER BY name ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, parentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category child = new Category();
                    int id = rs.getInt("id");

                    child.setId(id);
                    child.setName(rs.getString("name"));
                    child.setSlug(rs.getString("slug"));
                    child.setParentId(parentId);
                    child.setActive(true);
                    child.setProductCount(countMap.getOrDefault(id, 0));

                    children.add(child);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findChildren error", e);
        }

        return children;
    }

    /** Đếm số product ACTIVE theo category */
    private Map<Integer, Integer> countActiveProductsByCategory() {

        Map<Integer, Integer> map = new HashMap<>();

        String sql =
            "SELECT category_id, COUNT(*) AS cnt " +
            "FROM store_product " +
            "WHERE is_active = 1 " +
            "GROUP BY category_id";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("category_id"), rs.getInt("cnt"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.countActiveProductsByCategory error", e);
        }

        return map;
    }

    /* =====================================================
       ADMIN: LIST / CRUD
    ===================================================== */

    /** Admin list: lấy toàn bộ category (cha + con, active + inactive) */
    public List<Category> findAll() {

        Map<Integer, Integer> countMap = countActiveProductsByCategory();
        List<Category> list = new ArrayList<>();

        String sql =
            "SELECT id, name, slug, parent_id, is_active " +
            "FROM store_category " +
            "ORDER BY parent_id ASC, name ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category();
                int id = rs.getInt("id");

                cat.setId(id);
                cat.setName(rs.getString("name"));
                cat.setSlug(rs.getString("slug"));

                int pid = rs.getInt("parent_id");
                cat.setParentId(rs.wasNull() ? null : pid);

                cat.setActive(rs.getBoolean("is_active"));
                cat.setProductCount(countMap.getOrDefault(id, 0));

                list.add(cat);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAll error", e);
        }

        return list;
    }

    public Category findById(int id) {

        String sql =
            "SELECT id, name, slug, parent_id, is_active " +
            "FROM store_category " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getInt("id"));
                    cat.setName(rs.getString("name"));
                    cat.setSlug(rs.getString("slug"));

                    int pid = rs.getInt("parent_id");
                    cat.setParentId(rs.wasNull() ? null : pid);

                    cat.setActive(rs.getBoolean("is_active"));
                    return cat;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findById error", e);
        }

        return null;
    }

    /** Dropdown admin: lấy danh mục cha (kể cả inactive) */
    public List<Category> findAllParents() {

        List<Category> list = new ArrayList<>();

        String sql =
            "SELECT id, name, slug, is_active " +
            "FROM store_category " +
            "WHERE parent_id IS NULL " +
            "ORDER BY name ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setSlug(rs.getString("slug"));
                cat.setParentId(null);
                cat.setActive(rs.getBoolean("is_active"));
                list.add(cat);
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.findAllParents error", e);
        }

        return list;
    }

    public void create(Category cat) {

        String sql =
            "INSERT INTO store_category (name, slug, parent_id, is_active) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cat.getName());
            ps.setString(2, cat.getSlug());

            if (cat.getParentId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, cat.getParentId());
            }

            ps.setBoolean(4, cat.isActive());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.create error", e);
        }
    }

    public void update(Category cat) {

        String sql =
            "UPDATE store_category " +
            "SET name = ?, slug = ?, parent_id = ?, is_active = ? " +
            "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cat.getName());
            ps.setString(2, cat.getSlug());

            if (cat.getParentId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, cat.getParentId());
            }

            ps.setBoolean(4, cat.isActive());
            ps.setInt(5, cat.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.update error", e);
        }
    }

    public void delete(int id) {

        String sql = "DELETE FROM store_category WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new RuntimeException(
                    "Không thể xóa danh mục vì đang có sản phẩm hoặc danh mục con.",
                    e
                );
            }
            throw new RuntimeException("CategoryDAO.delete error", e);
        }
    }

    /* =====================================================
       SLUG CHECK
    ===================================================== */

    public boolean existsBySlug(String slug) {

        String sql = "SELECT 1 FROM store_category WHERE slug = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, slug);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.existsBySlug error", e);
        }
    }

    public boolean existsBySlugExceptId(String slug, int id) {

        String sql =
            "SELECT 1 FROM store_category WHERE slug = ? AND id <> ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, slug);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("CategoryDAO.existsBySlugExceptId error", e);
        }
    }
}
