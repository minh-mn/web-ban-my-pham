package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.model.User;
import com.webshop.app.service.FlashSaleLimitService;
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
    private final FlashSaleLimitService flashSaleLimitService = new FlashSaleLimitService();

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

        /*
         * Issue 139:
         * Gắn thông tin giới hạn Flash Sale vào CartItem trước khi render JSP.
         *
         * Mục tiêu:
         * - cart.jsp hiển thị: "Flash Sale: giới hạn X sản phẩm/khách".
         * - cart.jsp biết khi nào cần khóa nút tăng số lượng.
         * - Nếu user refresh trang giỏ hàng, thông tin giới hạn vẫn được cập nhật
         *   theo Flash Sale đang active và lịch sử mua của user.
         */
        int userId = getCurrentUserId(session);

        try {
            flashSaleLimitService.enrichCartItems(userId, cart);
        } catch (Exception e) {
            System.out.println("[CartViewServlet] enrich flash sale limit skipped: " + e.getMessage());
        }

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

        Object cartError = session.getAttribute("cartError");
        Object flashSaleLimitError = session.getAttribute("flashSaleLimitError");

        if (cartError != null) {
            req.setAttribute("cartError", cartError);
            session.removeAttribute("cartError");
        }

        if (flashSaleLimitError != null) {
            req.setAttribute("flashSaleLimitError", flashSaleLimitError);
            session.removeAttribute("flashSaleLimitError");
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
}
