package com.webshop.app.dao;

import com.webshop.app.model.AuditLog;
import com.webshop.app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    private static final int MAX_PAGE_SIZE = 100;

    public boolean insert(AuditLog log) {
        if (log == null) {
            return false;
        }

        String sql = """
                INSERT INTO audit_logs
                (
                    actor_user_id,
                    actor_username,
                    actor_full_name,
                    actor_role,
                    module,
                    action_type,
                    entity_type,
                    entity_id,
                    entity_name,
                    description,
                    old_value,
                    new_value,
                    request_method,
                    request_uri,
                    ip_address,
                    user_agent
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (log.getActorUserId() == null || log.getActorUserId() <= 0) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, log.getActorUserId());
            }

            ps.setString(2, limit(log.getActorUsername(), 100));
            ps.setString(3, limit(log.getActorFullName(), 150));
            ps.setString(4, limit(log.getActorRole(), 50));
            ps.setString(5, limit(defaultString(log.getModule(), "SYSTEM"), 60));
            ps.setString(6, limit(defaultString(log.getActionType(), "OTHER"), 40));
            ps.setString(7, limit(log.getEntityType(), 60));
            ps.setString(8, limit(log.getEntityId(), 80));
            ps.setString(9, limit(log.getEntityName(), 255));
            ps.setString(10, limit(defaultString(log.getDescription(), "Admin đã thực hiện thao tác quản trị."), 1000));
            ps.setString(11, log.getOldValue());
            ps.setString(12, log.getNewValue());
            ps.setString(13, limit(log.getRequestMethod(), 12));
            ps.setString(14, limit(log.getRequestUri(), 600));
            ps.setString(15, limit(log.getIpAddress(), 80));
            ps.setString(16, limit(log.getUserAgent(), 600));

            return ps.executeUpdate() > 0;

        } catch (RuntimeException | SQLException e) {
            /*
             * Audit log không được làm hỏng nghiệp vụ chính.
             * Nếu database chưa chạy migration, chỉ ghi lỗi ra console.
             */
            e.printStackTrace();
            return false;
        }
    }

    public List<AuditLog> find(AuditLogFilter filter) {
        AuditLogFilter f = normalizeFilter(filter);
        List<Object> params = new ArrayList<>();
        String whereSql = buildWhereSql(f, params);

        String sql = """
                SELECT
                    id,
                    actor_user_id,
                    actor_username,
                    actor_full_name,
                    actor_role,
                    module,
                    action_type,
                    entity_type,
                    entity_id,
                    entity_name,
                    description,
                    old_value,
                    new_value,
                    request_method,
                    request_uri,
                    ip_address,
                    user_agent,
                    created_at
                FROM audit_logs
                """ + whereSql + """
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        params.add(f.getPageSize());
        params.add((f.getPage() - 1) * f.getPageSize());

        List<AuditLog> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int count(AuditLogFilter filter) {
        AuditLogFilter f = normalizeFilter(filter);
        List<Object> params = new ArrayList<>();
        String whereSql = buildWhereSql(f, params);

        String sql = "SELECT COUNT(*) FROM audit_logs " + whereSql;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<String> findModules() {
        return findDistinct("module");
    }

    public List<String> findActionTypes() {
        return findDistinct("action_type");
    }

    private List<String> findDistinct(String column) {
        String safeColumn = "action_type".equals(column) ? "action_type" : "module";
        String sql = "SELECT DISTINCT " + safeColumn + " FROM audit_logs WHERE " + safeColumn + " IS NOT NULL ORDER BY " + safeColumn;
        List<String> values = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String value = trim(rs.getString(1));
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }

        } catch (RuntimeException | SQLException e) {
            e.printStackTrace();
        }

        return values;
    }

    private String buildWhereSql(AuditLogFilter filter, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");

        if (!trim(filter.getKeyword()).isEmpty()) {
            where.append("""
                    AND (
                        actor_username LIKE ?
                        OR actor_full_name LIKE ?
                        OR entity_name LIKE ?
                        OR entity_id LIKE ?
                        OR description LIKE ?
                        OR old_value LIKE ?
                        OR new_value LIKE ?
                    )
                    """);

            String kw = "%" + trim(filter.getKeyword()) + "%";
            for (int i = 0; i < 7; i++) {
                params.add(kw);
            }
        }

        if (!trim(filter.getModule()).isEmpty()) {
            where.append(" AND module = ? ");
            params.add(trim(filter.getModule()));
        }

        if (!trim(filter.getActionType()).isEmpty()) {
            where.append(" AND action_type = ? ");
            params.add(trim(filter.getActionType()));
        }

        if (!trim(filter.getActor()).isEmpty()) {
            where.append(" AND (actor_username LIKE ? OR actor_full_name LIKE ?) ");
            String actor = "%" + trim(filter.getActor()) + "%";
            params.add(actor);
            params.add(actor);
        }

        if (filter.getDateFrom() != null) {
            where.append(" AND created_at >= ? ");
            params.add(Timestamp.valueOf(filter.getDateFrom().atStartOfDay()));
        }

        if (filter.getDateTo() != null) {
            where.append(" AND created_at < ? ");
            params.add(Timestamp.valueOf(filter.getDateTo().plusDays(1).atStartOfDay()));
        }

        return where.toString();
    }

    private AuditLogFilter normalizeFilter(AuditLogFilter filter) {
        AuditLogFilter f = filter == null ? new AuditLogFilter() : filter;
        f.setPage(Math.max(f.getPage(), 1));
        f.setPageSize(Math.max(1, Math.min(f.getPageSize(), MAX_PAGE_SIZE)));
        return f;
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;

            if (value instanceof Timestamp timestamp) {
                ps.setTimestamp(index, timestamp);
            } else if (value instanceof Integer number) {
                ps.setInt(index, number);
            } else {
                ps.setString(index, String.valueOf(value));
            }
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));

        int actorId = rs.getInt("actor_user_id");
        log.setActorUserId(rs.wasNull() ? null : actorId);

        log.setActorUsername(rs.getString("actor_username"));
        log.setActorFullName(rs.getString("actor_full_name"));
        log.setActorRole(rs.getString("actor_role"));
        log.setModule(rs.getString("module"));
        log.setActionType(rs.getString("action_type"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setEntityName(rs.getString("entity_name"));
        log.setDescription(rs.getString("description"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setRequestMethod(rs.getString("request_method"));
        log.setRequestUri(rs.getString("request_uri"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }

    private String defaultString(String value, String fallback) {
        String trimmed = trim(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String limit(String value, int maxLength) {
        String trimmed = trim(value);
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class AuditLogFilter {
        private String keyword;
        private String module;
        private String actionType;
        private String actor;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private int page = 1;
        private int pageSize = 20;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public String getActor() {
            return actor;
        }

        public void setActor(String actor) {
            this.actor = actor;
        }

        public LocalDate getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(LocalDate dateFrom) {
            this.dateFrom = dateFrom;
        }

        public LocalDate getDateTo() {
            return dateTo;
        }

        public void setDateTo(LocalDate dateTo) {
            this.dateTo = dateTo;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = Math.max(page, 1);
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        }
    }
}
