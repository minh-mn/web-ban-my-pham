package com.webshop.app.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;

public final class VNPayUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final TimeZone VN_TIME_ZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private VNPayUtil() {
    }

    public static String hmacSHA512(String key, String data) {
        try {
            if (isBlank(key)) {
                throw new IllegalArgumentException("VNPay HashSecret đang rỗng.");
            }

            Mac hmac512 = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA512
            );

            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký HMAC SHA512 cho VNPay.", e);
        }
    }

    /**
     * Giữ đúng kiểu Java sample của VNPay: URLEncoder, khoảng trắng thành '+'.
     * Không đổi '+' thành '%20'.
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * HashData:
     * - Sort field name A-Z.
     * - Bỏ vnp_SecureHash và vnp_SecureHashType.
     * - Field name giữ nguyên.
     * - Field value được encode.
     */
    public static String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            if (isBlank(fieldName)
                    || "vnp_SecureHash".equals(fieldName)
                    || "vnp_SecureHashType".equals(fieldName)) {
                continue;
            }

            String fieldValue = params.get(fieldName);
            if (isBlank(fieldValue)) {
                continue;
            }

            if (hashData.length() > 0) {
                hashData.append('&');
            }

            hashData.append(fieldName)
                    .append('=')
                    .append(encode(fieldValue));
        }

        return hashData.toString();
    }

    /**
     * QueryString:
     * - Sort field name A-Z.
     * - Encode field name và field value.
     * - Chưa gắn vnp_SecureHash tại đây.
     */
    public static String buildQueryString(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            if (isBlank(fieldName)
                    || "vnp_SecureHash".equals(fieldName)
                    || "vnp_SecureHashType".equals(fieldName)) {
                continue;
            }

            String fieldValue = params.get(fieldName);
            if (isBlank(fieldValue)) {
                continue;
            }

            if (query.length() > 0) {
                query.append('&');
            }

            query.append(encode(fieldName))
                    .append('=')
                    .append(encode(fieldValue));
        }

        return query.toString();
    }

    public static String nowVnp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(VN_TIME_ZONE);
        return formatter.format(new Date());
    }

    public static String plusMinutesVnp(int minutes) {
        Calendar calendar = Calendar.getInstance(VN_TIME_ZONE);
        calendar.add(Calendar.MINUTE, minutes);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(VN_TIME_ZONE);

        return formatter.format(calendar.getTime());
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (!isBlank(ip)) {
            ip = ip.split(",")[0].trim();
        }

        if (isBlank(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (isBlank(ip)) {
            ip = request.getRemoteAddr();
        }

        if (isBlank(ip)) {
            return "127.0.0.1";
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    public static boolean verifySignature(Map<String, String> params, String secureHash) {
        if (isBlank(secureHash) || params == null || params.isEmpty()) {
            return false;
        }

        Map<String, String> filtered = new HashMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (isBlank(key) || !key.startsWith("vnp_") || isBlank(value)) {
                continue;
            }

            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            filtered.put(key, value);
        }

        String signData = buildHashData(filtered);
        String expectedHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, signData);

        if (VNPayConfig.DEBUG) {
            System.out.println("===== VNPAY RETURN VERIFY DEBUG =====");
            System.out.println("signData=" + signData);
            System.out.println("expectedHash=" + expectedHash);
            System.out.println("vnpSecureHash=" + secureHash);
            System.out.println("valid=" + expectedHash.equalsIgnoreCase(secureHash));
            System.out.println("=====================================");
        }

        return expectedHash.equalsIgnoreCase(secureHash);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
