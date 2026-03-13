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
 * Servlet xử lý xóa sản phẩm khỏi giỏ hàng
 * URL truy cập: /cart/remove
 *
 * Chức năng:
 * - Xóa hoàn toàn một sản phẩm khỏi giỏ hàng
 * - Sau khi xóa sẽ redirect về trang /cart
 */
@WebServlet("/cart/remove")
public class CartRemoveServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Phương thức GET
     * Xử lý yêu cầu xóa sản phẩm khỏi giỏ hàng
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

        // ===== XÓA SẢN PHẨM KHỎI CART =====
        cart.remove(productId);

        // ===== REDIRECT VỀ TRANG GIỎ HÀNG =====
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /*
     * Hàm parse String -> int an toàn
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