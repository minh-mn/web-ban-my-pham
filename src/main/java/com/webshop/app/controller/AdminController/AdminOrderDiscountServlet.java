package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/order-discounts")
public class AdminOrderDiscountServlet extends HttpServlet {

    private static final String UNIFIED_PROMOTION_PATH = "/admin/promotions";
    private static final String PROMOTION_TYPE_PARAM = "type";
    private static final String ORDER_PROMOTION_TYPE = "ORDER";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        resp.sendRedirect(buildRedirectUrl(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        /*
         * Giữ tương thích cho các form cũ đang POST về /admin/order-discounts.
         * Request sẽ được forward nội bộ sang /admin/promotions,
         * đồng thời tự bổ sung type=ORDER để servlet quản lý khuyến mãi mới
         * xử lý đúng loại giảm giá theo giá trị đơn hàng.
         */
        HttpServletRequest wrappedRequest = new PromotionTypeRequestWrapper(req, ORDER_PROMOTION_TYPE);
        req.getRequestDispatcher(UNIFIED_PROMOTION_PATH).forward(wrappedRequest, resp);
    }

    private String buildRedirectUrl(HttpServletRequest req) {
        Map<String, String[]> params = new LinkedHashMap<>(req.getParameterMap());

        /*
         * Ép type=ORDER để mọi link cũ /admin/order-discounts đều được chuyển đúng tab
         * Giảm giá theo giá trị đơn hàng trong giao diện quản lý khuyến mãi tập trung.
         */
        params.put(PROMOTION_TYPE_PARAM, new String[]{ORDER_PROMOTION_TYPE});

        StringBuilder url = new StringBuilder();
        url.append(req.getContextPath()).append(UNIFIED_PROMOTION_PATH);

        String queryString = buildQueryString(params);
        if (!queryString.isBlank()) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    private String buildQueryString(Map<String, String[]> params) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();

            if (key == null || key.isBlank()) {
                continue;
            }

            if (values == null || values.length == 0) {
                appendParam(query, key, "");
                continue;
            }

            for (String value : values) {
                appendParam(query, key, value == null ? "" : value);
            }
        }

        return query.toString();
    }

    private void appendParam(StringBuilder query, String key, String value) {
        if (query.length() > 0) {
            query.append("&");
        }

        query.append(encode(key))
                .append("=")
                .append(encode(value));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class PromotionTypeRequestWrapper extends HttpServletRequestWrapper {

        private final String promotionType;

        PromotionTypeRequestWrapper(HttpServletRequest request, String promotionType) {
            super(request);
            this.promotionType = promotionType;
        }

        @Override
        public String getParameter(String name) {
            String[] values = getParameterValues(name);
            if (values == null || values.length == 0) {
                return null;
            }
            return values[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            if (PROMOTION_TYPE_PARAM.equals(name)) {
                return new String[]{promotionType};
            }

            return super.getParameterValues(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> mergedParams = new LinkedHashMap<>(super.getParameterMap());
            mergedParams.put(PROMOTION_TYPE_PARAM, new String[]{promotionType});
            return Collections.unmodifiableMap(mergedParams);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            Set<String> names = new LinkedHashSet<>();

            Enumeration<String> originalNames = super.getParameterNames();
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement());
            }

            names.add(PROMOTION_TYPE_PARAM);
            return Collections.enumeration(names);
        }
    }
}