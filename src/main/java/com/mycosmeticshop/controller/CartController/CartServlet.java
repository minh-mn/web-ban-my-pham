package com.mycosmeticshop.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.model.CartItem;
import com.mycosmeticshop.model.Product;
import com.mycosmeticshop.utils.CartUtil;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
 * Servlet xử lý thêm sản phẩm vào giỏ hàng
 * URL truy cập: /cart/add
 *
 * Chức năng:
 * - Nhận productId từ request
 * - Tìm sản phẩm trong database
 * - Nếu sản phẩm chưa có trong giỏ thì thêm mới
 * - Nếu đã có thì tăng số lượng
 * - Không cho vượt quá số lượng tồn kho
 * - Sau khi xử lý sẽ chuyển hướng sang trang giỏ hàng
 */
@WebServlet("/cart/add")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để lấy thông tin sản phẩm từ database
    private final ProductDAO productDAO = new ProductDAO();

    /*
     * Phương thức POST
     * Xử lý thêm sản phẩm vào giỏ hàng
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // =====================================================
        // 1) ĐỌC productId AN TOÀN TỪ REQUEST
        // =====================================================
        String raw = req.getParameter("productId");
        int productId;

        try {
            productId = Integer.parseInt(raw);
        } catch (Exception e) {
            // Nếu productId không hợp lệ -> quay về trang sản phẩm
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 2) TÌM SẢN PHẨM TRONG DATABASE
        // =====================================================
        Product p = productDAO.findById(productId);

        // Nếu không tìm thấy sản phẩm -> quay lại trang sản phẩm
        if (p == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 3) LẤY GIỎ HÀNG TỪ SESSION
        // =====================================================
        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // =====================================================
        // 4) THÊM MỚI HOẶC TĂNG SỐ LƯỢNG
        // =====================================================
        CartItem item = cart.get(productId);

        if (item == null) {
            // ---------------------------------------------
            // Trường hợp sản phẩm chưa có trong giỏ hàng
            // -> tạo CartItem mới
            // ---------------------------------------------
            item = new CartItem();

            // Gán thông tin cơ bản
            item.setProductId(p.getId());
            item.setTitle(p.getTitle());

            /*
             * Giá sản phẩm:
             * - Ưu tiên dùng giá sau giảm (finalPrice) nếu có
             * - Nếu không có thì dùng giá gốc (price)
             */
            BigDecimal price = p.getFinalPrice() != null ? p.getFinalPrice() : p.getPrice();
            item.setPrice(price);

            // Các field bổ sung để JSP hiển thị
            item.setImageUrl(p.getImageUrl()); // JSP đang dùng item.imageUrl
            item.setStock(p.getStock());       // JSP đang dùng item.stock

            // Khi thêm lần đầu, số lượng mặc định là 1
            item.setQuantity(1);

            // Đưa sản phẩm vào giỏ hàng
            cart.put(productId, item);

        } else {
            // ---------------------------------------------
            // Trường hợp sản phẩm đã có trong giỏ hàng
            // -> tăng số lượng
            // ---------------------------------------------
            int newQty = item.getQuantity() + 1;

            /*
             * Nếu có giới hạn tồn kho thì không cho vượt quá stock
             * Ví dụ:
             * - stock = 5
             * - quantity hiện tại = 5
             * - tăng thêm -> vẫn giữ 5
             */
            if (item.getStock() > 0 && newQty > item.getStock()) {
                newQty = item.getStock();
            }

            // Cập nhật số lượng mới
            item.setQuantity(newQty);
        }

        // =====================================================
        // 5) REDIRECT SANG TRANG XEM GIỎ HÀNG
        // =====================================================
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}