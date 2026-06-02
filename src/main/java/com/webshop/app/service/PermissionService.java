package com.webshop.app.service;

import com.webshop.app.dao.RoleDAO;
import com.webshop.app.model.User;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionService {

    public static final String ADMIN_ACCESS = "ADMIN_ACCESS";
    public static final String REVENUE_VIEW = "REVENUE_VIEW";
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    public static final String PRODUCT_MANAGE = "PRODUCT_MANAGE";
    public static final String CATEGORY_MANAGE = "CATEGORY_MANAGE";
    public static final String BRAND_MANAGE = "BRAND_MANAGE";
    public static final String BANNER_MANAGE = "BANNER_MANAGE";
    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE";
    public static final String ORDER_MANAGE = "ORDER_MANAGE";
    public static final String RETURN_MANAGE = "RETURN_MANAGE";
    public static final String PROMOTION_MANAGE = "PROMOTION_MANAGE";
    public static final String REVIEW_MANAGE = "REVIEW_MANAGE";
    public static final String CMS_MANAGE = "CMS_MANAGE";
    public static final String CONTACT_MANAGE = "CONTACT_MANAGE";
    public static final String NOTIFICATION_MANAGE = "NOTIFICATION_MANAGE";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";
    public static final String SETTING_MANAGE = "SETTING_MANAGE";
    public static final String FLASHSALE_MANAGE = "FLASHSALE_MANAGE";
    public static final String RANK_MANAGE = "RANK_MANAGE";
    public static final String AUDIT_LOG_VIEW = "AUDIT_LOG_VIEW";

    private final RoleDAO roleDAO = new RoleDAO();

    public boolean isSuperAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    public Set<String> getPermissions(User user) {
        Set<String> permissions = new LinkedHashSet<>();

        if (user == null || user.getRole() == null || user.getRole().isBlank()) {
            return permissions;
        }

        if (isSuperAdmin(user)) {
            permissions.add(ADMIN_ACCESS);
            permissions.add(REVENUE_VIEW);
            permissions.add(DASHBOARD_VIEW);
            permissions.add(PRODUCT_MANAGE);
            permissions.add(CATEGORY_MANAGE);
            permissions.add(BRAND_MANAGE);
            permissions.add(BANNER_MANAGE);
            permissions.add(INVENTORY_MANAGE);
            permissions.add(ORDER_MANAGE);
            permissions.add(RETURN_MANAGE);
            permissions.add(PROMOTION_MANAGE);
            permissions.add(REVIEW_MANAGE);
            permissions.add(CMS_MANAGE);
            permissions.add(CONTACT_MANAGE);
            permissions.add(NOTIFICATION_MANAGE);
            permissions.add(USER_MANAGE);
            permissions.add(ROLE_MANAGE);
            permissions.add(SETTING_MANAGE);
            permissions.add(FLASHSALE_MANAGE);
            permissions.add(RANK_MANAGE);
            permissions.add(AUDIT_LOG_VIEW);
            return permissions;
        }

        permissions.addAll(roleDAO.findPermissionCodesByRole(user.getRole()));
        return permissions;
    }

    public boolean hasPermission(User user, String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            return false;
        }

        if (isSuperAdmin(user)) {
            return true;
        }

        Set<String> permissions = getPermissions(user);
        return permissions.contains(permissionCode.toUpperCase());
    }

    public boolean canAccessAdmin(User user) {
        return hasPermission(user, ADMIN_ACCESS) || !getPermissions(user).isEmpty();
    }

    public String resolveRequiredPermission(HttpServletRequest req) {
        if (req == null) {
            return ADMIN_ACCESS;
        }

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (path == null || path.isBlank() || "/admin".equals(path) || "/admin/".equals(path)) {
            return ADMIN_ACCESS;
        }

        if (path.startsWith("/admin/dashboard")) {
            return REVENUE_VIEW;
        }

        if (path.startsWith("/admin/products")) {
            return PRODUCT_MANAGE;
        }

        if (path.startsWith("/admin/categories")) {
            return CATEGORY_MANAGE;
        }

        if (path.startsWith("/admin/brands")) {
            return BRAND_MANAGE;
        }

        if (path.startsWith("/admin/banners")) {
            return BANNER_MANAGE;
        }

        if (path.startsWith("/admin/inventory")) {
            return INVENTORY_MANAGE;
        }

        if (path.startsWith("/admin/orders") || path.startsWith("/admin/order/")) {
            return ORDER_MANAGE;
        }

        if (path.startsWith("/admin/returns") || path.startsWith("/admin/cancel-requests")) {
            return RETURN_MANAGE;
        }

        if (path.startsWith("/admin/promotions")) {
            return PROMOTION_MANAGE;
        }

        if (path.startsWith("/admin/flash-sale")) {
            return FLASHSALE_MANAGE;
        }

        if (path.startsWith("/admin/reviews")) {
            return REVIEW_MANAGE;
        }

        if (path.startsWith("/admin/pages") || path.startsWith("/admin/events")) {
            return CMS_MANAGE;
        }

        if (path.startsWith("/admin/settings")) {
            return SETTING_MANAGE;
        }

        if (path.startsWith("/admin/contact-messages")) {
            return CONTACT_MANAGE;
        }

        if (path.startsWith("/admin/notifications")) {
            return NOTIFICATION_MANAGE;
        }

        if (path.startsWith("/admin/users")) {
            return USER_MANAGE;
        }

        if (path.startsWith("/admin/audit-logs")) {
            return AUDIT_LOG_VIEW;
        }

        if (path.startsWith("/admin/roles")) {
            return ROLE_MANAGE;
        }

        if (path.startsWith("/admin/ranks")) {
            return RANK_MANAGE;
        }

        return ADMIN_ACCESS;
    }
}
