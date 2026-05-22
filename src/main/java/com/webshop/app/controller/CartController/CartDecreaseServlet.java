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

        int productId = parseInt(req.getParameter("productId"), -1);
        String key = CartUtil.normalizeKey(req.getParameter("key"), productId);

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        CartItem item = cart.get(key);

        if (item != null) {
            int newQuantity = item.getQuantity() - 1;

            if (newQuantity <= 0) {
                cart.remove(key);
            } else {
                item.setQuantity(newQuantity);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int parseInt(String raw, int def) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }
}