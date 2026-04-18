package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Product;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart/add")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // 1) Read productId safely
        String raw = req.getParameter("productId");
        int productId;

        try {
            productId = Integer.parseInt(raw);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // 2) Load product
        Product p = productDAO.findById(productId);
        if (p == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // 3) Get cart from session
        HttpSession session = req.getSession();
        Map<Integer, CartItem> cart = CartUtil.getCart(session);

        // 4) Add/increase
        CartItem item = cart.get(productId);

        if (item == null) {
            item = new CartItem();
            item.setProductId(p.getId());
            item.setTitle(p.getTitle());

            // Giá: nếu bạn muốn dùng finalPrice thì set p.getFinalPrice()
            BigDecimal price = p.getFinalPrice() != null ? p.getFinalPrice() : p.getPrice();
            item.setPrice(price);

            // Các field để JSP dùng
            item.setImageUrl(p.getImageUrl()); // JSP đang đọc item.imageUrl
            item.setStock(p.getStock());       // JSP đang đọc item.stock

            item.setQuantity(1);
            cart.put(productId, item);

        } else {
            // Nếu có giới hạn tồn kho thì chặn tăng quá stock
            int newQty = item.getQuantity() + 1;
            if (item.getStock() > 0 && newQty > item.getStock()) {
                newQty = item.getStock();
            }
            item.setQuantity(newQty);
        }

        // 5) Redirect to cart view
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
