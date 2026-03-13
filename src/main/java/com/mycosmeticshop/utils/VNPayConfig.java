package com.mycosmeticshop.utils;

public class VNPayConfig {

    /* ================= BASIC ================= */

    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";

    /**
     * TMN CODE
     * - Sandbox / Production khác nhau
     * - Ưu tiên ENV trước
     */
    public static final String VNP_TMN_CODE =
            getEnv("VNP_TMN_CODE", "72GLCXDR");

    /**
     * HASH SECRET
     * ⚠️ TUYỆT ĐỐI KHÔNG COMMIT SECRET LÊN GIT
     * - Khi deploy: set ENV VNP_HASH_SECRET
     */
    public static final String VNP_HASH_SECRET =
            getEnv("VNP_HASH_SECRET", "OB9KWZAOLADRWI27ZXSF9VEH86RHFX35");

    /**
     * PAY URL
     * - Sandbox:
     *   https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
     * - Production:
     *   https://pay.vnpay.vn/vpcpay.html
     */
    public static final String VNP_PAY_URL =
            getEnv("VNP_PAY_URL",
                    "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

    /* ================= CALLBACK URL ================= */

    /**
     * Browser redirect (ReturnUrl)
     * - Phải public khi test production
     */
    public static final String VNP_RETURN_URL =
            getEnv("VNP_RETURN_URL",
                    "http://localhost:8080/MyCosmeticShop/payment/vnpay-return");

    /**
     * IPN (server-to-server)
     * - Localhost: KHÔNG HOẠT ĐỘNG
     * - Khi test IPN: dùng ngrok / domain public
     * - Có thể để null nếu chưa dùng
     */
    public static final String VNP_IPN_URL =
            getEnv("VNP_IPN_URL",
                    "http://localhost:8080/MyCosmeticShop/payment/vnpay-ipn");

    /**
     * Thời gian hiệu lực thanh toán (phút)
     */
    public static final int EXPIRE_MINUTES = 15;

    /* ================= HELPER ================= */

    private static String getEnv(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v.trim();
    }
}
