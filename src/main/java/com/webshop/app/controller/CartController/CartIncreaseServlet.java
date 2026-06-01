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
         * Đảm bảo nếu user đã đăng nhập nhưng session cart chưa được nạp,
         * hệ thống sẽ lấy lại giỏ hàng đã lưu trong database trước khi tăng số lượng.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);
        CartItem item = cart.get(key);

        if (item != null) {
            int newQuantity = item.getQuantity() + 1;

            if (item.getStock() > 0 && newQuantity > item.getStock()) {
                newQuantity = item.getStock();
            }

            item.setQuantity(Math.max(newQuantity, 1));
            cart.put(key, item);

            session.setAttribute(CartUtil.CART_SESSION_KEY, cart);

            /*
             * Lưu lại database ngay sau khi tăng số lượng.
             * Nếu user logout/login lại thì quantity mới vẫn còn.
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
