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

@WebServlet("/cart/increase")
public class CartIncreaseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        int productId = parseInt(req.getParameter("productId"), -1);
        if (productId == -1) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        CartItem item = cart.get(productId);
        if (item != null) {
            int newQty = item.getQuantity() + 1;

            // chặn vượt tồn kho
            if (item.getStock() > 0 && newQty > item.getStock()) {
                newQty = item.getStock();
            }
            item.setQuantity(newQty);
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
