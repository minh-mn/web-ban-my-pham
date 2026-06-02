package com.webshop.app.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class AuditLog {

    private long id;
    private Integer actorUserId;
    private String actorUsername;
    private String actorFullName;
    private String actorRole;
    private String module;
    private String actionType;
    private String entityType;
    private String entityId;
    private String entityName;
    private String description;
    private String oldValue;
    private String newValue;
    private String requestMethod;
    private String requestUri;
    private String ipAddress;
    private String userAgent;
    private Timestamp createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = Math.max(id, 0);
    }

    public Integer getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Integer actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = normalize(actorUsername);
    }

    public String getActorFullName() {
        return actorFullName;
    }

    public void setActorFullName(String actorFullName) {
        this.actorFullName = normalize(actorFullName);
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = normalize(actorRole);
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = normalize(module);
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = normalize(actionType);
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = normalize(entityType);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = normalize(entityId);
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = normalize(entityName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalize(description);
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = normalize(oldValue);
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = normalize(newValue);
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = normalize(requestMethod);
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = normalize(requestUri);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = normalize(ipAddress);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = normalize(userAgent);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getActorDisplayName() {
        if (actorFullName != null && !actorFullName.isBlank()) {
            return actorFullName;
        }
        if (actorUsername != null && !actorUsername.isBlank()) {
            return actorUsername;
        }
        if (actorUserId != null && actorUserId > 0) {
            return "User #" + actorUserId;
        }
        return "Hệ thống";
    }

    public String getEntityDisplay() {
        String name = entityName == null || entityName.isBlank() ? entityType : entityName;
        if (entityId == null || entityId.isBlank()) {
            return name == null || name.isBlank() ? "-" : name;
        }
        if (name == null || name.isBlank()) {
            return "#" + entityId;
        }
        return name + " (#" + entityId + ")";
    }

    public String getCreatedAtDisplay() {
        if (createdAt == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(createdAt);
    }

    public String getModuleLabel() {
        if (module == null || module.isBlank()) {
            return "Hệ thống";
        }
        return switch (module.toUpperCase()) {
            case "PRODUCT" -> "Sản phẩm";
            case "ORDER" -> "Đơn hàng";
            case "INVENTORY" -> "Tồn kho";
            case "USER" -> "Tài khoản";
            case "ROLE" -> "Phân quyền";
            case "PROMOTION" -> "Khuyến mãi";
            default -> module;
        };
    }

    public String getActionLabel() {
        if (actionType == null || actionType.isBlank()) {
            return "Khác";
        }
        return switch (actionType.toUpperCase()) {
            case "CREATE" -> "Thêm mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "SOFT_DELETE" -> "Ẩn/khóa";
            case "STATUS_CHANGE" -> "Đổi trạng thái";
            case "IMPORT" -> "Nhập file";
            case "EXPORT" -> "Xuất file";
            default -> actionType;
        };
    }

    public String getActionCssClass() {
        if (actionType == null) {
            return "admin-pill--info";
        }
        return switch (actionType.toUpperCase()) {
            case "CREATE", "IMPORT" -> "admin-pill--ok";
            case "UPDATE", "STATUS_CHANGE" -> "admin-pill--warning";
            case "DELETE", "SOFT_DELETE" -> "admin-pill--danger";
            default -> "admin-pill--info";
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
