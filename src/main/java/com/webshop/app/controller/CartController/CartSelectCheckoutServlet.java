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

        /*
         * Issue 132:
         * Trước khi chọn sản phẩm để thanh toán, nếu user đã đăng nhập
         * thì nạp lại giỏ hàng đã lưu trong database vào session nếu cần.
         *
         * Trường hợp user logout/login lại, session cart có thể rỗng,
         * nhưng cart_items trong database vẫn còn dữ liệu.
         */
        CartUtil.loadDatabaseCartIfNeeded(session);

        Map<String, CartItem> cart = CartUtil.getCart(session);

        if (cart == null || cart.isEmpty()) {
            CartUtil.clearSelectedCartKeys(session);
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        String[] selectedKeys = req.getParameterValues("selectedKeys");

        if (selectedKeys == null || selectedKeys.length == 0) {
            CartUtil.clearSelectedCartKeys(session);
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return;
        }

        List<String> validKeys = new ArrayList<>();

        for (String key : selectedKeys) {
            if (key == null || key.isBlank()) {
                continue;
            }

            String normalizedKey = key.trim();
            CartItem item = cart.get(normalizedKey);

            if (item != null && item.getQuantity() > 0) {
                validKeys.add(normalizedKey);
            }
        }

        if (validKeys.isEmpty()) {
            CartUtil.clearSelectedCartKeys(session);
            resp.sendRedirect(req.getContextPath() + "/cart?selectRequired=1");
            return;
        }

        CartUtil.setSelectedCartKeys(session, validKeys);

        resp.sendRedirect(req.getContextPath() + "/checkout");
    }
}
