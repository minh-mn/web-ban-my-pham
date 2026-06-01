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

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        String key = CartUtil.normalizeKey(req.getParameter("key"), productId);

        HttpSession session = req.getSession();

        /*
         * Issue 132:
         * Nếu user đã đăng nhập nhưng session chưa có cart mới nhất,
         * nạp lại dữ liệu đã lưu trong database trước khi giảm số lượng.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);
        CartItem item = cart.get(key);

        if (item != null) {
            int newQuantity = item.getQuantity() - 1;

            if (newQuantity <= 0) {
                cart.remove(key);
            } else {
                item.setQuantity(newQuantity);
                cart.put(key, item);
            }

            if (cart.isEmpty()) {
                session.removeAttribute(CartUtil.CART_SESSION_KEY);
            } else {
                session.setAttribute(CartUtil.CART_SESSION_KEY, cart);
            }

            /*
             * Lưu lại database ngay sau khi giảm/xóa sản phẩm.
             * Nếu user logout/login lại thì giỏ hàng vẫn giữ đúng số lượng mới.
             */
            CartUtil.saveCartForLoggedUser(session);
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
