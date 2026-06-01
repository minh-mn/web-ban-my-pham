package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
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

@WebServlet("/cart/add")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        int variantId = parseInt(req.getParameter("variantId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 1);

        if (quantity <= 0) {
            quantity = 1;
        }

        if (productId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        Product product = productDAO.findById(productId);

        if (product == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        ProductVariant selectedVariant = null;
        List<ProductVariant> variants = variantDAO.findActiveByProductId(productId);
        boolean productHasVariants = variants != null && !variants.isEmpty();

        if (productHasVariants) {
            if (variantId <= 0) {
                resp.sendRedirect(req.getContextPath()
                        + "/product/" + product.getSlug() + "?variantRequired=1");
                return;
            }

            selectedVariant = variantDAO.findActiveByIdAndProductId(variantId, productId);

            if (selectedVariant == null) {
                resp.sendRedirect(req.getContextPath()
                        + "/product/" + product.getSlug() + "?variantInvalid=1");
                return;
            }

            if (selectedVariant.getStock() <= 0) {
                resp.sendRedirect(req.getContextPath()
                        + "/product/" + product.getSlug() + "?variantOutOfStock=1");
                return;
            }
        }

        int maxStock = selectedVariant != null
                ? selectedVariant.getStock()
                : product.getStock();

        if (maxStock <= 0) {
            resp.sendRedirect(req.getContextPath()
                    + "/product/" + product.getSlug() + "?outOfStock=1");
            return;
        }

        if (quantity > maxStock) {
            quantity = maxStock;
        }

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        int realVariantId = selectedVariant != null ? selectedVariant.getId() : 0;
        String cartKey = CartUtil.buildKey(productId, realVariantId);

        BigDecimal originalProductPrice = safeMoney(product.getPrice());

        BigDecimal finalProductPrice = product.getFinalPrice() != null
                ? safeMoney(product.getFinalPrice())
                : originalProductPrice;

        BigDecimal variantExtraPrice = selectedVariant != null
                ? safeMoney(selectedVariant.getExtraPrice())
                : BigDecimal.ZERO;

        BigDecimal originalUnitPrice = originalProductPrice.add(variantExtraPrice);
        BigDecimal finalUnitPrice = finalProductPrice.add(variantExtraPrice);

        CartItem item = cart.get(cartKey);

        if (item == null) {
            item = new CartItem();

            item.setCartKey(cartKey);
            item.setProductId(product.getId());
            item.setTitle(product.getTitle());
            item.setPrice(finalUnitPrice);
            item.setOriginalPrice(originalUnitPrice);
            item.setImageUrl(product.getImageUrl());
            item.setStock(maxStock);
            item.setQuantity(quantity);

            if (selectedVariant != null) {
                item.setVariantId(selectedVariant.getId());
                item.setVariantSize(selectedVariant.getSize());
                item.setVariantType(selectedVariant.getType());
                item.setVariantName(selectedVariant.getDisplayName());
                item.setVariantExtraPrice(variantExtraPrice);
            }

            cart.put(cartKey, item);
        } else {
            int newQuantity = item.getQuantity() + quantity;

            if (newQuantity > maxStock) {
                newQuantity = maxStock;
            }

            item.setPrice(finalUnitPrice);
            item.setOriginalPrice(originalUnitPrice);
            item.setImageUrl(product.getImageUrl());
            item.setStock(maxStock);
            item.setQuantity(newQuantity);

            if (selectedVariant != null) {
                item.setVariantId(selectedVariant.getId());
                item.setVariantSize(selectedVariant.getSize());
                item.setVariantType(selectedVariant.getType());
                item.setVariantName(selectedVariant.getDisplayName());
                item.setVariantExtraPrice(variantExtraPrice);
            } else {
                item.setVariantId(0);
                item.setVariantSize(null);
                item.setVariantType(null);
                item.setVariantName(null);
                item.setVariantExtraPrice(BigDecimal.ZERO);
            }
        }

        session.setAttribute(CartUtil.CART_SESSION_KEY, cart);

        /*
         * Issue 132:
         * Nếu user đã đăng nhập, lưu ngay giỏ hàng xuống database sau khi thêm sản phẩm.
         * Khi logout/login lại, CartUtil có thể khôi phục giỏ hàng từ bảng cart_items.
         */
        CartUtil.saveCartForLoggedUser(session);

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int parseInt(String raw, int def) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return def;
        }
    }
}
