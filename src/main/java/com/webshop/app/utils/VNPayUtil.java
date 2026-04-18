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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;

public class VNPayUtil {

    // ================= HMAC =================
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);

            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hash.append(String.format("%02x", b));
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }

    // ================= RFC3986 ENCODE =================
    // VNPAY thường yêu cầu encode kiểu RFC3986 (space = %20, không phải '+')
    public static String encodeRFC3986(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    // ================= BUILD DATA (SORT + ENCODE) =================
    // Encode cả key và value để chắc chắn đúng chuẩn trong mọi tình huống.
    public static String buildData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (key == null || key.isBlank()) continue;

            String value = params.get(key);
            if (value == null || value.isBlank()) continue;

            if (sb.length() > 0) sb.append("&");
            sb.append(encodeRFC3986(key)).append("=").append(encodeRFC3986(value));
        }
        return sb.toString();
    }

    // HashData và QueryString dùng chung format cho ổn định
    public static String buildHashData(Map<String, String> params) {
        return buildData(params);
    }

    public static String buildQueryString(Map<String, String> params) {
        return buildData(params);
    }

    // ================= DATE =================
    public static String nowVnp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String plusMinutesVnp(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());
    }

    // ================= IP =================
    // - ưu tiên X-Forwarded-For / X-Real-IP nếu deploy sau proxy
    // - nếu là IPv6 localhost thì trả 127.0.0.1
    public static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = req.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        }

        if (ip == null || ip.isBlank()) return "127.0.0.1";

        // normalize localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return "127.0.0.1";

        return ip;
    }

    // ================= VERIFY SIGNATURE =================
    public static boolean verifySignature(Map<String, String> params, String secureHash) {
        if (secureHash == null || secureHash.isBlank()) return false;
        if (params == null || params.isEmpty()) return false;

        Map<String, String> copy = new HashMap<>();

        // chỉ lấy vnp_* để tránh bị nhiễu
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (k == null || !k.startsWith("vnp_")) continue;
            if (v == null || v.isBlank()) continue;
            copy.put(k, v);
        }

        copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");

        String signData = buildHashData(copy);
        String hash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, signData);
        return hash.equalsIgnoreCase(secureHash);
    }
}
