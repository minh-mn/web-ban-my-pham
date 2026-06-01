package com.webshop.app.controller.AjaxController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.model.User;
import com.webshop.app.service.FlashSaleLimitService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ajax/checkout-quantity")
public class AjaxCheckoutQuantityServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FlashSaleLimitService flashSaleLimitService = new FlashSaleLimitService();

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

        /*
         * Issue 132:
         * Nếu user đã đăng nhập nhưng session cart chưa được nạp,
         * lấy lại giỏ hàng đã lưu trong database trước khi cập nhật số lượng.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

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

            int newQuantity = quantity + 1;
            int userId = getCurrentUserId(session);

            /*
             * Issue 139:
             * Chặn tăng số lượng Flash Sale tại checkout.
             *
             * Đây là tầng chặn bổ sung sau CartServlet/CartIncreaseServlet.
             * Nếu user bypass giao diện giỏ hàng và gọi AJAX checkout trực tiếp,
             * servlet này vẫn không cho vượt max_quantity_per_user.
             */
            FlashSaleLimitService.LimitResult limitResult =
                    flashSaleLimitService.checkCanSetQuantity(
                            userId,
                            cart,
                            cartKey,
                            item.getProductId(),
                            newQuantity
                    );

            if (!limitResult.isAllowed()) {
                session.setAttribute("cartError", limitResult.getMessage());
                session.setAttribute("flashSaleLimitError", limitResult.getMessage());

                try {
                    flashSaleLimitService.enrichCartItems(userId, cart);
                } catch (Exception e) {
                    System.out.println("[AjaxCheckoutQuantityServlet] enrich flash sale limit skipped: " + e.getMessage());
                }

                writeLimitError(resp, cartKey, item, limitResult.getMessage());
                return;
            }

            quantity = newQuantity;
        }

        if ("decrease".equals(action)) {
            quantity--;

            if (quantity < 1) {
                quantity = 1;
            }
        }

        item.setQuantity(quantity);
        cart.put(cartKey, item);
        session.setAttribute(CartUtil.CART_SESSION_KEY, cart);

        // Nếu project có lưu selectedCart trong session thì đồng bộ luôn.
        Object selectedCartObj = session.getAttribute("selectedCart");
        if (selectedCartObj instanceof Map<?, ?> selectedMap) {
            Object selectedItem = selectedMap.get(cartKey);

            if (selectedItem instanceof CartItem selectedCartItem) {
                selectedCartItem.setQuantity(quantity);
            }
        }

        int userId = getCurrentUserId(session);

        try {
            flashSaleLimitService.enrichCartItems(userId, cart);
        } catch (Exception e) {
            System.out.println("[AjaxCheckoutQuantityServlet] enrich flash sale limit skipped: " + e.getMessage());
        }

        /*
         * Lưu lại database sau khi cập nhật AJAX.
         * Nếu user logout/login hoặc reset session thì số lượng mới vẫn được giữ.
         */
        CartUtil.saveCartForLoggedUser(session);

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
                + "\"canIncrease\":" + item.getCanIncreaseQuantity() + ","
                + "\"flashSaleItem\":" + item.getFlashSaleItem() + ","
                + "\"flashSaleLimitReached\":" + item.getFlashSaleLimitReached() + ","
                + "\"flashSaleLimitMessage\":\"" + escapeJson(item.getFlashSaleLimitMessage()) + "\","
                + "\"message\":\"" + escapeJson(message == null ? "" : message) + "\""
                + "}";

        resp.getWriter().write(json);
    }

    private void writeLimitError(HttpServletResponse resp,
                                 String cartKey,
                                 CartItem item,
                                 String message) throws IOException {

        BigDecimal itemSubtotal = item.getSubtotal() != null
                ? item.getSubtotal()
                : BigDecimal.ZERO;

        String json = "{"
                + "\"ok\":false,"
                + "\"flashLimit\":true,"
                + "\"key\":\"" + escapeJson(cartKey) + "\","
                + "\"productId\":" + item.getProductId() + ","
                + "\"quantity\":" + item.getQuantity() + ","
                + "\"stock\":" + item.getStock() + ","
                + "\"itemSubtotal\":" + itemSubtotal.toPlainString() + ","
                + "\"redirect\":\"cart?flashLimit=1&message=" + escapeJson(urlEncode(message)) + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
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

    private int getCurrentUserId(HttpSession session) {
        if (session == null) {
            return 0;
        }

        Object rawUser = session.getAttribute("user");

        if (!(rawUser instanceof User)) {
            rawUser = session.getAttribute("currentUser");
        }

        if (!(rawUser instanceof User)) {
            rawUser = session.getAttribute("authUser");
        }

        if (rawUser instanceof User) {
            return ((User) rawUser).getId();
        }

        return 0;
    }

    private String urlEncode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private int parseInt(String raw, int def) {
        try {
            if (raw == null || raw.isBlank()) {
                return def;
            }

            return Integer.parseInt(raw.trim());
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
