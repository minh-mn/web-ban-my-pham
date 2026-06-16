package com.webshop.app.service;

import com.webshop.app.utils.GHNConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ShippingService gom toàn bộ logic vận chuyển cho checkout.
 *
 * Đáp ứng các yêu cầu:
 * 83. Chọn đơn vị vận chuyển: GHTK / GHN / INTERNAL.
 * 84. Tính phí theo khu vực: TP.HCM và tỉnh khác.
 * 85. Lựa chọn phương thức: ECONOMY / FAST / EXPRESS.
 * 86. Tracking: sinh metadata provider/method/status để OrderDAO/Admin/OrderDetail theo dõi.
 * 87. Tích hợp API GHTK/GHN: tách provider code rõ ràng để sau này gọi API thật.
 * 88. Freeship: tự tính theo tổng sau voucher, không tin phí client gửi lên.
 */
public class ShippingService {

    private final GHNShippingService ghnShippingService = new GHNShippingService();

    public static final String PROVIDER_GHTK = "GHTK";
    public static final String PROVIDER_GHN = "GHN";
    public static final String PROVIDER_INTERNAL = "INTERNAL";

    public static final String METHOD_ECONOMY = "ECONOMY";
    public static final String METHOD_FAST = "FAST";
    public static final String METHOD_EXPRESS = "EXPRESS";

    public static final BigDecimal FREE_SHIP_THRESHOLD = new BigDecimal("500000");

    private static final BigDecimal HCM_ECONOMY_FEE = new BigDecimal("20000");
    private static final BigDecimal HCM_FAST_FEE = new BigDecimal("35000");
    private static final BigDecimal HCM_EXPRESS_FEE = new BigDecimal("50000");

    private static final BigDecimal OTHER_ECONOMY_FEE = new BigDecimal("35000");
    private static final BigDecimal OTHER_FAST_FEE = new BigDecimal("50000");

    public ShippingQuote quote(String province,
                               String provider,
                               String method,
                               BigDecimal amountAfterCoupon) {

        String normalizedMethod = normalizeMethod(method);
        String normalizedProvider = normalizeProvider(provider);

        if (normalizedProvider.isBlank()) {
            normalizedProvider = defaultProviderForMethod(normalizedMethod);
        }

        boolean hcm = isHcmCity(province);

        if (!isSupported(normalizedProvider, normalizedMethod, province)) {
            throw new IllegalArgumentException(
                    "Phương thức vận chuyển không hỗ trợ khu vực đã chọn. Vui lòng chọn đơn vị vận chuyển khác."
            );
        }

        BigDecimal fee = baseFee(normalizedProvider, normalizedMethod, hcm);
        boolean freeShip = isFreeShipEligible(amountAfterCoupon);

        /*
         * Nếu chọn GHN và đã cấu hình đủ mã GHN trong ghn.properties,
         * hệ thống gọi API GHN Staging để lấy phí thật.
         * Nếu API thiếu cấu hình/lỗi mạng, checkout tự fallback về phí nội bộ.
         */
        if (PROVIDER_GHN.equals(normalizedProvider) && !freeShip) {
            GHNShippingService.FeeResult ghnFee = ghnShippingService.calculateDemoFee(amountAfterCoupon);

            if (ghnFee.isAvailable() && ghnFee.getTotalFee().compareTo(BigDecimal.ZERO) > 0) {
                fee = ghnFee.getTotalFee();
            }
        }

        if (freeShip) {
            fee = BigDecimal.ZERO;
        }

        return new ShippingQuote(
                normalizedProvider,
                providerLabel(normalizedProvider),
                normalizedMethod,
                methodLabel(normalizedMethod),
                hcm ? "HCM" : "OTHER",
                hcm ? "TP.HCM" : "Tỉnh/thành khác",
                money0(fee),
                estimateTimeLabel(normalizedProvider, normalizedMethod, hcm),
                freeShip,
                true,
                trackingPrefix(normalizedProvider)
        );
    }


