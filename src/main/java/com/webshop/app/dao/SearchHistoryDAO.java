package com.webshop.app.dao;

import com.webshop.app.model.UserSearchHistory;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchHistoryDAO {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_KEYWORD_LENGTH = 255;
    private static final int MAX_URL_LENGTH = 500;

    /**
     * Lưu lịch sử tìm kiếm của user.
     *
     * Nếu user tìm cùng một keyword nhiều lần:
     * - Không tạo dòng trùng.
     * - Tăng search_count.
     * - Cập nhật result_count, search_url, last_searched_at.
     */
    public void saveSearch(int userId, String keyword, int resultCount, String searchUrl) {
        if (userId <= 0) {
            return;
        }

        String cleanedKeyword = cleanKeyword(keyword);

        if (cleanedKeyword == null) {
            return;
        }

        String normalizedKeyword = normalizeKeyword(cleanedKeyword);
        String safeSearchUrl = trimToLength(searchUrl, MAX_URL_LENGTH);
        int safeResultCount = Math.max(resultCount, 0);

        String sql =
                "INSERT INTO user_search_history " +
                        "(user_id, keyword, normalized_keyword, result_count, search_url, search_count, " +
                        " created_at, updated_at, last_searched_at) " +
                        "VALUES (?, ?, ?, ?, ?, 1, NOW(), NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "keyword = VALUES(keyword), " +
                        "result_count = VALUES(result_count), " +
                        "search_url = VALUES(search_url), " +
                        "search_count = search_count + 1, " +
                        "updated_at = NOW(), " +
                        "last_searched_at = NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, cleanedKeyword);
            ps.setString(3, normalizedKeyword);
            ps.setInt(4, safeResultCount);
            ps.setString(5, safeSearchUrl);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.saveSearch error", e);
        }
    }

    /**
     * Alias dùng cho servlet nếu bạn đang gọi tên hàm ngắn hơn.
     */
    public void save(int userId, String keyword, int resultCount, String searchUrl) {
        saveSearch(userId, keyword, resultCount, searchUrl);
    }

    /**
     * Alias dùng khi chỉ cần lưu keyword, không cần số kết quả/url.
     */
    public void saveKeyword(int userId, String keyword) {
        saveSearch(userId, keyword, 0, buildDefaultSearchUrl(keyword));
    }

    /**
     * Lấy lịch sử tìm kiếm gần đây của user.
     */
    public List<UserSearchHistory> findRecentByUserId(int userId, int limit) {
        List<UserSearchHistory> histories = new ArrayList<>();

        if (userId <= 0) {
            return histories;
        }

        int safeLimit = normalizeLimit(limit);

        String sql =
                "SELECT id, user_id, keyword, normalized_keyword, result_count, search_url, " +
                        "search_count, created_at, updated_at, last_searched_at " +
                        "FROM user_search_history " +
                        "WHERE user_id = ? " +
                        "ORDER BY last_searched_at DESC, updated_at DESC, id DESC " +
                        "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, safeLimit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapRow(rs));
                }
            }

            return histories;

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.findRecentByUserId error", e);
        }
    }

    public List<UserSearchHistory> findRecentByUserId(int userId) {
        return findRecentByUserId(userId, DEFAULT_LIMIT);
    }

    /**
     * Alias nếu AccountServlet đang gọi findLatestByUserId.
     */
    public List<UserSearchHistory> findLatestByUserId(int userId, int limit) {
        return findRecentByUserId(userId, limit);
    }

    public List<UserSearchHistory> findLatestByUserId(int userId) {
        return findRecentByUserId(userId, DEFAULT_LIMIT);
    }

    /**
     * Lấy tất cả lịch sử của user, dùng cho trang quản lý riêng nếu sau này cần.
     */
    public List<UserSearchHistory> findAllByUserId(int userId) {
        return findRecentByUserId(userId, 100);
    }

    /**
     * Xóa một dòng lịch sử, bắt buộc kèm user_id để tránh xóa nhầm dữ liệu tài khoản khác.
     */
    public boolean deleteByIdAndUserId(long id, int userId) {
        if (id <= 0 || userId <= 0) {
            return false;
        }

        String sql = "DELETE FROM user_search_history WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.deleteByIdAndUserId error", e);
        }
    }

    /**
     * Alias nếu servlet đang gọi delete().
     */
    public boolean delete(int userId, long id) {
        return deleteByIdAndUserId(id, userId);
    }

    /**
     * Alias nếu servlet truyền id trước, userId sau.
     */
    public boolean delete(long id, int userId) {
        return deleteByIdAndUserId(id, userId);
    }

    /**
     * Xóa toàn bộ lịch sử tìm kiếm của user.
     */
    public int clearByUserId(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String sql = "DELETE FROM user_search_history WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.clearByUserId error", e);
        }
    }

    /**
     * Alias nếu servlet đang gọi clear().
     */
    public int clear(int userId) {
        return clearByUserId(userId);
    }

    /**
     * Đếm số dòng lịch sử của user.
     */
    public int countByUserId(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM user_search_history WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.countByUserId error", e);
        }
    }

    /**
     * Giữ lại N lịch sử gần nhất, xóa các lịch sử cũ hơn.
     * Không bắt buộc dùng, nhưng hữu ích để bảng không phình quá lớn.
     */
    public int deleteOldHistoriesKeepingLatest(int userId, int keepLatest) {
        if (userId <= 0) {
            return 0;
        }

        int safeKeepLatest = normalizeLimit(keepLatest);

        String sql =
                "DELETE FROM user_search_history " +
                        "WHERE user_id = ? " +
                        "AND id NOT IN ( " +
                        "    SELECT id FROM ( " +
                        "        SELECT id " +
                        "        FROM user_search_history " +
                        "        WHERE user_id = ? " +
                        "        ORDER BY last_searched_at DESC, updated_at DESC, id DESC " +
                        "        LIMIT ? " +
                        "    ) latest_history " +
                        ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, safeKeepLatest);

            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("SearchHistoryDAO.deleteOldHistoriesKeepingLatest error", e);
        }
    }

    private UserSearchHistory mapRow(ResultSet rs) throws SQLException {
        UserSearchHistory history = new UserSearchHistory();

        history.setId(rs.getLong("id"));
        history.setUserId(rs.getInt("user_id"));
        history.setKeyword(rs.getString("keyword"));
        history.setNormalizedKeyword(rs.getString("normalized_keyword"));
        history.setResultCount(rs.getInt("result_count"));
        history.setSearchUrl(rs.getString("search_url"));
        history.setSearchCount(rs.getInt("search_count"));
        history.setCreatedAt(rs.getTimestamp("created_at"));
        history.setUpdatedAt(rs.getTimestamp("updated_at"));
        history.setLastSearchedAt(rs.getTimestamp("last_searched_at"));

        return history;
    }

    private String cleanKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String cleaned = keyword.trim().replaceAll("\\s+", " ");

        if (cleaned.isBlank()) {
            return null;
        }

        return trimToLength(cleaned, MAX_KEYWORD_LENGTH);
    }

    public String normalizeKeyword(String keyword) {
        String cleaned = cleanKeyword(keyword);

        if (cleaned == null) {
            return "";
        }

        String normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");

        return trimToLength(normalized, MAX_KEYWORD_LENGTH);
    }

    private String buildDefaultSearchUrl(String keyword) {
        String cleaned = cleanKeyword(keyword);

        if (cleaned == null) {
            return null;
        }

        return "/search?q=" + cleaned.replace(" ", "+");
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, 100);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
