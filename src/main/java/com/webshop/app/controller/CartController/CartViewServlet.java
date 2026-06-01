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

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();

        /*
         * Issue 132:
         * Khi user đã đăng nhập và mở trang giỏ hàng, tự nạp lại giỏ hàng
         * đã lưu trong database vào session.
         *
         * CartUtil sẽ chỉ load một lần cho mỗi user trong cùng session,
         * tránh việc refresh trang giỏ hàng làm cộng dồn số lượng nhiều lần.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);

        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, List<ProductVariant>> variantOptions = new HashMap<>();

        if (cart != null && !cart.isEmpty()) {
            for (CartItem item : cart.values()) {
                if (item == null) {
                    continue;
                }

                if (item.getPrice() != null) {
                    total = total.add(item.getSubtotal());
                }

                if (item.getProductId() > 0) {
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
