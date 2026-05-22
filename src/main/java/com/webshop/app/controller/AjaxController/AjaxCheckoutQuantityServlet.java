package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ajax/checkout-quantity")
public class AjaxCheckoutQuantityServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null) {
            writeError(resp, "Phiên làm việc đã hết hạn.");
            return;
        }

        int productId = parseInt(req.getParameter("productId"), -1);
        String rawKey = req.getParameter("key");
        String action = req.getParameter("action");

        if (productId <= 0 || rawKey == null || rawKey.isBlank()) {
            writeError(resp, "Dữ liệu sản phẩm không hợp lệ.");
            return;
        }

        if (!"increase".equals(action) && !"decrease".equals(action)) {
            writeError(resp, "Hành động cập nhật không hợp lệ.");
            return;
        }

        Map<String, CartItem> cart = CartUtil.getCart(session);
        String cartKey = CartUtil.normalizeKey(rawKey, productId);

        CartItem item = cart.get(cartKey);

        if (item == null) {
            writeError(resp, "Không tìm thấy sản phẩm trong giỏ hàng.");
            return;
        }

        int quantity = item.getQuantity();
        int stock = item.getStock();

        if ("increase".equals(action)) {
            if (stock > 0 && quantity >= stock) {
                writeOk(resp, cartKey, item, "Số lượng đã đạt mức tồn kho tối đa.");
                return;
            }

            quantity++;
        }

        if ("decrease".equals(action)) {
            quantity--;

            if (quantity < 1) {
                quantity = 1;
            }
        }

        item.setQuantity(quantity);

        // Nếu project có lưu selectedCart trong session thì đồng bộ luôn.
        Object selectedCartObj = session.getAttribute("selectedCart");
        if (selectedCartObj instanceof Map<?, ?> selectedMap) {
            Object selectedItem = selectedMap.get(cartKey);

            if (selectedItem instanceof CartItem selectedCartItem) {
                selectedCartItem.setQuantity(quantity);
            }
        }

        writeOk(resp, cartKey, item, null);
    }

    private void writeOk(HttpServletResponse resp,
                         String cartKey,
                         CartItem item,
                         String message) throws IOException {

        BigDecimal itemSubtotal = item.getSubtotal() != null
                ? item.getSubtotal()
                : BigDecimal.ZERO;

        String json = "{"
                + "\"ok\":true,"
                + "\"key\":\"" + escapeJson(cartKey) + "\","
                + "\"productId\":" + item.getProductId() + ","
                + "\"quantity\":" + item.getQuantity() + ","
                + "\"stock\":" + item.getStock() + ","
                + "\"itemSubtotal\":" + itemSubtotal.toPlainString() + ","
                + "\"message\":\"" + escapeJson(message == null ? "" : message) + "\""
                + "}";

        resp.getWriter().write(json);
    }

    private void writeError(HttpServletResponse resp, String message)
            throws IOException {

        resp.getWriter().write(
                "{"
                        + "\"ok\":false,"
                        + "\"message\":\"" + escapeJson(message) + "\""
                        + "}"
        );
    }

    private int parseInt(String raw, int def) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }

        return raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}