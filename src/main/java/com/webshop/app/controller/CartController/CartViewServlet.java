package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart")
public class CartViewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // ===== TÍNH TỔNG TIỀN =====
        BigDecimal total = BigDecimal.ZERO;
        if (cart != null) {
            for (CartItem item : cart.values()) {
                if (item != null && item.getPrice() != null) {
                    BigDecimal line = item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    total = total.add(line);
                }
            }
        }

        // ===== DATA =====
        req.setAttribute("cart", cart);
        req.setAttribute("total", total);

        // ===== META LAYOUT =====
        req.setAttribute("pageTitle", "MyCosmetic | Giỏ hàng");
        req.setAttribute("pageCss", "/cart.css");
        req.setAttribute("pageContent", "/jsp/cart/cart.jsp");

        // ===== RENDER BASE =====
        req.getRequestDispatcher("/jsp/common/base.jsp")
           .forward(req, resp);
    }
}
