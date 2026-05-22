package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart/select-checkout")
public class CartSelectCheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        String[] selectedKeys = req.getParameterValues("selectedKeys");

        if (selectedKeys == null || selectedKeys.length == 0) {
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return;
        }

        List<String> validKeys = new ArrayList<>();

        for (String key : selectedKeys) {
            if (key != null && cart.containsKey(key.trim())) {
                validKeys.add(key.trim());
            }
        }

        if (validKeys.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return;
        }

        CartUtil.setSelectedCartKeys(session, validKeys);

        resp.sendRedirect(req.getContextPath() + "/checkout");
    }
}