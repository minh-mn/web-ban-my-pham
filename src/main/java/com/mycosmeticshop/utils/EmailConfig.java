package com.mycosmeticshop.utils;

//nzbt eqig hjqk pkhx

public class EmailConfig {

	public static final String SMTP_HOST = "smtp.gmail.com";
	public static final int SMTP_PORT = 587;

	public static final String SMTP_USERNAME = "mycosmetic0101@gmail.com";

	// Đọc từ biến môi trường tên: EMAIL_APP_PASSWORD
	public static final String SMTP_APP_PASSWORD = getRequiredEnv("EMAIL_APP_PASSWORD");

	public static final String FROM_NAME = "MyCosmeticShop";
	public static final String FROM_EMAIL = SMTP_USERNAME;

	public static final String APP_BASE_URL = "http://localhost:8080/MyCosmeticShop";
	public static final int RESET_TOKEN_EXPIRE_MINUTES = 15;

	private static String getRequiredEnv(String key) {
		String v = System.getenv(key);
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalStateException(
					"Missing environment variable: " + key + ". Please set it before starting Tomcat.");
		}
		return v.trim();
	}
}
