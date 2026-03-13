package com.mycosmeticshop.utils;

/*
 * Utility hỗ trợ xử lý trạng thái đơn hàng
 *
 * Chức năng:
 * 1. Chuyển status trong database -> nhãn hiển thị tiếng Việt
 * 2. Chuyển status -> CSS class để hiển thị màu trên UI
 *
 * Ví dụ DB:
 * processing
 * confirmed
 * shipping
 * completed
 * cancelled
 */
public final class OrderStatusUtils {

	/*
	 * Constructor private để ngăn khởi tạo object
	 * Vì đây là utility class chỉ chứa static method
	 */
	private OrderStatusUtils() {}

	/**
	 * Chuyển status trong DB sang nhãn hiển thị tiếng Việt
	 *
	 * @param status trạng thái trong database
	 * @return nhãn hiển thị cho UI
	 */
	public static String toLabel(String status) {

		if (status == null) {
			return "";
		}

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
				// fallback nếu status mới chưa được map
				return status;
		}
	}

	/**
	 * Map status sang CSS class để hiển thị màu trong UI
	 *
	 * Ví dụ:
	 * <span class="status-processing">Đang xử lý</span>
	 *
	 * @param status trạng thái đơn hàng
	 * @return tên CSS class
	 */
	public static String toCssClass(String status) {

		if (status == null) {
			return "status-default";
		}

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