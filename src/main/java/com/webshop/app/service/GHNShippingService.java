package com.webshop.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Order;
import com.webshop.app.utils.GHNConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tích hợp GHN thật.
 *
 * Nếu thiếu token, shopId, fromDistrictId hoặc toDistrictId/toWardCode
 * thì trả về unavailable để checkout fallback sang phí nội bộ.
 */
public class GHNShippingService {

    private final GHNClient ghnClient = new GHNClient();

    public boolean isReadyForApi() {
        return GHNConfig.isConfigured();
    }

    public boolean canQuoteWithDefaultReceiver() {
        return GHNConfig.isConfigured()
                && GHNConfig.hasPickupAddressCode()
                && GHNConfig.hasDemoReceiverCode();
    }

    public FeeResult calculateDemoFee(BigDecimal insuranceValue) {
        if (!canQuoteWithDefaultReceiver()) {
            return FeeResult.unavailable("Thiếu fromDistrictId/fromWardCode hoặc demoToDistrictId/demoToWardCode.");
        }

        return calculateFee(
                GHNConfig.FROM_DISTRICT_ID,
                GHNConfig.FROM_WARD_CODE,
                GHNConfig.DEMO_TO_DISTRICT_ID,
                GHNConfig.DEMO_TO_WARD_CODE,
                insuranceValue
        );
    }

    public FeeResult calculateFee(int fromDistrictId,
                                  String fromWardCode,
                                  int toDistrictId,
                                  String toWardCode,
                                  BigDecimal insuranceValue) {
        try {
            if (!GHNConfig.isConfigured()) {
                return FeeResult.unavailable("GHN chưa cấu hình.");
            }

            if (fromDistrictId <= 0 || toDistrictId <= 0 || isBlank(toWardCode)) {
                return FeeResult.unavailable("Thiếu mã quận/phường GHN.");
            }

            int serviceId = resolveServiceId(fromDistrictId, toDistrictId);
            BigDecimal safeInsurance = money0(insuranceValue);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("from_district_id", fromDistrictId);
            body.put("from_ward_code", blankToNull(fromWardCode));
            body.put("service_id", serviceId);
            body.put("service_type_id", null);
            body.put("to_district_id", toDistrictId);
            body.put("to_ward_code", toWardCode);
            body.put("height", GHNConfig.DEFAULT_HEIGHT);
            body.put("length", GHNConfig.DEFAULT_LENGTH);
            body.put("weight", GHNConfig.DEFAULT_WEIGHT);
            body.put("width", GHNConfig.DEFAULT_WIDTH);
            body.put("insurance_value", safeInsurance.intValue());
            body.put("coupon", null);
            body.put("items", defaultItems());

            JsonNode root = ghnClient.postWithShop("/v2/shipping-order/fee", body);
            JsonNode data = root.path("data");

            BigDecimal totalFee = BigDecimal.valueOf(data.path("total").asLong(0));
            BigDecimal serviceFee = BigDecimal.valueOf(data.path("service_fee").asLong(totalFee.longValue()));

            return FeeResult.success(money0(totalFee), serviceFee, serviceId);
        } catch (Exception e) {
            return FeeResult.unavailable(e.getMessage());
        }
    }

