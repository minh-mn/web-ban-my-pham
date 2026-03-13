package com.mycosmeticshop.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Utility hỗ trợ tích hợp VNPay
 *
 * Chức năng:
 * - Tạo HMAC SHA512 để ký dữ liệu
 * - Encode dữ liệu theo chuẩn RFC3986
 * - Build hash data / query string theo đúng thứ tự key
 * - Tạo thời gian theo format VNPay yêu cầu
 * - Lấy IP client
 * - Verify chữ ký VNPay trả về
 */
public class VNPayUtil {

    // =====================================================
    // HMAC SHA512
    // =====================================================

    /*
     * Tạo chữ ký HMAC SHA512
     *
     * @param key  khóa bí mật
     * @param data dữ liệu cần ký
     * @return chuỗi hash hex lowercase
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

            hmac.init(secretKey);

            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }

    // =====================================================
    // RFC3986 ENCODE
    // =====================================================

    /*
     * Encode theo chuẩn RFC3986
     *
     * Lưu ý:
     * - space phải là %20, không phải dấu +
     * - dùng cho cả key và value khi build dữ liệu VNPay
     *
     * @param value chuỗi cần encode
     * @return chuỗi đã encode
     */
    public static String encodeRFC3986(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    // =====================================================
    // BUILD DATA
    // =====================================================

    /*
     * Build chuỗi dữ liệu từ Map:
     * - sort key tăng dần
     * - encode cả key và value
     * - bỏ qua key/value null hoặc blank
     *
     * Ví dụ:
     * key1=value1&key2=value2
     *
     * @param params map tham số
     * @return chuỗi query/hash data
     */
    public static String buildData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();

        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }

            String value = params.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(encodeRFC3986(key))
                    .append("=")
                    .append(encodeRFC3986(value));
        }

        return sb.toString();
    }

    /*
     * Dữ liệu dùng để ký hash
     */
    public static String buildHashData(Map<String, String> params) {
        return buildData(params);
    }

    /*
     * Dữ liệu dùng để build query string gửi lên VNPay
     */
    public static String buildQueryString(Map<String, String> params) {
        return buildData(params);
    }

    // =====================================================
    // DATE TIME
    // =====================================================

    /*
     * Lấy thời gian hiện tại theo format VNPay yêu cầu
     * Format: yyyyMMddHHmmss
     */
    public static String nowVnp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /*
     * Lấy thời gian hiện tại + số phút chỉ định
     * Dùng cho expire date của VNPay
     *
     * @param minutes số phút cộng thêm
     * @return thời gian format yyyyMMddHHmmss
     */
    public static String plusMinutesVnp(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());
    }

    // =====================================================
    // CLIENT IP
    // =====================================================

    /*
     * Lấy IP client
     *
     * Ưu tiên:
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. req.getRemoteAddr()
     *
     * Nếu là localhost IPv6 thì chuẩn hóa về 127.0.0.1
     *
     * @param req HttpServletRequest hiện tại
     * @return IP client
     */
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

        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }

        // Chuẩn hóa localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    // =====================================================
    // VERIFY SIGNATURE
    // =====================================================

    /*
     * Kiểm tra chữ ký VNPay
     *
     * Quy trình:
     * - chỉ lấy các param bắt đầu bằng vnp_
     * - bỏ vnp_SecureHash và vnp_SecureHashType
     * - build hash data
     * - ký lại bằng VNP_HASH_SECRET
     * - so sánh với secureHash nhận được
     *
     * @param params      toàn bộ params nhận từ VNPay
     * @param secureHash  chữ ký VNPay gửi kèm
     * @return true nếu chữ ký hợp lệ
     */
    public static boolean verifySignature(Map<String, String> params, String secureHash) {
        if (secureHash == null || secureHash.isBlank()) {
            return false;
        }

        if (params == null || params.isEmpty()) {
            return false;
        }

        Map<String, String> copy = new HashMap<>();

        // Chỉ lấy param vnp_* để tránh nhiễu
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();

            if (k == null || !k.startsWith("vnp_")) {
                continue;
            }

            if (v == null || v.isBlank()) {
                continue;
            }

            copy.put(k, v);
        }

        // Không đưa 2 field này vào chuỗi ký
        copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");

        String signData = buildHashData(copy);
        String hash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, signData);

        return hash.equalsIgnoreCase(secureHash);
    }
}