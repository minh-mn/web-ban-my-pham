package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Product;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.utils.CartUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/cart/update-variant")
public class CartUpdateVariantServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        int variantId = parseInt(req.getParameter("variantId"), 0);
        String oldKey = CartUtil.normalizeKey(req.getParameter("key"), productId);

        if (productId <= 0 || variantId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);
        CartItem oldItem = cart.get(oldKey);

        if (oldItem == null) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        Product product = productDAO.findById(productId);
        ProductVariant variant = variantDAO.findActiveByIdAndProductId(variantId, productId);

        if (product == null || variant == null || variant.getStock() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        String newKey = CartUtil.buildKey(productId, variant.getId());

        BigDecimal basePrice = product.getFinalPrice() != null
                ? product.getFinalPrice()
                : product.getPrice();

        BigDecimal newPrice = basePrice.add(variant.getExtraPrice());
        int newQty = Math.min(oldItem.getQuantity(), variant.getStock());

        cart.remove(oldKey);

        CartItem existing = cart.get(newKey);

        if (existing != null) {
            int mergedQty = existing.getQuantity() + newQty;

            if (mergedQty > variant.getStock()) {
                mergedQty = variant.getStock();
            }

            existing.setQuantity(mergedQty);
            existing.setStock(variant.getStock());
        } else {
            oldItem.setCartKey(newKey);
            oldItem.setPrice(newPrice);
            oldItem.setStock(variant.getStock());
            oldItem.setQuantity(newQty);

            oldItem.setVariantId(variant.getId());
            oldItem.setVariantSize(variant.getSize());
            oldItem.setVariantType(variant.getType());
            oldItem.setVariantName(variant.getDisplayName());
            oldItem.setVariantExtraPrice(variant.getExtraPrice());

            cart.put(newKey, oldItem);
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}