    public CreateOrderResult createOrder(Order order,
                                         List<CartItem> items,
                                         int toDistrictId,
                                         String toWardCode) {
        try {
            if (!GHNConfig.isConfigured()) {
                return CreateOrderResult.failed("GHN chưa cấu hình.");
            }

            if (!GHNConfig.hasPickupAddressCode()) {
                return CreateOrderResult.failed("Thiếu mã quận/phường lấy hàng của shop.");
            }

            if (order == null || order.getId() <= 0) {
                return CreateOrderResult.failed("Thiếu thông tin đơn hàng.");
            }

            if (toDistrictId <= 0 || isBlank(toWardCode)) {
                if (!GHNConfig.hasDemoReceiverCode()) {
                    return CreateOrderResult.failed("Thiếu mã quận/phường nhận hàng GHN.");
                }

                toDistrictId = GHNConfig.DEMO_TO_DISTRICT_ID;
                toWardCode = GHNConfig.DEMO_TO_WARD_CODE;
            }

            int serviceId = resolveServiceId(GHNConfig.FROM_DISTRICT_ID, toDistrictId);
            int codAmount = GHNConfig.COD_ENABLED && "COD".equalsIgnoreCase(order.getPaymentMethod())
                    ? money0(order.getTotal()).intValue()
                    : 0;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("payment_type_id", 2);
            body.put("note", "MyCosmeticShop order #" + order.getId());
            body.put("required_note", GHNConfig.REQUIRED_NOTE);
            body.put("return_phone", GHNConfig.FROM_PHONE);
            body.put("return_address", GHNConfig.FROM_ADDRESS);
            body.put("return_district_id", GHNConfig.FROM_DISTRICT_ID);
            body.put("return_ward_code", GHNConfig.FROM_WARD_CODE);
            body.put("client_order_code", "MC-" + order.getId());

            body.put("from_name", GHNConfig.FROM_NAME);
            body.put("from_phone", GHNConfig.FROM_PHONE);
            body.put("from_address", GHNConfig.FROM_ADDRESS);
            body.put("from_ward_code", GHNConfig.FROM_WARD_CODE);
            body.put("from_district_id", GHNConfig.FROM_DISTRICT_ID);

            body.put("to_name", safe(order.getFullName(), "Khach hang"));
            body.put("to_phone", safe(order.getPhone(), "0900000000"));
            body.put("to_address", safe(order.getAddress(), "Dia chi nhan hang"));
            body.put("to_ward_code", toWardCode);
            body.put("to_district_id", toDistrictId);

            body.put("cod_amount", codAmount);
            body.put("content", "MyCosmeticShop cosmetics order #" + order.getId());
            body.put("weight", GHNConfig.DEFAULT_WEIGHT);
            body.put("length", GHNConfig.DEFAULT_LENGTH);
            body.put("width", GHNConfig.DEFAULT_WIDTH);
            body.put("height", GHNConfig.DEFAULT_HEIGHT);
            body.put("insurance_value", Math.max(GHNConfig.INSURANCE_VALUE, money0(order.getTotal()).intValue()));
            body.put("service_id", serviceId);
            body.put("service_type_id", 2);
            body.put("coupon", null);
            body.put("items", toGhnItems(items));

            JsonNode root = ghnClient.postWithShop("/v2/shipping-order/create", body);
            JsonNode data = root.path("data");

            String orderCode = data.path("order_code").asText("");
            BigDecimal totalFee = BigDecimal.valueOf(data.path("total_fee").asLong(order.getShippingFee().longValue()));

            if (isBlank(orderCode)) {
                return CreateOrderResult.failed("GHN không trả mã vận đơn.");
            }

            return CreateOrderResult.success(orderCode, money0(totalFee), serviceId);
        } catch (Exception e) {
            return CreateOrderResult.failed(e.getMessage());
        }
    }

    public TrackingResult getOrderDetail(String orderCode) {
        try {
            if (!GHNConfig.isConfigured()) {
                return TrackingResult.failed("GHN chưa cấu hình.");
            }

            if (isBlank(orderCode)) {
                return TrackingResult.failed("Thiếu order_code GHN.");
            }

            JsonNode root = ghnClient.post("/v2/shipping-order/detail", Map.of(
                    "order_code", orderCode.trim()
            ));

            JsonNode data = root.path("data");

            return TrackingResult.success(
                    data.path("order_code").asText(orderCode),
                    data.path("status").asText(""),
                    data.path("leadtime").asText("")
            );
        } catch (Exception e) {
            return TrackingResult.failed(e.getMessage());
        }
    }

    private int resolveServiceId(int fromDistrictId, int toDistrictId) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("shop_id", GHNConfig.GHN_SHOP_ID);
            body.put("from_district", fromDistrictId);
            body.put("to_district", toDistrictId);

            JsonNode root = ghnClient.post("/v2/shipping-order/available-services", body);
            JsonNode data = root.path("data");

            if (data.isArray()) {
                for (JsonNode item : data) {
                    int serviceTypeId = item.path("service_type_id").asInt(0);

                    if (serviceTypeId == 2) {
                        return item.path("service_id").asInt(0);
                    }
                }

                if (!data.isEmpty()) {
                    return data.get(0).path("service_id").asInt(0);
                }
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private List<Map<String, Object>> defaultItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new LinkedHashMap<>();

        item.put("name", "MyCosmeticShop item");
        item.put("quantity", 1);
        item.put("height", GHNConfig.DEFAULT_HEIGHT);
        item.put("weight", GHNConfig.DEFAULT_WEIGHT);
        item.put("length", GHNConfig.DEFAULT_LENGTH);
        item.put("width", GHNConfig.DEFAULT_WIDTH);

        items.add(item);

        return items;
    }

