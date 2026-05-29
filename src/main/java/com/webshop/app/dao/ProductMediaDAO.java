package com.webshop.app.dao;

import com.webshop.app.model.ProductMedia;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductMediaDAO {

    /*
     * Lấy danh sách media ảnh/video theo sản phẩm.
     * Dùng cho:
     * - AdminProductServlet khi sửa sản phẩm
     * - ProductDetailServlet khi hiển thị chi tiết sản phẩm
     */
    public List<ProductMedia> findByProductId(int productId) {
        List<ProductMedia> list = new ArrayList<>();

        String sql =
                "SELECT id, product_id, media_url, media_type, sort_order, created_at " +
                        "FROM store_productmedia " +
                        "WHERE product_id = ? " +
                        "ORDER BY sort_order ASC, id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.findByProductId error", e);
        }

        return list;
    }

    public ProductMedia findById(int mediaId) {
        String sql =
                "SELECT id, product_id, media_url, media_type, sort_order, created_at " +
                        "FROM store_productmedia " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.findById error", e);
        }
    }

    public ProductMedia findByIdAndProductId(int mediaId, int productId) {
        String sql =
                "SELECT id, product_id, media_url, media_type, sort_order, created_at " +
                        "FROM store_productmedia " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.findByIdAndProductId error", e);
        }
    }

    /*
     * Lấy media_url trước khi xóa SQL.
     * Servlet dùng URL này để xóa file vật lý sau khi SQL delete thành công.
     */
    public String findMediaUrlByIdAndProductId(int mediaId, int productId) {
        String sql =
                "SELECT media_url " +
                        "FROM store_productmedia " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("media_url");
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.findMediaUrlByIdAndProductId error", e);
        }
    }

    /*
     * Lấy toàn bộ media_url của sản phẩm.
     * Dùng trước khi hard delete product để xóa file vật lý sau khi SQL delete thành công.
     */
    public List<String> findMediaUrlsByProductId(int productId) {
        List<String> urls = new ArrayList<>();

        String sql =
                "SELECT media_url " +
                        "FROM store_productmedia " +
                        "WHERE product_id = ? " +
                        "ORDER BY sort_order ASC, id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    urls.add(rs.getString("media_url"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.findMediaUrlsByProductId error", e);
        }

        return urls;
    }

    /*
     * Thêm media mới cho sản phẩm.
     *
     * mediaType nên là:
     * - IMAGE
     * - VIDEO
     */
    public boolean insert(int productId, String mediaUrl, String mediaType, int sortOrder) {
        String sql =
                "INSERT INTO store_productmedia " +
                        "(product_id, media_url, media_type, sort_order, created_at) " +
                        "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setString(2, normalizeMediaUrl(mediaUrl));
            ps.setString(3, normalizeMediaType(mediaType));
            ps.setInt(4, Math.max(sortOrder, 0));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.insert error", e);
        }
    }

    public boolean insert(ProductMedia media) {
        if (media == null) {
            return false;
        }

        return insert(
                media.getProductId(),
                media.getMediaUrl(),
                media.getMediaType(),
                media.getSortOrder()
        );
    }

    /*
     * Xóa 1 media theo id.
     * Ít dùng hơn deleteByIdAndProductId vì không kiểm tra thuộc sản phẩm nào.
     */
    public boolean deleteById(int mediaId) {
        String sql =
                "DELETE FROM store_productmedia " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.deleteById error", e);
        }
    }

    /*
     * Xóa 1 media theo id và product_id.
     * Dùng khi admin tick xóa từng ảnh/video trong form sửa sản phẩm.
     */
    public boolean deleteByIdAndProductId(int mediaId, int productId) {
        String sql =
                "DELETE FROM store_productmedia " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.deleteByIdAndProductId error", e);
        }
    }

    /*
     * Xóa toàn bộ media của một sản phẩm.
     *
     * Lưu ý:
     * - Hàm này chỉ xóa SQL.
     * - Nếu cần xóa file vật lý, servlet/service phải lấy media_url trước bằng
     *   findMediaUrlsByProductId(productId), sau đó gọi UploadConfig.deleteProductMediaFileByUrl(...).
     */
    public int deleteByProductId(int productId) {
        String sql =
                "DELETE FROM store_productmedia " +
                        "WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.deleteByProductId error", e);
        }
    }

    /*
     * Đổi thứ tự hiển thị media.
     * Có thể dùng sau này nếu admin cần kéo thả sắp xếp ảnh/video.
     */
    public boolean updateSortOrder(int mediaId, int productId, int sortOrder) {
        String sql =
                "UPDATE store_productmedia " +
                        "SET sort_order = ? " +
                        "WHERE id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(sortOrder, 0));
            ps.setInt(2, mediaId);
            ps.setInt(3, productId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.updateSortOrder error", e);
        }
    }

    /*
     * Kiểm tra media có thuộc sản phẩm không.
     * Dùng được nếu sau này cần validate trước khi xóa/sửa.
     */
    public boolean existsByIdAndProductId(int mediaId, int productId) {
        String sql =
                "SELECT 1 " +
                        "FROM store_productmedia " +
                        "WHERE id = ? AND product_id = ? " +
                        "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mediaId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("ProductMediaDAO.existsByIdAndProductId error", e);
        }
    }

    private ProductMedia mapRow(ResultSet rs) throws SQLException {
        ProductMedia media = new ProductMedia();

        media.setId(rs.getInt("id"));
        media.setProductId(rs.getInt("product_id"));
        media.setMediaUrl(rs.getString("media_url"));
        media.setMediaType(rs.getString("media_type"));
        media.setSortOrder(rs.getInt("sort_order"));
        media.setCreatedAt(rs.getTimestamp("created_at"));

        return media;
    }

    private String normalizeMediaType(String mediaType) {
        if (mediaType == null || mediaType.isBlank()) {
            return "IMAGE";
        }

        String value = mediaType.trim().toUpperCase();

        if ("VIDEO".equals(value)) {
            return "VIDEO";
        }

        return "IMAGE";
    }

    private String normalizeMediaUrl(String mediaUrl) {
        if (mediaUrl == null) {
            return "";
        }

        return mediaUrl.trim();
    }
}