package com.mycosmeticshop.controller.CartController;

import java.io.IOException;
import java.util.Map;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.utils.CartUtil;

/*
 * Servlet xử lý giảm số lượng sản phẩm trong giỏ hàng
 * URL truy cập: /cart/decrease
 *
 * Chức năng:
 * - Giảm số lượng sản phẩm trong giỏ hàng
 * - Nếu số lượng sau khi giảm = 0 thì xóa sản phẩm khỏi giỏ
 * - Sau khi cập nhật sẽ redirect về trang /cart
 */
@WebServlet("/cart/decrease")
public class CartDecreaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Phương thức GET
     * Xử lý giảm số lượng sản phẩm trong giỏ hàng
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // ===== LẤY productId TỪ REQUEST =====
        int productId = parseInt(req.getParameter("productId"), -1);

        // Nếu productId không hợp lệ -> quay lại trang cart
        if (productId == -1) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // ===== LẤY SESSION =====
        HttpSession session = req.getSession();

        // Lấy giỏ hàng từ session
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // ===== TÌM SẢN PHẨM TRONG CART =====
        CartItem item = cart.get(productId);

        if (item != null) {

            // Giảm số lượng sản phẩm
            int newQty = item.getQuantity() - 1;

            // Nếu số lượng <= 0 -> xóa khỏi cart
            if (newQty <= 0) {
                cart.remove(productId);
            } else {
                // Cập nhật số lượng mới
                item.setQuantity(newQty);
            }
        }

        // ===== REDIRECT VỀ TRANG GIỎ HÀNG =====
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /*
     * Hàm chuyển String -> int an toàn
     * Nếu parse lỗi sẽ trả về giá trị mặc định
     */
    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}