package com.webshop.app.utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.webshop.app.model.CartItem;

import jakarta.servlet.http.HttpSession;

public class CartUtil {

    public static final String CART_SESSION_KEY = "CART";
    public static final String SELECTED_CART_KEYS_SESSION_KEY = "SELECTED_CART_KEYS";

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
    }

    public static void clearSelectedCartKeys(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SELECTED_CART_KEYS_SESSION_KEY);
        }
    }
}