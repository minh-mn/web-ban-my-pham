package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart/decrease")
public class CartDecreaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleDecrease(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleDecrease(req, resp);
    }

    private void handleDecrease(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        String requestedKey = CartUtil.normalizeKey(req.getParameter("key"), productId);

        HttpSession session = req.getSession();

        /*
         * Nếu user đã đăng nhập nhưng session chưa có cart mới nhất,
         * nạp lại dữ liệu đã lưu trong database trước khi giảm số lượng.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);
        CartLookup lookup = findCartItem(cart, requestedKey, productId);

        if (lookup.item != null) {
            CartItem item = lookup.item;
            String realKey = lookup.key;

            int newQuantity = item.getQuantity() - 1;

            /*
             * Giữ tối thiểu 1 sản phẩm. Muốn xóa khỏi giỏ thì dùng nút X.
             * Cách này tránh bấm nhầm dấu trừ làm mất sản phẩm khỏi giỏ.
             */
            if (newQuantity < 1) {
                newQuantity = 1;
            }

            item.setQuantity(newQuantity);
            item.setCartKey(realKey);
            cart.put(realKey, item);

            session.setAttribute(CartUtil.CART_SESSION_KEY, cart);
            CartUtil.saveCartForLoggedUser(session);
        } else {
            session.setAttribute("cartError", "Không tìm thấy sản phẩm trong giỏ hàng để giảm số lượng.");
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private CartLookup findCartItem(Map<String, CartItem> cart, String requestedKey, int productId) {
        if (cart == null || cart.isEmpty()) {
            return CartLookup.empty();
        }

        if (requestedKey != null && !requestedKey.isBlank()) {
            CartItem exactItem = cart.get(requestedKey);
            if (exactItem != null) {
                return new CartLookup(requestedKey, exactItem);
            }
        }

        String defaultKey = CartUtil.buildKey(productId, 0);
        CartItem defaultItem = cart.get(defaultKey);
        if (defaultItem != null) {
            return new CartLookup(defaultKey, defaultItem);
        }

        CartKey parsedKey = parseCartKey(requestedKey, productId);
        if (parsedKey != null) {
            String normalizedKey = CartUtil.buildKey(parsedKey.productId, parsedKey.variantId);
            CartItem normalizedItem = cart.get(normalizedKey);
            if (normalizedItem != null) {
                return new CartLookup(normalizedKey, normalizedItem);
            }
        }

        if (productId > 0) {
            for (Map.Entry<String, CartItem> entry : cart.entrySet()) {
                CartItem item = entry.getValue();
                if (item != null && item.getProductId() == productId) {
                    return new CartLookup(entry.getKey(), item);
                }
            }
        }

        return CartLookup.empty();
    }

    private CartKey parseCartKey(String key, int fallbackProductId) {
        if (key == null || key.isBlank()) {
            return fallbackProductId > 0 ? new CartKey(fallbackProductId, 0) : null;
        }

        String[] parts = key.trim().split(":");

        try {
            int productId = parts.length > 0 && !parts[0].isBlank()
                    ? Integer.parseInt(parts[0].trim())
                    : fallbackProductId;
            int variantId = parts.length > 1 && !parts[1].isBlank()
                    ? Integer.parseInt(parts[1].trim())
                    : 0;

            if (productId <= 0) {
                return null;
            }

            return new CartKey(productId, Math.max(variantId, 0));
        } catch (Exception e) {
            return fallbackProductId > 0 ? new CartKey(fallbackProductId, 0) : null;
        }
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

    private static class CartLookup {
        private final String key;
        private final CartItem item;

        private CartLookup(String key, CartItem item) {
            this.key = key;
            this.item = item;
        }

        private static CartLookup empty() {
            return new CartLookup(null, null);
        }
    }

    private static class CartKey {
        private final int productId;
        private final int variantId;

        private CartKey(int productId, int variantId) {
            this.productId = productId;
            this.variantId = variantId;
        }
    }
}
