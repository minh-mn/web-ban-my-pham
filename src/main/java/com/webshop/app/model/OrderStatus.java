package com.webshop.app.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum OrderStatus {

    PROCESSING("processing", "Chờ xác nhận", "warning", 1),
    CONFIRMED("confirmed", "Đã xác nhận", "primary", 2),
    SHIPPING("shipping", "Đang giao", "info", 3),
    COMPLETED("completed", "Giao thành công", "ok", 4),
    CANCELLED("cancelled", "Đã hủy", "danger", -1);

    private final String key;
    private final String label;
    private final String cssClass;
    private final int step;

    OrderStatus(String key, String label, String cssClass, int step) {
        this.key = key;
        this.label = label;
        this.cssClass = cssClass;
        this.step = step;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public int getStep() {
        return step;
    }

    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isActiveStatus() {
        return this != COMPLETED && this != CANCELLED;
    }

    public static List<OrderStatus> choices() {
        return Arrays.asList(values());
    }

    public static OrderStatus fromKey(String key) {
        String normalized = normalizeKey(key);

        for (OrderStatus status : values()) {
            if (status.key.equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        return PROCESSING;
    }

    public static String normalizeKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return PROCESSING.key;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);

        if ("pending".equals(normalized)) {
            return PROCESSING.key;
        }

        if ("canceled".equals(normalized)) {
            return CANCELLED.key;
        }

        for (OrderStatus status : values()) {
            if (status.key.equalsIgnoreCase(normalized)) {
                return status.key;
            }
        }

        return PROCESSING.key;
    }

    public static String labelOf(String key) {
        return fromKey(key).label;
    }

    public static String cssClassOf(String key) {
        return fromKey(key).cssClass;
    }

    public static int stepOf(String key) {
        return fromKey(key).step;
    }

    public static boolean isValidKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);

        if ("canceled".equals(normalized)) {
            normalized = CANCELLED.key;
        }

        if ("pending".equals(normalized)) {
            normalized = PROCESSING.key;
        }

        for (OrderStatus status : values()) {
            if (status.key.equalsIgnoreCase(normalized)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFinalKey(String key) {
        return fromKey(key).isFinalStatus();
    }

    public static boolean isCancelableKey(String key) {
        OrderStatus status = fromKey(key);

        return status == PROCESSING || status == CONFIRMED;
    }

    public static boolean canConfirm(String key) {
        return fromKey(key) == PROCESSING;
    }

    public static boolean canStartShipping(String key) {
        return fromKey(key) == CONFIRMED;
    }

    public static boolean canComplete(String key) {
        return fromKey(key) == SHIPPING;
    }

    public static boolean canCancel(String key) {
        return isCancelableKey(key);
    }

    public static boolean canMove(String currentKey, String nextKey) {
        OrderStatus current = fromKey(currentKey);
        OrderStatus next = fromKey(nextKey);

        if (current == next) {
            return true;
        }

        if (current.isFinalStatus()) {
            return false;
        }

        return switch (current) {
            case PROCESSING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPING || next == CANCELLED;
            case SHIPPING -> next == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
