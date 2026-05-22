package com.webshop.app.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.webshop.app.model.CartItem;

import jakarta.servlet.http.HttpSession;

public class CartUtil {

    @SuppressWarnings("unchecked")
    public static Map<String, CartItem> getCart(HttpSession session) {
        Map<String, CartItem> cart =
                (Map<String, CartItem>) session.getAttribute("CART");

        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("CART", cart);
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
}