    public ShippingQuote quoteGhnReal(String province,
                                      String method,
                                      BigDecimal amountAfterCoupon,
                                      int toDistrictId,
                                      String toWardCode) {
        String normalizedMethod = normalizeMethod(method);

        if (!METHOD_FAST.equals(normalizedMethod)) {
            normalizedMethod = METHOD_FAST;
        }

        if (!GHNConfig.isConfigured()) {
            throw new IllegalArgumentException("GHN chưa cấu hình token hoặc shopId, chưa thể tính phí thật theo địa chỉ.");
        }

        if (!GHNConfig.hasPickupAddressCode()) {
            throw new IllegalArgumentException("Thiếu fromDistrictId/fromWardCode của shop, chưa thể tính phí GHN thật.");
        }

        if (toDistrictId <= 0 || toWardCode == null || toWardCode.isBlank()) {
            throw new IllegalArgumentException("Hệ thống chưa xác định được khu vực giao hàng từ địa chỉ đã chọn, chưa thể tính phí vận chuyển thật.");
        }

        boolean hcm = isHcmCity(province);
        boolean freeShip = isFreeShipEligible(amountAfterCoupon);
        BigDecimal fee = BigDecimal.ZERO;
        String estimatedTime = hcm
                ? "GHN · phí tính theo địa chỉ: 1 - 3 ngày"
                : "GHN · phí tính theo địa chỉ: 1 - 3 ngày";

        if (!freeShip) {
            GHNShippingService.FeeResult ghnFee = ghnShippingService.calculateFee(
                    GHNConfig.FROM_DISTRICT_ID,
                    GHNConfig.FROM_WARD_CODE,
                    toDistrictId,
                    toWardCode,
                    amountAfterCoupon
            );

            if (ghnFee.isAvailable() && ghnFee.getTotalFee().compareTo(BigDecimal.ZERO) > 0) {
                fee = ghnFee.getTotalFee();
            } else {
                throw new IllegalArgumentException(
                        ghnFee.getMessage() == null || ghnFee.getMessage().isBlank()
                                ? "GHN chưa trả được phí vận chuyển theo địa chỉ này."
                                : ghnFee.getMessage()
                );
            }
        }

        return new ShippingQuote(
                PROVIDER_GHN,
                providerLabel(PROVIDER_GHN),
                METHOD_FAST,
                methodLabel(METHOD_FAST),
                hcm ? "HCM" : "OTHER",
                hcm ? "TP.HCM" : "Tỉnh/thành khác",
                money0(freeShip ? BigDecimal.ZERO : fee),
                freeShip
                        ? "Đơn đủ điều kiện freeship, GHN vẫn được chọn để tạo vận đơn"
                        : estimatedTime,
                freeShip,
                true,
                trackingPrefix(PROVIDER_GHN)
        );
    }

    public List<ShippingQuote> availableQuotes(String province,
                                               BigDecimal amountAfterCoupon) {
        List<ShippingQuote> quotes = new ArrayList<>();

        addIfSupported(quotes, province, PROVIDER_GHTK, METHOD_ECONOMY, amountAfterCoupon);
        addIfSupported(quotes, province, PROVIDER_GHN, METHOD_FAST, amountAfterCoupon);
        addIfSupported(quotes, province, PROVIDER_INTERNAL, METHOD_EXPRESS, amountAfterCoupon);

        return quotes;
    }

    private void addIfSupported(List<ShippingQuote> quotes,
                                String province,
                                String provider,
                                String method,
                                BigDecimal amountAfterCoupon) {
        if (isSupported(provider, method, province)) {
            quotes.add(quote(province, provider, method, amountAfterCoupon));
        }
    }

    public boolean isSupported(String provider, String method, String province) {
        String normalizedProvider = normalizeProvider(provider);
        String normalizedMethod = normalizeMethod(method);
        boolean hcm = isHcmCity(province);

        if (PROVIDER_GHTK.equals(normalizedProvider)) {
            return METHOD_ECONOMY.equals(normalizedMethod);
        }

        if (PROVIDER_GHN.equals(normalizedProvider)) {
            return METHOD_FAST.equals(normalizedMethod);
        }

        if (PROVIDER_INTERNAL.equals(normalizedProvider)) {
            return METHOD_EXPRESS.equals(normalizedMethod) && hcm;
        }

        return false;
    }

