package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.ProductVariant;
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

    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, List<ProductVariant>> variantOptions = new HashMap<>();

        if (cart != null) {
            for (CartItem item : cart.values()) {
                if (item != null && item.getPrice() != null) {
                    total = total.add(item.getSubtotal());

                    variantOptions.computeIfAbsent(
                            item.getProductId(),
                            productVariantDAO::findActiveByProductId
                    );
                }
            }
        }

        req.setAttribute("cart", cart);
        req.setAttribute("total", total);
        req.setAttribute("variantOptions", variantOptions);

        req.setAttribute("pageTitle", "MyCosmetic | Giỏ hàng");
        req.setAttribute("pageCss", "/cart.css");
        req.setAttribute("pageContent", "/jsp/cart/cart.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp")
                .forward(req, resp);
    }
}