package com.webshop.app.controller.AjaxController;

import com.fasterxml.jackson.databind.JsonNode;
import com.webshop.app.service.GHNClient;
import com.webshop.app.service.GHNApiException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Proxy API địa chỉ GHN thật cho checkout.
 *
 * GET /api/ghn/master-data?type=province
 * GET /api/ghn/master-data?type=district&provinceId=202
 * GET /api/ghn/master-data?type=ward&districtId=3695
 */
@WebServlet("/api/ghn/master-data")
public class GHNMasterDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final GHNClient ghnClient = new GHNClient();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String type = trim(req.getParameter("type"));

        try {
            JsonNode result;

            switch (type) {
                case "province" -> result = ghnClient.get("/master-data/province");
                case "district" -> {
                    int provinceId = parseInt(req.getParameter("provinceId"), 0);

                    if (provinceId <= 0) {
                        writeError(resp, "Thiếu provinceId.");
                        return;
                    }

                    result = ghnClient.get("/master-data/district?province_id=" + provinceId);
                }
                case "ward" -> {
                    int districtId = parseInt(req.getParameter("districtId"), 0);

                    if (districtId <= 0) {
                        writeError(resp, "Thiếu districtId.");
                        return;
                    }

                    result = ghnClient.get("/master-data/ward?district_id=" + districtId);
                }
                default -> {
                    writeError(resp, "type không hợp lệ.");
                    return;
                }
            }

            resp.getWriter().write(result.toString());
        } catch (GHNApiException e) {
            writeError(resp, e.getMessage());
        } catch (Exception e) {
            writeError(resp, "Không tải được dữ liệu địa chỉ GHN.");
        }
    }

    private void writeError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"code\":400,\"message\":\""
                + escapeJson(message)
                + "\",\"data\":[]}");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(trim(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private String escapeJson(String value) {
        return trim(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
