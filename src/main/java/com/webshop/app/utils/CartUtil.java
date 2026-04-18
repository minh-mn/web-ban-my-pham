package com.webshop.app.utils;

import java.util.HashMap;
import java.util.Map;

import com.webshop.app.model.CartItem;

import jakarta.servlet.http.HttpSession;


public class CartUtil {

    @SuppressWarnings("unchecked")
    public static Map<Integer, CartItem> getCart(HttpSession session) {
        Map<Integer, CartItem> cart =
            (Map<Integer, CartItem>) session.getAttribute("CART");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("CART", cart);
        }
        return cart;
    }
}