    private List<Map<String, Object>> toGhnItems(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return defaultItems();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            if (cartItem == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", safe(resolveProductName(cartItem), "MyCosmeticShop item"));
            item.put("quantity", Math.max(cartItem.getQuantity(), 1));
            item.put("height", GHNConfig.DEFAULT_HEIGHT);
            item.put("weight", GHNConfig.DEFAULT_WEIGHT);
            item.put("length", GHNConfig.DEFAULT_LENGTH);
            item.put("width", GHNConfig.DEFAULT_WIDTH);

            result.add(item);
        }

        return result.isEmpty() ? defaultItems() : result;
    }

    private String resolveProductName(CartItem cartItem) {
        if (cartItem == null) {
            return "MyCosmeticShop item";
        }

        if (!isBlank(cartItem.getTitle())) {
            return cartItem.getTitle();
        }

        if (!isBlank(cartItem.getVariantDisplayName())
                && !"Mặc định".equalsIgnoreCase(cartItem.getVariantDisplayName())) {
            return "MyCosmeticShop - " + cartItem.getVariantDisplayName();
        }

        return "MyCosmeticShop item";
    }

    private BigDecimal money0(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private Object blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String safe(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class FeeResult {
        private final boolean available;
        private final BigDecimal totalFee;
        private final BigDecimal serviceFee;
        private final int serviceId;
        private final String message;

        private FeeResult(boolean available,
                          BigDecimal totalFee,
                          BigDecimal serviceFee,
                          int serviceId,
                          String message) {
            this.available = available;
            this.totalFee = totalFee == null ? BigDecimal.ZERO : totalFee;
            this.serviceFee = serviceFee == null ? BigDecimal.ZERO : serviceFee;
            this.serviceId = serviceId;
            this.message = message;
        }

        public static FeeResult success(BigDecimal totalFee, BigDecimal serviceFee, int serviceId) {
            return new FeeResult(true, totalFee, serviceFee, serviceId, "");
        }

        public static FeeResult unavailable(String message) {
            return new FeeResult(false, BigDecimal.ZERO, BigDecimal.ZERO, 0, message);
        }

        public boolean isAvailable() {
            return available;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public BigDecimal getServiceFee() {
            return serviceFee;
        }

        public int getServiceId() {
            return serviceId;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class CreateOrderResult {
        private final boolean success;
        private final String orderCode;
        private final BigDecimal totalFee;
        private final int serviceId;
        private final String message;

        private CreateOrderResult(boolean success,
                                  String orderCode,
                                  BigDecimal totalFee,
                                  int serviceId,
                                  String message) {
            this.success = success;
            this.orderCode = orderCode;
            this.totalFee = totalFee == null ? BigDecimal.ZERO : totalFee;
            this.serviceId = serviceId;
            this.message = message;
        }

        public static CreateOrderResult success(String orderCode, BigDecimal totalFee, int serviceId) {
            return new CreateOrderResult(true, orderCode, totalFee, serviceId, "");
        }

        public static CreateOrderResult failed(String message) {
            return new CreateOrderResult(false, "", BigDecimal.ZERO, 0, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public BigDecimal getTotalFee() {
            return totalFee;
        }

        public int getServiceId() {
            return serviceId;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class TrackingResult {
        private final boolean success;
        private final String orderCode;
        private final String status;
        private final String leadTime;
        private final String message;

        private TrackingResult(boolean success,
                               String orderCode,
                               String status,
                               String leadTime,
                               String message) {
            this.success = success;
            this.orderCode = orderCode;
            this.status = status;
            this.leadTime = leadTime;
            this.message = message;
        }

        public static TrackingResult success(String orderCode, String status, String leadTime) {
            return new TrackingResult(true, orderCode, status, leadTime, "");
        }

        public static TrackingResult failed(String message) {
            return new TrackingResult(false, "", "", "", message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public String getStatus() {
            return status;
        }

        public String getLeadTime() {
            return leadTime;
        }

        public String getMessage() {
            return message;
        }
    }
}
