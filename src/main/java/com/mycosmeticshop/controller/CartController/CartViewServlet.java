package com.mycosmeticshop.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.utils.CartUtil;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
 * Servlet hiển thị trang giỏ hàng
 * URL truy cập: /cart
 *
 * Chức năng:
 * - Lấy giỏ hàng từ session
 * - Tính tổng tiền của toàn bộ sản phẩm trong giỏ
 * - Gửi dữ liệu sang JSP để hiển thị
 * - Render thông qua layout base.jsp
 */
@WebServlet("/cart")
public class CartViewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Phương thức GET
     * Hiển thị trang giỏ hàng
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // =====================================================
        // 1) LẤY GIỎ HÀNG TỪ SESSION
        // =====================================================
        HttpSession session = req.getSession();

        // Lấy cart từ session thông qua CartUtil
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // =====================================================
        // 2) TÍNH TỔNG TIỀN GIỎ HÀNG
        // =====================================================
        BigDecimal total = BigDecimal.ZERO;

        if (cart != null) {
            for (CartItem item : cart.values()) {

                /*
                 * Kiểm tra item và price để tránh NullPointerException
                 * Thành tiền mỗi dòng:
                 *   price * quantity
                 */
                if (item != null && item.getPrice() != null) {
                    BigDecimal line = item.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    total = total.add(line);
                }
            }
        }

        // =====================================================
        // 3) GỬI DỮ LIỆU SANG JSP
        // =====================================================
        req.setAttribute("cart", cart);
        req.setAttribute("total", total);

        // =====================================================
        // 4) THIẾT LẬP THÔNG TIN LAYOUT
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Giỏ hàng");
        req.setAttribute("pageCss", "/cart.css");
        req.setAttribute("pageContent", "/jsp/cart/cart.jsp");

        // =====================================================
        // 5) RENDER QUA BASE LAYOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp")
                .forward(req, resp);
    }
}