package com.webshop.app.utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.webshop.app.dao.CartItemDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.User;

import jakarta.servlet.http.HttpSession;

public class CartUtil {

    public static final String CART_SESSION_KEY = "CART";
    public static final String SELECTED_CART_KEYS_SESSION_KEY = "SELECTED_CART_KEYS";
    private static final String CART_DB_LOADED_USER_ID_SESSION_KEY = "CART_DB_LOADED_USER_ID";

    @SuppressWarnings("unchecked")
    public static Map<String, CartItem> getCart(HttpSession session) {
        Map<String, CartItem> cart =
                (Map<String, CartItem>) session.getAttribute(CART_SESSION_KEY);

        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }

        return cart;
    }

    public static String buildKey(int productId, int variantId) {
        return productId + ":" + Math.max(variantId, 0);
    }

    public static String normalizeKey(String key, int productId) {
        if (key != null && !key.isBlank()) {
            return key.trim();
        }

        return buildKey(productId, 0);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getSelectedCartKeys(HttpSession session) {
        Object raw = session.getAttribute(SELECTED_CART_KEYS_SESSION_KEY);

        if (raw instanceof Set<?>) {
            return (Set<String>) raw;
        }

        return new LinkedHashSet<>();
    }

    public static void setSelectedCartKeys(HttpSession session, Collection<String> keys) {
        LinkedHashSet<String> selectedKeys = new LinkedHashSet<>();

        if (keys != null) {
            for (String key : keys) {
                if (key != null && !key.isBlank()) {
                    selectedKeys.add(key.trim());
                }
            }
        }

        session.setAttribute(SELECTED_CART_KEYS_SESSION_KEY, selectedKeys);
    }

    public static Map<String, CartItem> filterCartByKeys(
            Map<String, CartItem> cart,
            Collection<String> selectedKeys
    ) {
        Map<String, CartItem> selectedCart = new LinkedHashMap<>();

        if (cart == null || cart.isEmpty() || selectedKeys == null || selectedKeys.isEmpty()) {
            return selectedCart;
        }

        for (String key : selectedKeys) {
            CartItem item = cart.get(key);

            if (item != null) {
                selectedCart.put(key, item);
            }
        }

        return selectedCart;
    }

    public static Map<String, CartItem> getSelectedCart(HttpSession session) {
        Map<String, CartItem> cart = getCart(session);
        Set<String> selectedKeys = getSelectedCartKeys(session);

        return filterCartByKeys(cart, selectedKeys);
    }

    public static void removeItems(HttpSession session, Collection<String> keys) {
        if (session == null || keys == null || keys.isEmpty()) {
            return;
        }

        Map<String, CartItem> cart = getCart(session);

        for (String key : keys) {
            cart.remove(key);
        }

        if (cart.isEmpty()) {
            session.removeAttribute(CART_SESSION_KEY);
        } else {
            session.setAttribute(CART_SESSION_KEY, cart);
        }

        saveCartForLoggedUser(session);
    }

    public static void clearSelectedCartKeys(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SELECTED_CART_KEYS_SESSION_KEY);
        }
    }

    /**
     * Gộp giỏ hàng đã lưu trong database vào session sau khi user đăng nhập.
     * Nếu session đang có sản phẩm trùng key thì cộng số lượng session + database.
     */
    public static void mergeDatabaseCartIntoSession(HttpSession session, int userId) {
        if (session == null || userId <= 0) {
            return;
        }

        CartItemDAO cartItemDAO = new CartItemDAO();
        Map<String, CartItem> databaseCart = cartItemDAO.findByUserId(userId);
        Map<String, CartItem> sessionCart = getCart(session);

        if (databaseCart != null && !databaseCart.isEmpty()) {
            for (Map.Entry<String, CartItem> entry : databaseCart.entrySet()) {
                String key = entry.getKey();
                CartItem dbItem = entry.getValue();

                if (key == null || dbItem == null) {
                    continue;
                }

                CartItem sessionItem = sessionCart.get(key);

                if (sessionItem == null) {
                    sessionCart.put(key, dbItem);
                    continue;
                }

                int mergedQuantity = sessionItem.getQuantity() + dbItem.getQuantity();
                int maxStock = sessionItem.getStock() > 0
                        ? sessionItem.getStock()
                        : dbItem.getStock();

                if (maxStock > 0 && mergedQuantity > maxStock) {
                    mergedQuantity = maxStock;
                }

                sessionItem.setQuantity(Math.max(mergedQuantity, 1));
                sessionItem.setStock(maxStock);
            }
        }

        session.setAttribute(CART_SESSION_KEY, sessionCart);
        session.setAttribute(CART_DB_LOADED_USER_ID_SESSION_KEY, userId);

        saveCartForLoggedUser(session);
    }

    /**
     * Chỉ load database cart một lần cho mỗi user trong cùng session.
     * Dùng ở trang giỏ hàng để user đăng nhập lại vẫn thấy giỏ hàng cũ.
     */
    public static void loadDatabaseCartIfNeeded(HttpSession session) {
        User user = getLoggedUser(session);

        if (user == null || user.getId() <= 0) {
            return;
        }

        Object loadedUserId = session.getAttribute(CART_DB_LOADED_USER_ID_SESSION_KEY);

        if (loadedUserId instanceof Integer && ((Integer) loadedUserId) == user.getId()) {
            return;
        }

        mergeDatabaseCartIntoSession(session, user.getId());
    }

    /**
     * Lưu giỏ hàng session hiện tại xuống database cho user đang đăng nhập.
     * Gọi sau khi thêm, tăng, giảm, xóa, đổi biến thể và trước khi logout.
     */
    public static void saveCartForLoggedUser(HttpSession session) {
        User user = getLoggedUser(session);

        if (user == null || user.getId() <= 0) {
            return;
        }

        Map<String, CartItem> cart = getCart(session);
        new CartItemDAO().replaceByUserId(user.getId(), cart);
    }

    private static User getLoggedUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object rawUser = session.getAttribute("user");

        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        return null;
    }
}
