package com.webshop.app.utils;

import java.io.InputStream;
import java.util.Properties;

import jakarta.servlet.http.HttpServletRequest;

public final class VNPayConfig {

    private static final String CONFIG_FILE = "vnpay.properties";
    private static final Properties PROPERTIES = loadProperties();

    public static final String VNP_VERSION = getConfig("vnp.version", "VNP_VERSION", "2.1.0");
    public static final String VNP_COMMAND = getConfig("vnp.command", "VNP_COMMAND", "pay");
    public static final String VNP_TMN_CODE = getConfig("vnp.tmnCode", "VNP_TMN_CODE", "");
    public static final String VNP_HASH_SECRET = getConfig("vnp.hashSecret", "VNP_HASH_SECRET", "");

    public static final String VNP_PAY_URL = getConfig(
            "vnp.payUrl",
            "VNP_PAY_URL",
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    );

    public static final String VNP_RETURN_URL = getConfig("vnp.returnUrl", "VNP_RETURN_URL", "");
    public static final String VNP_IPN_URL = getConfig("vnp.ipnUrl", "VNP_IPN_URL", "");
    public static final String VNP_ORDER_TYPE = getConfig("vnp.orderType", "VNP_ORDER_TYPE", "other");
    public static final String VNP_LOCALE = getConfig("vnp.locale", "VNP_LOCALE", "vn");
    public static final String VNP_CURR_CODE = getConfig("vnp.currCode", "VNP_CURR_CODE", "VND");

    public static final int EXPIRE_MINUTES = parseInt(
            getConfig("vnp.expireMinutes", "VNP_EXPIRE_MINUTES", "15"),
            15
    );

    public static final boolean SEND_EXPIRE_DATE = Boolean.parseBoolean(
            getConfig("vnp.sendExpireDate", "VNP_SEND_EXPIRE_DATE", "true")
    );

    public static final boolean DEBUG = Boolean.parseBoolean(
            getConfig("vnp.debug", "VNP_DEBUG", "true")
    );

    private VNPayConfig() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream input = classLoader.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("[VNPayConfig] Không tìm thấy " + CONFIG_FILE + " trong classpath/WEB-INF/classes.");
                return props;
            }

            props.load(input);
            System.out.println("[VNPayConfig] Đã đọc cấu hình từ " + CONFIG_FILE + ".");
        } catch (Exception e) {
            System.err.println("[VNPayConfig] Lỗi đọc " + CONFIG_FILE + ": " + e.getMessage());
        }

        return props;
    }

    /**
     * Ưu tiên đọc từ vnpay.properties trước để tránh VM options / environment cũ đè cấu hình mới.
     */
    private static String getConfig(String propertyKey, String envKey, String defaultValue) {
        String propertyValue = PROPERTIES.getProperty(propertyKey);
        if (!VNPayUtil.isBlank(propertyValue)) {
            return propertyValue.trim();
        }

        String systemValue = System.getProperty(envKey);
        if (!VNPayUtil.isBlank(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(envKey);
        if (!VNPayUtil.isBlank(envValue)) {
            return envValue.trim();
        }

        return defaultValue == null ? "" : defaultValue.trim();
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean isConfigured() {
        return !VNPayUtil.isBlank(VNP_TMN_CODE)
                && !VNPayUtil.isBlank(VNP_HASH_SECRET)
                && !VNPayUtil.isBlank(VNP_PAY_URL)
                && !"DAN_SECRET_KEY_VNPAY_GUI_VAO_DAY".equalsIgnoreCase(VNP_HASH_SECRET);
    }

    public static String getConfigError() {
        StringBuilder builder = new StringBuilder();

        if (VNPayUtil.isBlank(VNP_TMN_CODE)) {
            builder.append("Thiếu vnp.tmnCode. ");
        }

        if (VNPayUtil.isBlank(VNP_HASH_SECRET)
                || "DAN_SECRET_KEY_VNPAY_GUI_VAO_DAY".equalsIgnoreCase(VNP_HASH_SECRET)) {
            builder.append("Thiếu vnp.hashSecret hoặc chưa thay Secret Key thật. ");
        }

        if (VNPayUtil.isBlank(VNP_PAY_URL)) {
            builder.append("Thiếu vnp.payUrl. ");
        }

        return builder.toString().trim();
    }

    public static String resolveReturnUrl(HttpServletRequest request) {
        if (!VNPayUtil.isBlank(VNP_RETURN_URL)) {
            return VNP_RETURN_URL;
        }

        return getBaseUrl(request) + request.getContextPath() + "/payment/vnpay-return";
    }

    public static String resolveIpnUrl(HttpServletRequest request) {
        if (!VNPayUtil.isBlank(VNP_IPN_URL)) {
            return VNP_IPN_URL;
        }

        return getBaseUrl(request) + request.getContextPath() + "/payment/vnpay-ipn";
    }

    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80)
                || ("https".equalsIgnoreCase(scheme) && serverPort == 443);

        return scheme + "://" + serverName + (defaultPort ? "" : ":" + serverPort);
    }

    public static String maskedSecret() {
        if (VNPayUtil.isBlank(VNP_HASH_SECRET)) {
            return "<blank>";
        }

        if (VNP_HASH_SECRET.length() <= 8) {
            return "****";
        }

        return VNP_HASH_SECRET.substring(0, 4)
                + "****"
                + VNP_HASH_SECRET.substring(VNP_HASH_SECRET.length() - 4);
    }
}
