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
         * nạp lại dữ liệu đã lưu trong database trước khi xóa sản phẩm.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);
        cart.remove(key);

        if (cart.isEmpty()) {
            session.removeAttribute(CartUtil.CART_SESSION_KEY);
        } else {
            session.setAttribute(CartUtil.CART_SESSION_KEY, cart);
        }

        /*
         * Lưu lại database ngay sau khi xóa sản phẩm khỏi giỏ hàng.
         * Nếu user logout/login lại thì sản phẩm đã xóa sẽ không xuất hiện lại.
         */
        CartUtil.saveCartForLoggedUser(session);

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
