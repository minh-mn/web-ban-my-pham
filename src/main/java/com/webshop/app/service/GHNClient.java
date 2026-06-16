package com.webshop.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webshop.app.utils.GHNConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.Duration;
import java.util.Map;

/**
 * Client gọi GHN Staging/Production.
 *
 * GHN docs:
 * - available-services: /v2/shipping-order/available-services
 * - fee: /v2/shipping-order/fee
 * - create: /v2/shipping-order/create
 * - detail: /v2/shipping-order/detail
 */
public class GHNClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(20))
            .writeTimeout(Duration.ofSeconds(20))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode get(String path) {
        Request request = baseBuilder(path, false, false)
                .get()
                .build();

        return execute(request, path);
    }

    public JsonNode post(String path, Map<String, Object> body) {
        return post(path, body, false);
    }

    public JsonNode postWithShop(String path, Map<String, Object> body) {
        return post(path, body, true);
    }

    private JsonNode post(String path, Map<String, Object> body, boolean includeShopHeader) {
        try {
            String json = objectMapper.writeValueAsString(body == null ? Map.of() : body);

            Request request = baseBuilder(path, includeShopHeader)
                    .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                    .build();

            return execute(request, path);
        } catch (GHNApiException e) {
            throw e;
        } catch (Exception e) {
            throw new GHNApiException("Không tạo được request GHN: " + path, e);
        }
    }

    private Request.Builder baseBuilder(String path, boolean includeShopHeader) {
        return baseBuilder(path, includeShopHeader, true);
    }

    private Request.Builder baseBuilder(String path, boolean includeShopHeader, boolean requireShopId) {
        if (requireShopId) {
            if (!GHNConfig.isConfigured()) {
                throw new GHNApiException("GHN chưa được cấu hình token/shopId.");
            }
        } else if (!GHNConfig.hasApiToken()) {
            throw new GHNApiException("GHN chưa được cấu hình token API.");
        }

        String normalizedPath = path == null ? "" : path.trim();

        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        Request.Builder builder = new Request.Builder()
                .url(GHNConfig.GHN_BASE_URL + normalizedPath)
                .addHeader("Content-Type", "application/json")
                .addHeader("Token", GHNConfig.GHN_TOKEN);

        if (includeShopHeader) {
            builder.addHeader("ShopId", String.valueOf(GHNConfig.GHN_SHOP_ID));
        }

        return builder;
    }

    private JsonNode execute(Request request, String path) {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();

            if (!response.isSuccessful()) {
                throw new GHNApiException(
                        "GHN HTTP " + response.code() + " tại " + path + ": " + body
                );
            }

            JsonNode root = objectMapper.readTree(body);
            int code = root.path("code").asInt(response.code());

            if (code < 200 || code >= 300) {
                throw new GHNApiException(
                        "GHN API lỗi tại " + path + ": " + root.path("message").asText(body)
                );
            }

            return root;
        } catch (GHNApiException e) {
            throw e;
        } catch (Exception e) {
            throw new GHNApiException("Không gọi được GHN API: " + path, e);
        }
    }
}
