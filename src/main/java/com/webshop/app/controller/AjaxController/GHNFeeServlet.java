package com.webshop.app.controller.AjaxController;

import com.webshop.app.service.GHNShippingService;
import com.webshop.app.service.GHNShippingService.FeeResult;
import com.webshop.app.utils.GHNConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Tính phí GHN thật theo mã quận/phường GHN.
 *
 * GET /api/ghn/fee?toDistrictId=3695&toWardCode=20306&insuranceValue=189800
 */
@WebServlet("/api/ghn/fee")
public class GHNFeeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final GHNShippingService ghnShippingService = new GHNShippingService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        int toDistrictId = parseInt(req.getParameter("toDistrictId"), 0);
        String toWardCode = trim(req.getParameter("toWardCode"));
        BigDecimal insuranceValue = parseMoney(req.getParameter("insuranceValue"));

        if (!GHNConfig.isConfigured()) {
            writeUnavailable(resp, "GHN chưa cấu hình token hoặc shopId.");
            return;
        }

        if (!GHNConfig.hasPickupAddressCode()) {
            writeUnavailable(resp, "Thiếu fromDistrictId/fromWardCode của shop trong ghn.properties.");
            return;
        }

        if (toDistrictId <= 0 || toWardCode.isBlank()) {
            writeUnavailable(resp, "Thiếu mã quận/phường nhận hàng GHN.");
            return;
        }

        FeeResult result = ghnShippingService.calculateFee(
                GHNConfig.FROM_DISTRICT_ID,
                GHNConfig.FROM_WARD_CODE,
                toDistrictId,
                toWardCode,
                insuranceValue
        );

        if (!result.isAvailable()) {
            writeUnavailable(resp, result.getMessage());
            return;
        }

        resp.getWriter().write("{"
                + "\"available\":true,"
                + "\"fee\":" + result.getTotalFee().toPlainString() + ","
                + "\"serviceFee\":" + result.getServiceFee().toPlainString() + ","
                + "\"serviceId\":" + result.getServiceId() + ","
                + "\"message\":\"Tính phí GHN thành công\""
                + "}");
    }

    private void writeUnavailable(HttpServletResponse resp, String message) throws IOException {
        resp.getWriter().write("{"
                + "\"available\":false,"
                + "\"fee\":0,"
                + "\"serviceFee\":0,"
                + "\"serviceId\":0,"
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}");
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private BigDecimal parseMoney(String value) {
        try {
            String clean = trim(value).replaceAll("[^0-9.]", "");

            if (clean.isBlank()) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal(clean);
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeJson(String value) {
        return trim(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
