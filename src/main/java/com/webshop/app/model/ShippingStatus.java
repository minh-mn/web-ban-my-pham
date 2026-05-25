package com.webshop.app.model;

import java.util.Locale;

public enum ShippingStatus {

    PENDING_PICKUP(
            "PENDING_PICKUP",
            "Chờ lấy hàng",
            "shipping-pending-pickup",
            0,
            false
    ),

    DELIVERING(
            "DELIVERING",
            "Đang giao",
            "shipping-delivering",
            1,
            false
    ),

    DELIVERED(
            "DELIVERED",
            "Giao thành công",
            "shipping-delivered",
            2,
            true
    ),

    FAILED(
            "FAILED",
            "Giao thất bại",
            "shipping-failed",
            2,
            true
    ),

    CANCELED(
            "CANCELED",
            "Đã hủy vận chuyển",
            "shipping-canceled",
            -1,
            true
    );

    private final String code;
    private final String label;
    private final String cssClass;
    private final int step;
    private final boolean terminal;

    ShippingStatus(String code,
                   String label,
                   String cssClass,
                   int step,
                   boolean terminal) {
        this.code = code;
        this.label = label;
        this.cssClass = cssClass;
        this.step = step;
        this.terminal = terminal;
    }

    public String getCode() {
        return code;
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

    public boolean isTerminal() {
        return terminal;
    }

    public boolean isSuccess() {
        return this == DELIVERED;
    }

    public boolean isFailure() {
        return this == FAILED || this == CANCELED;
    }

    public boolean matches(String rawCode) {
        return this == fromCode(rawCode);
    }

    public static ShippingStatus fromCode(String rawCode) {
        String normalized = normalizeRaw(rawCode);

        if (normalized == null) {
            return PENDING_PICKUP;
        }

        return switch (normalized) {
            /* Giá trị chuẩn mới */
            case "PENDING_PICKUP" -> PENDING_PICKUP;
            case "DELIVERING" -> DELIVERING;
            case "DELIVERED" -> DELIVERED;
            case "FAILED" -> FAILED;
            case "CANCELED", "CANCELLED" -> CANCELED;

            /* Giá trị cũ trong database/project */
            case "PENDING", "CREATED", "PICKING", "WAITING_PICKUP", "WAITING_FOR_PICKUP" -> PENDING_PICKUP;
            case "SHIPPING", "IN_TRANSIT", "DELIVERY", "ON_DELIVERY" -> DELIVERING;
            case "SUCCESS", "COMPLETED", "COMPLETE" -> DELIVERED;
            case "DELIVERY_FAILED", "FAIL", "RETURNED", "RETURNING" -> FAILED;

            default -> PENDING_PICKUP;
        };
    }

    public static String normalizeCode(String rawCode) {
        return fromCode(rawCode).getCode();
    }

    public static String labelOf(String rawCode) {
        return fromCode(rawCode).getLabel();
    }

    public static String cssClassOf(String rawCode) {
        return fromCode(rawCode).getCssClass();
    }

    public static int stepOf(String rawCode) {
        return fromCode(rawCode).getStep();
    }

    public static boolean isTerminal(String rawCode) {
        return fromCode(rawCode).isTerminal();
    }

    public static boolean isSuccess(String rawCode) {
        return fromCode(rawCode).isSuccess();
    }

    public static boolean isFailure(String rawCode) {
        return fromCode(rawCode).isFailure();
    }

    private static String normalizeRaw(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            return null;
        }

        return rawCode.trim().toUpperCase(Locale.ROOT);
    }
}
