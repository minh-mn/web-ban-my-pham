package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.webshop.app.model.CartItem;
import com.webshop.app.model.User;
import com.webshop.app.service.FlashSaleLimitService;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart/increase")
public class CartIncreaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FlashSaleLimitService flashSaleLimitService = new FlashSaleLimitService();

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

            /*
             * Issue 139:
             * Chặn tăng số lượng vượt giới hạn Flash Sale.
             *
             * Cần kiểm tra theo productId, không chỉ theo cartKey, vì cùng một sản phẩm
             * có thể có nhiều variant khác nhau trong giỏ. Nếu chỉ chặn theo cartKey,
             * user có thể lách bằng cách thêm nhiều variant.
             */
            int userId = getCurrentUserId(session);

            FlashSaleLimitService.LimitResult limitResult =
                    flashSaleLimitService.checkCanSetQuantity(
                            userId,
                            cart,
                            key,
                            item.getProductId(),
                            newQuantity
                    );

            if (!limitResult.isAllowed()) {
                session.setAttribute("cartError", limitResult.getMessage());
                session.setAttribute("flashSaleLimitError", limitResult.getMessage());

                try {
                    flashSaleLimitService.enrichCartItems(userId, cart);
                } catch (Exception e) {
                    System.out.println("[CartIncreaseServlet] enrich flash sale limit skipped: " + e.getMessage());
                }

                resp.sendRedirect(req.getContextPath()
                        + "/cart?flashLimit=1&message="
                        + urlEncode(limitResult.getMessage()));
                return;
            }

            item.setQuantity(Math.max(newQuantity, 1));
            cart.put(key, item);

            try {
                flashSaleLimitService.enrichCartItems(userId, cart);
            } catch (Exception e) {
                System.out.println("[CartIncreaseServlet] enrich flash sale limit skipped: " + e.getMessage());
            }

            session.setAttribute(CartUtil.CART_SESSION_KEY, cart);

            /*
             * Lưu lại database ngay sau khi tăng số lượng.
             * Nếu user logout/login lại thì quantity mới vẫn còn.
             */
            CartUtil.saveCartForLoggedUser(session);
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int getCurrentUserId(HttpSession session) {
        if (session == null) {
            return 0;
        }

        Object rawUser = session.getAttribute("user");

        if (!(rawUser instanceof User)) {
            rawUser = session.getAttribute("currentUser");
        }

        if (!(rawUser instanceof User)) {
            rawUser = session.getAttribute("authUser");
        }

        if (rawUser instanceof User) {
            return ((User) rawUser).getId();
        }

        return 0;
    }

    private String urlEncode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private int parseInt(String raw, int def) {
        try {
            if (raw == null || raw.isBlank()) {
                return def;
            }

            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
