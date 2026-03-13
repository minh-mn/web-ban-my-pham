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
 * Servlet xử lý tăng số lượng sản phẩm trong giỏ hàng
 * URL truy cập: /cart/increase
 *
 * Chức năng:
 * - Tăng số lượng sản phẩm trong giỏ hàng
 * - Kiểm tra không cho vượt quá tồn kho
 * - Sau khi cập nhật sẽ redirect về trang /cart
 */
@WebServlet("/cart/increase")
public class CartIncreaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Phương thức GET
     * Xử lý tăng số lượng sản phẩm
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // ===== LẤY productId TỪ REQUEST =====
        int productId = parseInt(req.getParameter("productId"), -1);

        // Nếu productId không hợp lệ -> quay về trang cart
        if (productId == -1) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // ===== LẤY SESSION =====
        HttpSession session = req.getSession();

        // Lấy cart từ session
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // ===== TÌM SẢN PHẨM TRONG CART =====
        CartItem item = cart.get(productId);

        if (item != null) {

            // Tăng số lượng sản phẩm
            int newQty = item.getQuantity() + 1;

            /*
             * Kiểm tra tồn kho
             * Nếu số lượng mới vượt quá stock thì giới hạn bằng stock
             */
            if (item.getStock() > 0 && newQty > item.getStock()) {
                newQty = item.getStock();
            }

            // Cập nhật số lượng mới
            item.setQuantity(newQty);
        }

        // ===== REDIRECT VỀ TRANG GIỎ HÀNG =====
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /*
     * Hàm chuyển String -> int an toàn
     * Nếu lỗi parse thì trả về giá trị mặc định
     */
    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}