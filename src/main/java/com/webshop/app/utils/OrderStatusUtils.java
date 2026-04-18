package com.webshop.app.utils;

public class OrderStatusUtils {

	private OrderStatusUtils() {
		// Ngăn không cho khởi tạo
	}

	/**
	 * Chuyển status trong DB sang nhãn hiển thị tiếng Việt
	 */
	public static String toLabel(String status) {
		if (status == null)
			return "";

		switch (status) {
		case "processing":
			return "Đang xử lý";
		case "confirmed":
			return "Đã xác nhận";
		case "shipping":
			return "Đang giao hàng";
		case "completed":
			return "Hoàn tất";
		case "cancelled":
			return "Đã hủy";
		default:
			return status; // fallback an toàn
		}
	}

	/**
	 * (Tùy chọn) Map status sang CSS class để tô màu UI
	 */
	public static String toCssClass(String status) {
		if (status == null)
			return "status-default";

		switch (status) {
		case "processing":
			return "status-processing";
		case "confirmed":
			return "status-confirmed";
		case "shipping":
			return "status-shipping";
		case "completed":
			return "status-completed";
		case "cancelled":
			return "status-cancelled";
		default:
			return "status-default";
		}
	}
}
