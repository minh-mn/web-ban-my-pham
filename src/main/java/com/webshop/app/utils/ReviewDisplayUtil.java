package com.webshop.app.utils;

public final class ReviewDisplayUtil {
    private ReviewDisplayUtil() {
    }

    public static String maskUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Người dùng ẩn danh";
        }

        String value = username.trim();

        if (value.length() <= 1) {
            return value + "***";
        }

        if (value.length() <= 4) {
            return value.substring(0, 1) + "***" + value.substring(value.length() - 1);
        }

        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    public static String displayAuthorName(String username, boolean anonymous) {
        return anonymous ? maskUsername(username) : username;
    }
}
