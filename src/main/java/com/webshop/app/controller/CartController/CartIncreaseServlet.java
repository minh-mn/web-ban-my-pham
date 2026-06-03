package com.webshop.app.controller.CartController;

import java.io.IOException;
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

@WebServlet("/cart/increase")
public class CartIncreaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FlashSaleLimitService flashSaleLimitService = new FlashSaleLimitService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleIncrease(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleIncrease(req, resp);
    }

    private void handleIncrease(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        String requestedKey = CartUtil.normalizeKey(req.getParameter("key"), productId);

        HttpSession session = req.getSession();

        /*
         * Nạp giỏ hàng đã lưu trong DB nếu session chưa có dữ liệu mới nhất.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);
        CartLookup lookup = findCartItem(cart, requestedKey, productId);

        if (lookup.item != null) {
            CartItem item = lookup.item;
            String realKey = lookup.key;

            int newQuantity = item.getQuantity() + 1;

            if (item.getStock() > 0 && newQuantity > item.getStock()) {
                newQuantity = item.getStock();
            }

            int userId = getCurrentUserId(session);

            FlashSaleLimitService.LimitResult limitResult =
                    flashSaleLimitService.checkCanSetQuantity(
                            userId,
                            cart,
                            realKey,
                            item.getProductId(),
                            newQuantity
                    );

            if (!limitResult.isAllowed()) {
                session.setAttribute("cartError", limitResult.getMessage());
                session.setAttribute("flashSaleLimitError", limitResult.getMessage());

                try {
                    flashSaleLimitService.enrichCartItems(userId, cart);
                } catch (Exception e) {
                    System.out.println("[CartIncreaseServlet] enrich flash sale limit skipped: " + e.getMessage());
                }

                resp.sendRedirect(req.getContextPath()
                        + "/cart?flashLimit=1&message="
                        + urlEncode(limitResult.getMessage()));
                return;
            }

            item.setQuantity(Math.max(newQuantity, 1));
            item.setCartKey(realKey);
            cart.put(realKey, item);

            try {
                flashSaleLimitService.enrichCartItems(userId, cart);
            } catch (Exception e) {
                System.out.println("[CartIncreaseServlet] enrich flash sale limit skipped: " + e.getMessage());
            }

            session.setAttribute(CartUtil.CART_SESSION_KEY, cart);
            CartUtil.saveCartForLoggedUser(session);
        } else {
            session.setAttribute("cartError", "Không tìm thấy sản phẩm trong giỏ hàng để tăng số lượng.");
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
