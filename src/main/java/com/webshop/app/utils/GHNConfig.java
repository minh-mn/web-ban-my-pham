package com.webshop.app.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Đọc cấu hình GHN từ src/main/resources/ghn.properties.
 *
 * Ưu tiên biến môi trường:
 * - GHN_ENABLED
 * - GHN_BASE_URL
 * - GHN_TOKEN
 * - GHN_SHOP_ID
 * - GHN_FROM_DISTRICT_ID
 * - GHN_FROM_WARD_CODE
 *
 * Không hard-code token thật trong Java.
 */
public final class GHNConfig {

    private static final Properties PROPERTIES = loadProperties();

    public static final boolean GHN_ENABLED = boolValue("ghn.enabled", "GHN_ENABLED", true);
    public static final String GHN_ENV = value("ghn.env", "GHN_ENV", "staging");
    public static final String GHN_BASE_URL = trimSlash(
            value("ghn.baseUrl", "GHN_BASE_URL", "https://dev-online-gateway.ghn.vn/shiip/public-api")
    );
    public static final String GHN_TOKEN = valueAny(new String[]{"ghn.token", "ghn.apiToken", "ghn.api_token"}, new String[]{"GHN_TOKEN", "GHN_API_TOKEN"}, "");
    public static final int GHN_SHOP_ID = intValueAny(new String[]{"ghn.shopId", "ghn.shopID", "ghn.shop_id"}, new String[]{"GHN_SHOP_ID", "GHN_SHOPID"}, 0);

    public static final String FROM_NAME = value("ghn.fromName", "GHN_FROM_NAME", "MyCosmeticShop");
    public static final String FROM_PHONE = value("ghn.fromPhone", "GHN_FROM_PHONE", "0856272116");
    public static final String FROM_ADDRESS = value("ghn.fromAddress", "GHN_FROM_ADDRESS", "");
    public static final int FROM_DISTRICT_ID = intValue("ghn.fromDistrictId", "GHN_FROM_DISTRICT_ID", 0);
    public static final String FROM_WARD_CODE = value("ghn.fromWardCode", "GHN_FROM_WARD_CODE", "");

    public static final int DEMO_TO_DISTRICT_ID = intValue("ghn.demoToDistrictId", "GHN_DEMO_TO_DISTRICT_ID", 0);
    public static final String DEMO_TO_WARD_CODE = value("ghn.demoToWardCode", "GHN_DEMO_TO_WARD_CODE", "");

    public static final int DEFAULT_WEIGHT = intValue("ghn.defaultWeight", "GHN_DEFAULT_WEIGHT", 500);
    public static final int DEFAULT_LENGTH = intValue("ghn.defaultLength", "GHN_DEFAULT_LENGTH", 20);
    public static final int DEFAULT_WIDTH = intValue("ghn.defaultWidth", "GHN_DEFAULT_WIDTH", 15);
    public static final int DEFAULT_HEIGHT = intValue("ghn.defaultHeight", "GHN_DEFAULT_HEIGHT", 10);
    public static final int INSURANCE_VALUE = intValue("ghn.insuranceValue", "GHN_INSURANCE_VALUE", 100000);
    public static final boolean COD_ENABLED = boolValue("ghn.codEnabled", "GHN_COD_ENABLED", false);
    public static final String REQUIRED_NOTE = value("ghn.requiredNote", "GHN_REQUIRED_NOTE", "KHONGCHOXEMHANG");

    private GHNConfig() {
    }

    public static boolean hasApiToken() {
        String token = GHN_TOKEN == null ? "" : GHN_TOKEN.trim();

        return GHN_ENABLED
                && !isBlank(token)
                && !isPlaceholderToken(token);
    }

    public static boolean isConfigured() {
        return hasApiToken() && GHN_SHOP_ID > 0;
    }

    private static boolean isPlaceholderToken(String token) {
        String value = token == null ? "" : token.trim().toUpperCase();

        return value.contains("YOUR_GHN")
                || value.contains("TOKEN_API")
                || value.contains("PLACEHOLDER");
    }

    public static boolean hasPickupAddressCode() {
        return FROM_DISTRICT_ID > 0 && !isBlank(FROM_WARD_CODE);
    }

    public static boolean hasDemoReceiverCode() {
        return DEMO_TO_DISTRICT_ID > 0 && !isBlank(DEMO_TO_WARD_CODE);
    }

    public static String maskedToken() {
        if (isBlank(GHN_TOKEN)) {
            return "(empty)";
        }

        String token = GHN_TOKEN.trim();

        if (token.length() <= 12) {
            return "********";
        }

        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = GHNConfig.class
                .getClassLoader()
                .getResourceAsStream("ghn.properties")) {

            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception ignored) {
        }

        return properties;
    }

    private static String valueAny(String[] propertyKeys, String[] envKeys, String defaultValue) {
        if (envKeys != null) {
            for (String envKey : envKeys) {
                String envValue = System.getenv(envKey);

                if (!isBlank(envValue)) {
                    return envValue.trim();
                }
            }
        }

        if (propertyKeys != null) {
            for (String propertyKey : propertyKeys) {
                String propertyValue = PROPERTIES.getProperty(propertyKey);

                if (!isBlank(propertyValue)) {
                    return propertyValue.trim();
                }
            }
        }

        return defaultValue;
    }

    private static int intValueAny(String[] propertyKeys, String[] envKeys, int defaultValue) {
        String raw = valueAny(propertyKeys, envKeys, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static String value(String propertyKey, String envKey, String defaultValue) {
        String envValue = System.getenv(envKey);

        if (!isBlank(envValue)) {
            return envValue.trim();
        }

        String propertyValue = PROPERTIES.getProperty(propertyKey);

        if (!isBlank(propertyValue)) {
            return propertyValue.trim();
        }

        return defaultValue;
    }

    private static int intValue(String propertyKey, String envKey, int defaultValue) {
        String raw = value(propertyKey, envKey, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static boolean boolValue(String propertyKey, String envKey, boolean defaultValue) {
        String raw = value(propertyKey, envKey, String.valueOf(defaultValue));

        if (isBlank(raw)) {
            return defaultValue;
        }

        return "true".equalsIgnoreCase(raw)
                || "1".equals(raw.trim())
                || "yes".equalsIgnoreCase(raw)
                || "on".equalsIgnoreCase(raw);
    }

    private static String trimSlash(String value) {
        if (isBlank(value)) {
            return "";
        }

        String result = value.trim();

        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
