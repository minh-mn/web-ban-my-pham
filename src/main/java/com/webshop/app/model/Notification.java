package com.webshop.app.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Notification {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String REF_ORDER = "ORDER";
    public static final String REF_CANCEL_REQUEST = "CANCEL_REQUEST";
    public static final String REF_RETURN_REQUEST = "RETURN_REQUEST";
    public static final String REF_REVIEW = "REVIEW";
    public static final String REF_SYSTEM = "SYSTEM";

    private long id;

    /*
     * userId có thể null:
     * - USER notification: userId = id khách hàng nhận thông báo
     * - ADMIN notification: userId = null, roleTarget = ADMIN
     */
    private Integer userId;

    private String roleTarget;
    private String type;
    private String title;
    private String message;
    private String targetUrl;
    private String referenceType;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime readAt;

    public Notification() {
        this.roleTarget = ROLE_USER;
        this.type = "SYSTEM";
        this.read = false;
    }

    public Notification(
            Integer userId,
            String roleTarget,
            String type,
            String title,
            String message,
            String targetUrl,
            String referenceType,
            Long referenceId
    ) {
        this();
        setUserId(userId);
        setRoleTarget(roleTarget);
        setType(type);
        setTitle(title);
        setMessage(message);
        setTargetUrl(targetUrl);
        setReferenceType(referenceType);
        setReferenceId(referenceId);
    }

    /* =========================================================
       ID / TARGET
    ========================================================= */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = Math.max(id, 0);
    }

    public Integer getUserId() {
        return userId;
    }

    public int getUserIdValue() {
        return userId == null ? 0 : userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId == null || userId <= 0 ? null : userId;
    }

    public void setUserId(int userId) {
        this.userId = userId <= 0 ? null : userId;
    }

    public String getRoleTarget() {
        return roleTarget;
    }

    public void setRoleTarget(String roleTarget) {
        String value = normalizeUpper(roleTarget, ROLE_USER);

        if (ROLE_ADMIN.equals(value)) {
            this.roleTarget = ROLE_ADMIN;
        } else {
            this.roleTarget = ROLE_USER;
        }
    }

    public boolean isAdminTarget() {
        return ROLE_ADMIN.equalsIgnoreCase(roleTarget);
    }

    public boolean getAdminTarget() {
        return isAdminTarget();
    }

    public boolean isUserTarget() {
        return ROLE_USER.equalsIgnoreCase(roleTarget);
    }

    public boolean getUserTarget() {
        return isUserTarget();
    }

    /* =========================================================
       CONTENT
    ========================================================= */

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = normalizeUpper(type, "SYSTEM");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalizeRequired(title, "Thông báo");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = normalizeRequired(message, "");
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = normalizeNullable(targetUrl);
    }

    public boolean hasTargetUrl() {
        return targetUrl != null && !targetUrl.isBlank();
    }

    public boolean getHasTargetUrl() {
        return hasTargetUrl();
    }

    /* =========================================================
       REFERENCE
    ========================================================= */

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = normalizeNullableUpper(referenceType);
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId == null || referenceId <= 0 ? null : referenceId;
    }

    public void setReferenceId(long referenceId) {
        this.referenceId = referenceId <= 0 ? null : referenceId;
    }

    /*
     * Tương thích code cũ đang dùng orderId.
     * orderId sẽ map vào reference_type = ORDER, reference_id = orderId.
     */
    public Long getOrderId() {
        if (REF_ORDER.equalsIgnoreCase(referenceType)) {
            return referenceId;
        }

        return null;
    }

    public void setOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            if (REF_ORDER.equalsIgnoreCase(referenceType)) {
                this.referenceType = null;
                this.referenceId = null;
            }
            return;
        }

        this.referenceType = REF_ORDER;
        this.referenceId = orderId;
    }

    public void setOrderId(long orderId) {
        setOrderId(Long.valueOf(orderId));
    }

    public boolean isOrderNotification() {
        return REF_ORDER.equalsIgnoreCase(referenceType);
    }

    public boolean getOrderNotification() {
        return isOrderNotification();
    }

    public boolean isReviewNotification() {
        return REF_REVIEW.equalsIgnoreCase(referenceType);
    }

    public boolean getReviewNotification() {
        return isReviewNotification();
    }

    public boolean isReturnRequestNotification() {
        return REF_RETURN_REQUEST.equalsIgnoreCase(referenceType);
    }

    public boolean getReturnRequestNotification() {
        return isReturnRequestNotification();
    }

    public boolean isCancelRequestNotification() {
        return REF_CANCEL_REQUEST.equalsIgnoreCase(referenceType);
    }

    public boolean getCancelRequestNotification() {
        return isCancelRequestNotification();
    }

    /* =========================================================
       READ STATE
    ========================================================= */

    public boolean isRead() {
        return read;
    }

    public boolean getRead() {
        return read;
    }

    public boolean isUnread() {
        return !read;
    }

    public boolean getUnread() {
        return isUnread();
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Date getReadAtDate() {
        return toDate(readAt);
    }

    /* =========================================================
       TIME
    ========================================================= */

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAtDate() {
        return toDate(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAtDate() {
        return toDate(updatedAt);
    }

    /* =========================================================
       VIEW HELPERS
    ========================================================= */

    public String getTypeLabel() {
        if (type == null || type.isBlank()) {
            return "Thông báo";
        }

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "ORDER_CREATED" -> "Đơn hàng mới";
            case "ORDER_CONFIRMED" -> "Đơn đã xác nhận";
            case "ORDER_SHIPPING" -> "Đơn đang giao";
            case "ORDER_DELIVERED" -> "Giao thành công";
            case "ORDER_DELIVERY_FAILED" -> "Giao thất bại";
            case "ORDER_CANCELLED" -> "Đơn đã hủy";

            case "CANCEL_REQUEST_CREATED" -> "Yêu cầu hủy đơn";
            case "CANCEL_REQUEST_APPROVED" -> "Hủy đơn được duyệt";
            case "CANCEL_REQUEST_REJECTED" -> "Hủy đơn bị từ chối";

            case "RETURN_REQUEST_CREATED" -> "Yêu cầu hoàn hàng";
            case "RETURN_REQUEST_APPROVED" -> "Hoàn hàng được duyệt";
            case "RETURN_REQUEST_REJECTED" -> "Hoàn hàng bị từ chối";

            case "REVIEW_CREATED" -> "Đánh giá mới";
            case "REVIEW_APPROVED" -> "Đánh giá được duyệt";
            case "REVIEW_REJECTED" -> "Đánh giá bị từ chối";
            case "REVIEW_HIDDEN" -> "Đánh giá bị ẩn";

            case "CONTACT_CREATED" -> "Liên hệ mới";

            default -> "Thông báo";
        };
    }

    public String getTypeCssClass() {
        if (type == null || type.isBlank()) {
            return "system";
        }

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "ORDER_CREATED",
                 "ORDER_CONFIRMED",
                 "ORDER_SHIPPING" -> "order";

            case "ORDER_DELIVERED",
                 "CANCEL_REQUEST_APPROVED",
                 "RETURN_REQUEST_APPROVED",
                 "REVIEW_APPROVED" -> "success";

            case "ORDER_DELIVERY_FAILED",
                 "ORDER_CANCELLED",
                 "CANCEL_REQUEST_REJECTED",
                 "RETURN_REQUEST_REJECTED",
                 "REVIEW_REJECTED",
                 "REVIEW_HIDDEN" -> "danger";

            case "CANCEL_REQUEST_CREATED",
                 "RETURN_REQUEST_CREATED",
                 "CONTACT_CREATED" -> "warning";

            case "REVIEW_CREATED" -> "review";

            default -> "system";
        };
    }

    public String getReadCssClass() {
        return read ? "is-read" : "is-unread";
    }

    public String getIcon() {
        if (type == null || type.isBlank()) {
            return "🔔";
        }

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "ORDER_CREATED" -> "🛒";
            case "ORDER_CONFIRMED" -> "✅";
            case "ORDER_SHIPPING" -> "🚚";
            case "ORDER_DELIVERED" -> "📦";
            case "ORDER_DELIVERY_FAILED" -> "⚠️";
            case "ORDER_CANCELLED" -> "❌";

            case "CANCEL_REQUEST_CREATED" -> "📝";
            case "CANCEL_REQUEST_APPROVED" -> "✅";
            case "CANCEL_REQUEST_REJECTED" -> "🚫";

            case "RETURN_REQUEST_CREATED" -> "↩️";
            case "RETURN_REQUEST_APPROVED" -> "✅";
            case "RETURN_REQUEST_REJECTED" -> "🚫";

            case "REVIEW_CREATED" -> "⭐";
            case "CONTACT_CREATED" -> "📞";
            case "REVIEW_APPROVED" -> "🌟";
            case "REVIEW_REJECTED", "REVIEW_HIDDEN" -> "🚫";

            default -> "🔔";
        };
    }

    /* =========================================================
       NORMALIZE
    ========================================================= */

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String normalizeUpper(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullableUpper(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeRequired(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }
}
