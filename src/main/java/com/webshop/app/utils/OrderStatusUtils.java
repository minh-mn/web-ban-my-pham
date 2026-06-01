package com.webshop.app.utils;

import com.webshop.app.model.OrderStatus;
import com.webshop.app.model.ShippingStatus;

public class OrderStatusUtils {

    private OrderStatusUtils() {
        // Ngăn không cho khởi tạo
    }

    /* =========================================================
       ORDER STATUS
    ========================================================= */

    /**
     * Chuẩn hóa status trong DB.
     * Hỗ trợ cả dữ liệu cũ: pending -> processing, canceled -> cancelled.
     */
    public static String normalize(String status) {
        return OrderStatus.normalizeKey(status);
    }

    /**
     * Chuyển status trong DB sang nhãn hiển thị tiếng Việt.
     */
    public static String toLabel(String status) {
        return OrderStatus.labelOf(status);
    }

    /**
     * Map status sang CSS class để tô màu UI.
     */
    public static String toCssClass(String status) {
        return switch (normalize(status)) {
            case "processing" -> "status-processing";
            case "confirmed" -> "status-confirmed";
            case "shipping" -> "status-shipping";
            case "completed" -> "status-completed";
            case "cancelled" -> "status-cancelled";
            default -> "status-default";
        };
    }

    /**
     * CSS class ngắn dùng cho badge/card.
     */
    public static String toShortCssClass(String status) {
        return OrderStatus.cssClassOf(status);
    }

    /**
     * Step dùng để vẽ timeline:
     * 1: Chờ xác nhận
     * 2: Đã xác nhận
     * 3: Đang giao
     * 4: Giao thành công
     * -1: Đã hủy
     */
    public static int toStep(String status) {
        return OrderStatus.stepOf(status);
    }

    public static boolean isValid(String status) {
        return OrderStatus.isValidKey(status);
    }

    public static boolean isFinal(String status) {
        return OrderStatus.isFinalKey(status);
    }

    public static boolean isProcessing(String status) {
        return "processing".equals(normalize(status));
    }

    public static boolean isConfirmed(String status) {
        return "confirmed".equals(normalize(status));
    }

    public static boolean isShipping(String status) {
        return "shipping".equals(normalize(status));
    }

    public static boolean isCompleted(String status) {
        return "completed".equals(normalize(status));
    }

    public static boolean isCancelled(String status) {
        return "cancelled".equals(normalize(status));
    }

    public static boolean canConfirm(String status) {
        return OrderStatus.canConfirm(status);
    }

    public static boolean canStartShipping(String status) {
        return OrderStatus.canStartShipping(status);
    }

    public static boolean canComplete(String status) {
        return OrderStatus.canComplete(status);
    }

    public static boolean canCancel(String status) {
        return OrderStatus.canCancel(status);
    }

    public static boolean canMove(String currentStatus, String nextStatus) {
        return OrderStatus.canMove(currentStatus, nextStatus);
    }

    /* =========================================================
       ORDER TIMELINE FLAGS
    ========================================================= */

    public static boolean isStepProcessingDone(String status) {
        return toStep(status) >= 1;
    }

    public static boolean isStepConfirmedDone(String status) {
        return toStep(status) >= 2;
    }

    public static boolean isStepShippingDone(String status) {
        return toStep(status) >= 3;
    }

    public static boolean isStepCompletedDone(String status) {
        return toStep(status) >= 4;
    }

    /* =========================================================
       SHIPPING STATUS
    ========================================================= */

    public static String normalizeShippingStatus(String shippingStatus) {
        return ShippingStatus.normalizeCode(shippingStatus);
    }

    public static String shippingLabelOf(String shippingStatus) {
        return ShippingStatus.labelOf(shippingStatus);
    }

    public static String shippingCssClassOf(String shippingStatus) {
        return ShippingStatus.cssClassOf(shippingStatus);
    }

    public static int shippingStepOf(String shippingStatus) {
        return ShippingStatus.stepOf(shippingStatus);
    }

    public static boolean isPendingPickup(String shippingStatus) {
        return ShippingStatus.PENDING_PICKUP.matches(shippingStatus);
    }

    public static boolean isDelivering(String shippingStatus) {
        return ShippingStatus.DELIVERING.matches(shippingStatus);
    }

    public static boolean isDelivered(String shippingStatus) {
        return ShippingStatus.DELIVERED.matches(shippingStatus);
    }

    public static boolean isDeliveryFailed(String shippingStatus) {
        return ShippingStatus.FAILED.matches(shippingStatus);
    }

    public static boolean isShippingCanceled(String shippingStatus) {
        return ShippingStatus.CANCELED.matches(shippingStatus);
    }

    public static boolean isShippingFinished(String shippingStatus) {
        return ShippingStatus.fromCode(shippingStatus).isTerminal();
    }

    /* =========================================================
       PAYMENT STATUS
    ========================================================= */

    public static String normalizePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            return "PENDING";
        }

        String value = paymentStatus.trim().toUpperCase();

        if ("CANCELLED".equals(value)) {
            return "CANCELED";
        }

        return switch (value) {
            case "PENDING", "PAID", "FAILED", "CANCELED", "REFUNDED" -> value;
            default -> "PENDING";
        };
    }

    public static String paymentLabelOf(String paymentStatus) {
        return switch (normalizePaymentStatus(paymentStatus)) {
            case "PAID" -> "Đã thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "CANCELED" -> "Đã hủy thanh toán";
            case "REFUNDED" -> "Đã hoàn tiền";
            case "PENDING" -> "Chờ thanh toán";
            default -> "Chờ thanh toán";
        };
    }

    public static String paymentCssClassOf(String paymentStatus) {
        return switch (normalizePaymentStatus(paymentStatus)) {
            case "PAID" -> "ok";
            case "FAILED", "CANCELED" -> "danger";
            case "REFUNDED" -> "info";
            case "PENDING" -> "warning";
            default -> "warning";
        };
    }

    /* =========================================================
       SHIPPING PROVIDER / METHOD LABELS
    ========================================================= */

    public static String shippingProviderLabelOf(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return "Vận chuyển nội bộ";
        }

        return switch (provider.trim().toUpperCase()) {
            case "GHTK" -> "Giao hàng tiết kiệm";
            case "GHN" -> "Giao hàng nhanh";
            case "VIETTEL_POST" -> "Viettel Post";
            case "INTERNAL" -> "Vận chuyển nội bộ";
            case "OTHER" -> "Đơn vị vận chuyển khác";
            default -> provider.trim();
        };
    }

    public static String shippingMethodLabelOf(String method) {
        if (method == null || method.trim().isEmpty()) {
            return "Giao hàng tiết kiệm";
        }

        return switch (method.trim().toUpperCase()) {
            case "ECONOMY" -> "Giao hàng tiết kiệm";
            case "FAST" -> "Giao hàng nhanh";
            case "EXPRESS" -> "Hỏa tốc";
            default -> method.trim();
        };
    }
}
