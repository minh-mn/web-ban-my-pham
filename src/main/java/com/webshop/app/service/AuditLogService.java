package com.webshop.app.service;

import com.webshop.app.dao.AuditLogDAO;
import com.webshop.app.model.AuditLog;
import com.webshop.app.model.User;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuditLogService {

    private static final AuditLogDAO AUDIT_LOG_DAO = new AuditLogDAO();
    private static final DecimalFormat VND_FORMAT = new DecimalFormat("#,##0");

    private AuditLogService() {
    }

    public static void logCreate(HttpServletRequest request,
                                 String module,
                                 String entityType,
                                 Object entityId,
                                 String entityName,
                                 String description,
                                 String newValue) {
        log(request, module, "CREATE", entityType, entityId, entityName, description, null, newValue);
    }

    public static void logUpdate(HttpServletRequest request,
                                 String module,
                                 String entityType,
                                 Object entityId,
                                 String entityName,
                                 String description,
                                 String oldValue,
                                 String newValue) {
        log(request, module, "UPDATE", entityType, entityId, entityName, description, oldValue, newValue);
    }

    public static void logDelete(HttpServletRequest request,
                                 String module,
                                 String entityType,
                                 Object entityId,
                                 String entityName,
                                 String description,
                                 String oldValue) {
        log(request, module, "DELETE", entityType, entityId, entityName, description, oldValue, null);
    }

    public static void logSoftDelete(HttpServletRequest request,
                                     String module,
                                     String entityType,
                                     Object entityId,
                                     String entityName,
                                     String description,
                                     String oldValue,
                                     String newValue) {
        log(request, module, "SOFT_DELETE", entityType, entityId, entityName, description, oldValue, newValue);
    }

    public static void logStatusChange(HttpServletRequest request,
                                       String module,
                                       String entityType,
                                       Object entityId,
                                       String entityName,
                                       String description,
                                       String oldValue,
                                       String newValue) {
        log(request, module, "STATUS_CHANGE", entityType, entityId, entityName, description, oldValue, newValue);
    }

    public static void logImport(HttpServletRequest request,
                                 String module,
                                 String entityType,
                                 Object entityId,
                                 String entityName,
                                 String description,
                                 String newValue) {
        log(request, module, "IMPORT", entityType, entityId, entityName, description, null, newValue);
    }

    public static void log(HttpServletRequest request,
                           String module,
                           String actionType,
                           String entityType,
                           Object entityId,
                           String entityName,
                           String description,
                           String oldValue,
                           String newValue) {
        try {
            AuditLog log = new AuditLog();
            fillActor(log, request);
            fillRequest(log, request);

            log.setModule(normalizeUpper(module, "SYSTEM"));
            log.setActionType(normalizeUpper(actionType, "OTHER"));
            log.setEntityType(trimToNull(entityType));
            log.setEntityId(entityId == null ? null : String.valueOf(entityId));
            log.setEntityName(trimToNull(entityName));
            log.setDescription(trimToDefault(description, "Admin đã thực hiện thao tác quản trị."));
            log.setOldValue(trimToNull(oldValue));
            log.setNewValue(trimToNull(newValue));

            AUDIT_LOG_DAO.insert(log);
        } catch (Exception e) {
            /* Không làm hỏng nghiệp vụ chính nếu logging lỗi. */
            e.printStackTrace();
        }
    }

    public static String changes(String... changes) {
        if (changes == null || changes.length == 0) {
            return null;
        }

        List<String> validChanges = new ArrayList<>();
        for (String change : changes) {
            String value = trimToNull(change);
            if (value != null) {
                validChanges.add(value);
            }
        }

        return validChanges.isEmpty() ? null : String.join("\n", validChanges);
    }

    public static String change(String fieldName, Object oldValue, Object newValue) {
        String oldText = displayValue(oldValue);
        String newText = displayValue(newValue);

        if (Objects.equals(oldText, newText)) {
            return null;
        }

        return trimToDefault(fieldName, "Giá trị") + ": " + oldText + " -> " + newText;
    }

    public static String moneyChange(String fieldName, BigDecimal oldValue, BigDecimal newValue) {
        String oldText = formatMoney(oldValue);
        String newText = formatMoney(newValue);

        if (Objects.equals(oldText, newText)) {
            return null;
        }

        return trimToDefault(fieldName, "Số tiền") + ": " + oldText + " -> " + newText;
    }

    public static String formatMoney(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return VND_FORMAT.format(safeValue) + "đ";
    }

    private static void fillActor(AuditLog log, HttpServletRequest request) {
        User user = currentUser(request);

        if (user == null) {
            return;
        }

        if (user.getId() > 0) {
            log.setActorUserId(user.getId());
        }
        log.setActorUsername(user.getUsername());
        log.setActorFullName(user.getFullName());
        log.setActorRole(user.getRole());
    }

    private static void fillRequest(AuditLog log, HttpServletRequest request) {
        if (request == null) {
            return;
        }

        log.setRequestMethod(request.getMethod());
        log.setRequestUri(buildRequestUri(request));
        log.setIpAddress(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
    }

    private static User currentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("user");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("authUser");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("currentUser");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        return null;
    }

    private static String buildRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        if (query == null || query.isBlank()) {
            return uri;
        }

        return uri + "?" + query;
    }

    private static String clientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                int comma = value.indexOf(',');
                return comma >= 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private static String displayValue(Object value) {
        if (value == null) {
            return "-";
        }

        if (value instanceof BigDecimal money) {
            return formatMoney(money);
        }

        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "-" : text;
    }

    private static String normalizeUpper(String value, String fallback) {
        String text = trimToDefault(value, fallback);
        return text.toUpperCase();
    }

    private static String trimToDefault(String value, String fallback) {
        String text = trimToNull(value);
        return text == null ? fallback : text;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }
}
