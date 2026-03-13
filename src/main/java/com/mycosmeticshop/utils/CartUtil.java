package com.mycosmeticshop.utils;

import java.util.HashMap;
import java.util.Map;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.http.HttpSession;

import com.mycosmeticshop.model.CartItem;

/*
 * Utility class quản lý giỏ hàng trong session
 *
 * Chức năng:
 * - Lấy giỏ hàng từ HttpSession
 * - Nếu chưa tồn tại thì tạo mới
 * - Luôn đảm bảo session có Map<Integer, CartItem>
 *
 * Key session:
 * "CART"
 *
 * Cấu trúc:
 * productId -> CartItem
 */
public class CartUtil {

    /*
     * Lấy giỏ hàng từ session
     *
     * Nếu chưa có giỏ hàng thì:
     * - tạo HashMap mới
     * - lưu vào session
     *
     * @param session HttpSession hiện tại
     * @return Map<Integer, CartItem>
     */
    @SuppressWarnings("unchecked")
    public static Map<Integer, CartItem> getCart(HttpSession session) {

        // Lấy cart từ session
        Map<Integer, CartItem> cart =
                (Map<Integer, CartItem>) session.getAttribute("CART");

        // Nếu chưa tồn tại thì tạo mới
        if (cart == null) {

            cart = new HashMap<>();

            // Lưu cart vào session
            session.setAttribute("CART", cart);
        }

        return cart;
    }
}