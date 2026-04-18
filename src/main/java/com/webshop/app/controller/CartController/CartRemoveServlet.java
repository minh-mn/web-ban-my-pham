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

@WebServlet("/cart/remove")
public class CartRemoveServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        int productId = parseInt(req.getParameter("productId"), -1);
        if (productId == -1) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        cart.remove(productId);

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
