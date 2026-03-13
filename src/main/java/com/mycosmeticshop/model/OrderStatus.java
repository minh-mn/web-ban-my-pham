package com.mycosmeticshop.model;

import java.util.Arrays;
import java.util.List;

public enum OrderStatus {
	PROCESSING("processing", "Đang xử lý"), CONFIRMED("confirmed", "Đã xác nhận"), SHIPPING("shipping", "Đang giao"),
	COMPLETED("completed", "Giao thành công"), CANCELLED("cancelled", "Đã hủy");

	private final String key;
	private final String label;

	OrderStatus(String key, String label) {
		this.key = key;
		this.label = label;
	}

	public String getKey() {
		return key;
	}

	public String getLabel() {
		return label;
	}

	public static List<OrderStatus> choices() {
		return Arrays.asList(values());
	}

	public static String labelOf(String key) {
		if (key == null)
			return null;
		for (OrderStatus s : values()) {
			if (s.key.equalsIgnoreCase(key))
				return s.label;
		}
		return key;
	}

	public static boolean isValidKey(String key) {
	    if (key == null) return false;
	    for (var c : choices()) { // choices() trả list/map có key
	        if (key.equalsIgnoreCase(c.getKey())) return true; // tùy type của choices()
	    }
	    return false;
	}
}
