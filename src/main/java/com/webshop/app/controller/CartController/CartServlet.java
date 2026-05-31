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
                if (quantity > selectedVariant.getStock()) {

                    resp.sendRedirect(
                            req.getContextPath()
                                    + "/product/"
                                    + product.getSlug()
                                    + "?variantOutOfStock=1"
                    );

                    return;
                }
            }
            
        }

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        int realVariantId = selectedVariant != null ? selectedVariant.getId() : 0;
        String cartKey = CartUtil.buildKey(productId, realVariantId);

        int maxStock = selectedVariant != null
                ? selectedVariant.getStock()
                : product.getStock();

        if (maxStock > 0 && quantity > maxStock) {
            quantity = maxStock;
        }

        /*
         * originalProductPrice = giá gốc của sản phẩm.
         * finalProductPrice    = giá sau giảm / giá đang bán.
         */
        BigDecimal originalProductPrice = safeMoney(product.getPrice());

        BigDecimal finalProductPrice = product.getFinalPrice() != null
                ? safeMoney(product.getFinalPrice())
                : originalProductPrice;

        /*
         * Nếu sản phẩm có biến thể thì cộng thêm extraPrice vào cả:
         * - giá gốc
         * - giá sau giảm
         */
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

            /*
             * Quan trọng:
             * price         = giá đang bán / giá sau giảm
             * originalPrice = giá gốc trước giảm
             */
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

            if (maxStock > 0 && newQuantity > maxStock) {
                newQuantity = maxStock;
            }

            /*
             * Cập nhật lại giá để tránh trường hợp giá sản phẩm/giá biến thể đã thay đổi.
             */
            item.setPrice(finalUnitPrice);
            item.setOriginalPrice(originalUnitPrice);
            item.setStock(maxStock);
            item.setQuantity(newQuantity);

            if (selectedVariant != null) {
                item.setVariantId(selectedVariant.getId());
                item.setVariantSize(selectedVariant.getSize());
                item.setVariantType(selectedVariant.getType());
                item.setVariantName(selectedVariant.getDisplayName());
                item.setVariantExtraPrice(variantExtraPrice);
            }
        }

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