    public String normalizeProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return "";
        }

        String value = provider.trim().toUpperCase(Locale.ROOT);

        return switch (value) {
            case PROVIDER_GHTK, PROVIDER_GHN, PROVIDER_INTERNAL -> value;
            case "GIAO_HANG_TIET_KIEM" -> PROVIDER_GHTK;
            case "GIAO_HANG_NHANH" -> PROVIDER_GHN;
            case "MYCOSMETIC", "NOI_BO", "INHOUSE" -> PROVIDER_INTERNAL;
            default -> "";
        };
    }

    public String normalizeMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return METHOD_ECONOMY;
        }

        String value = method.trim().toUpperCase(Locale.ROOT);

        return switch (value) {
            case METHOD_ECONOMY, METHOD_FAST, METHOD_EXPRESS -> value;
            default -> METHOD_ECONOMY;
        };
    }

    public String defaultProviderForMethod(String method) {
        String normalizedMethod = normalizeMethod(method);

        return switch (normalizedMethod) {
            case METHOD_FAST -> PROVIDER_GHN;
            case METHOD_EXPRESS -> PROVIDER_INTERNAL;
            case METHOD_ECONOMY -> PROVIDER_GHTK;
            default -> PROVIDER_GHTK;
        };
    }

    public String providerLabel(String provider) {
        String normalizedProvider = normalizeProvider(provider);

        return switch (normalizedProvider) {
            case PROVIDER_GHTK -> "Giao Hàng Tiết Kiệm";
            case PROVIDER_GHN -> "Giao Hàng Nhanh";
            case PROVIDER_INTERNAL -> "MyCosmetic Express";
            default -> "Đơn vị vận chuyển";
        };
    }

    public String methodLabel(String method) {
        String normalizedMethod = normalizeMethod(method);

        return switch (normalizedMethod) {
            case METHOD_FAST -> "Giao hàng nhanh";
            case METHOD_EXPRESS -> "Hỏa tốc";
            case METHOD_ECONOMY -> "Giao hàng tiết kiệm";
            default -> "Giao hàng tiết kiệm";
        };
    }

    public boolean isFreeShipEligible(BigDecimal amountAfterCoupon) {
        return money0(amountAfterCoupon).compareTo(FREE_SHIP_THRESHOLD) >= 0;
    }

    public boolean isHcmCity(String province) {
        String value = normalizeVietnameseText(province);

        return value.contains("ho chi minh")
                || value.contains("tp hcm")
                || value.contains("tphcm")
                || value.contains("thanh pho ho chi minh");
    }

    public String trackingPrefix(String provider) {
        String normalizedProvider = normalizeProvider(provider);

        return switch (normalizedProvider) {
            case PROVIDER_GHTK -> "GHTK";
            case PROVIDER_GHN -> "GHN";
            case PROVIDER_INTERNAL -> "MCX";
            default -> "MC";
        };
    }

    public String buildTrackingUrl(String provider, String shippingCode) {
        if (shippingCode == null || shippingCode.trim().isEmpty()) {
            return "";
        }

        String code = shippingCode.trim();
        String normalizedProvider = normalizeProvider(provider);

        return switch (normalizedProvider) {
            case PROVIDER_GHTK -> "https://i.ghtk.vn/" + code;
            case PROVIDER_GHN -> "https://donhang.ghn.vn/?order_code=" + code;
            default -> "";
        };
    }

    /**
     * Điểm mở rộng API thật.
     *
     * Hiện tại project chưa có token GHTK/GHN, nên checkout chỉ lưu provider/method/fee/status.
     * Khi có token, tạo class client gọi API và trả về mã vận đơn thật tại đây.
     */
    public ExternalShipmentResult createExternalShipmentStub(int orderId,
                                                             String provider,
                                                             String method) {
        String normalizedProvider = normalizeProvider(provider);

        if (orderId <= 0 || normalizedProvider.isBlank()) {
            return ExternalShipmentResult.failed("Không đủ dữ liệu tạo vận đơn.");
        }

        String generatedCode = trackingPrefix(normalizedProvider) + "-"
                + String.format("%06d", orderId);

        return ExternalShipmentResult.success(generatedCode, buildTrackingUrl(normalizedProvider, generatedCode));
    }

    private BigDecimal baseFee(String provider, String method, boolean hcm) {
        String normalizedProvider = normalizeProvider(provider);
        String normalizedMethod = normalizeMethod(method);

        if (PROVIDER_GHTK.equals(normalizedProvider) && METHOD_ECONOMY.equals(normalizedMethod)) {
            return hcm ? HCM_ECONOMY_FEE : OTHER_ECONOMY_FEE;
        }

        if (PROVIDER_GHN.equals(normalizedProvider) && METHOD_FAST.equals(normalizedMethod)) {
            return hcm ? HCM_FAST_FEE : OTHER_FAST_FEE;
        }

        if (PROVIDER_INTERNAL.equals(normalizedProvider) && METHOD_EXPRESS.equals(normalizedMethod) && hcm) {
            return HCM_EXPRESS_FEE;
        }

        throw new IllegalArgumentException("Cấu hình phí vận chuyển không hợp lệ.");
    }

    private String estimateTimeLabel(String provider, String method, boolean hcm) {
        String normalizedProvider = normalizeProvider(provider);
        String normalizedMethod = normalizeMethod(method);

        if (PROVIDER_GHTK.equals(normalizedProvider) && METHOD_ECONOMY.equals(normalizedMethod)) {
            return hcm ? "TP.HCM: 3 - 5 ngày" : "Tỉnh/thành khác: 3 - 5 ngày";
        }

        if (PROVIDER_GHN.equals(normalizedProvider) && METHOD_FAST.equals(normalizedMethod)) {
            return hcm ? "TP.HCM: 1 - 3 ngày" : "Tỉnh/thành khác: 1 - 3 ngày";
        }

        if (PROVIDER_INTERNAL.equals(normalizedProvider) && METHOD_EXPRESS.equals(normalizedMethod)) {
            return "Hỏa tốc trong ngày tại TP.HCM";
        }

        return "Thời gian dự kiến sẽ được cập nhật";
    }

    private BigDecimal money0(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String normalizeVietnameseText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace("đ", "d");
        normalized = normalized.replaceAll("[^a-z0-9\\s.]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    public static class ShippingQuote {
        private final String providerCode;
        private final String providerLabel;
        private final String methodCode;
        private final String methodLabel;
        private final String areaCode;
        private final String areaLabel;
        private final BigDecimal fee;
        private final String estimatedTime;
        private final boolean freeShip;
        private final boolean available;
        private final String trackingPrefix;

        public ShippingQuote(String providerCode,
                             String providerLabel,
                             String methodCode,
                             String methodLabel,
                             String areaCode,
                             String areaLabel,
                             BigDecimal fee,
                             String estimatedTime,
                             boolean freeShip,
                             boolean available,
                             String trackingPrefix) {
            this.providerCode = providerCode;
            this.providerLabel = providerLabel;
            this.methodCode = methodCode;
            this.methodLabel = methodLabel;
            this.areaCode = areaCode;
            this.areaLabel = areaLabel;
            this.fee = fee == null ? BigDecimal.ZERO : fee;
            this.estimatedTime = estimatedTime;
            this.freeShip = freeShip;
            this.available = available;
            this.trackingPrefix = trackingPrefix;
        }

        public String getProviderCode() {
            return providerCode;
        }

        public String getProviderLabel() {
            return providerLabel;
        }

        public String getMethodCode() {
            return methodCode;
        }

        public String getMethodLabel() {
            return methodLabel;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public String getAreaLabel() {
            return areaLabel;
        }

        public BigDecimal getFee() {
            return fee;
        }

        public String getEstimatedTime() {
            return estimatedTime;
        }

        public boolean isFreeShip() {
            return freeShip;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getTrackingPrefix() {
            return trackingPrefix;
        }
    }

    public static class ExternalShipmentResult {
        private final boolean success;
        private final String shippingCode;
        private final String trackingUrl;
        private final String message;

        private ExternalShipmentResult(boolean success,
                                       String shippingCode,
                                       String trackingUrl,
                                       String message) {
            this.success = success;
            this.shippingCode = shippingCode;
            this.trackingUrl = trackingUrl;
            this.message = message;
        }

        public static ExternalShipmentResult success(String shippingCode, String trackingUrl) {
            return new ExternalShipmentResult(true, shippingCode, trackingUrl, "");
        }

        public static ExternalShipmentResult failed(String message) {
            return new ExternalShipmentResult(false, "", "", message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getShippingCode() {
            return shippingCode;
        }

        public String getTrackingUrl() {
            return trackingUrl;
        }

        public String getMessage() {
            return message;
        }
    }
}
