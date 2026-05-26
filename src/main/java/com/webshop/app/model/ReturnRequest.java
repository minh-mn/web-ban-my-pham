package com.webshop.app.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class ReturnRequest {

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");

    private long id;
    private long orderId;
    private int userId;
    private String reason;
    private String status; // REQUESTED | APPROVED | REJECTED | RETURNED | REFUNDED
    private String adminNote;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private String refundMethod;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Integer processedBy;

    // Extra view fields from JOIN
    private String username;
    private String customerName;
    private BigDecimal orderTotal = BigDecimal.ZERO;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = Math.max(id, 0);
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = Math.max(orderId, 0);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = Math.max(userId, 0);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = normalizeNullable(reason);
    }

    public String getStatus() {
        return status == null || status.isBlank() ? "REQUESTED" : status;
    }

    public void setStatus(String status) {
        String value = normalizeUpper(status);
        this.status = value == null ? "REQUESTED" : value;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = normalizeNullable(adminNote);
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = safeMoney(refundAmount);
    }

    public String getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = normalizeUpper(refundMethod);
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizeNullable(username);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = normalizeNullable(customerName);
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = safeMoney(orderTotal);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = normalizeUpper(paymentMethod);
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = normalizeUpper(paymentStatus);
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = normalizeLower(orderStatus);
    }

    public String getStatusLabel() {
        return switch (getStatus().toUpperCase(Locale.ROOT)) {
            case "APPROVED" -> "Đã duyệt";
            case "REJECTED" -> "Đã từ chối";
            case "RETURNED" -> "Đã nhận hàng hoàn";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> "Chờ xử lý";
        };
    }

    public String getStatusCssClass() {
        return switch (getStatus().toUpperCase(Locale.ROOT)) {
            case "APPROVED", "RETURNED" -> "info";
            case "REJECTED" -> "danger";
            case "REFUNDED" -> "ok";
            default -> "warning";
        };
    }

    public String getRefundMethodLabel() {
        if (refundMethod == null || refundMethod.isBlank()) {
            return "Chưa xác định";
        }

        return switch (refundMethod.toUpperCase(Locale.ROOT)) {
            case "VNPAY" -> "Hoàn về VNPay";
            case "BANK_TRANSFER" -> "Chuyển khoản ngân hàng";
            case "CASH" -> "Tiền mặt";
            case "STORE_CREDIT" -> "Điểm / ví cửa hàng";
            case "MANUAL" -> "Xử lý thủ công";
            default -> refundMethod;
        };
    }

    public String getRefundAmountVnd() {
        return formatVnd(refundAmount);
    }

    public String getOrderTotalVnd() {
        return formatVnd(orderTotal);
    }

    public Date getRequestedAtDate() {
        return toDate(requestedAt);
    }

    public Date getProcessedAtDate() {
        return toDate(processedAt);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    private String formatVnd(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;

        NumberFormat formatter = NumberFormat.getInstance(VIETNAM_LOCALE);
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(0);

        return formatter.format(safeValue);
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String normalizeUpper(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLower(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
