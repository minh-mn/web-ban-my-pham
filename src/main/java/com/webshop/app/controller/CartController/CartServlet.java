package com.webshop.app.controller.CartController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.model.CartItem;
import com.webshop.app.model.Product;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.model.User;
import com.webshop.app.service.FlashSaleLimitService;
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
    private final FlashSaleLimitService flashSaleLimitService = new FlashSaleLimitService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int productId = parseInt(req.getParameter("productId"), -1);
        int variantId = parseInt(req.getParameter("variantId"), 0);
        int quantity = parseInt(req.getParameter("quantity"), 1);
        boolean quickAdd = "1".equals(req.getParameter("quickAdd"));

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
            if (variantId > 0) {
                selectedVariant = variantDAO.findActiveByIdAndProductId(variantId, productId);

                if (selectedVariant == null) {
                    resp.sendRedirect(productDetailUrl(req, product, "variantInvalid=1"));
                    return;
                }
            } else if (quickAdd) {
                selectedVariant = findFirstAvailableVariant(variants);

                if (selectedVariant == null) {
                    resp.sendRedirect(productDetailUrl(req, product, "variantOutOfStock=1"));
                    return;
                }
            } else {
                resp.sendRedirect(productDetailUrl(req, product, "variantRequired=1"));
                return;
            }

            if (selectedVariant.getStock() <= 0) {
                resp.sendRedirect(productDetailUrl(req, product, "variantOutOfStock=1"));
                return;
            }
        }

        int maxStock = selectedVariant != null ? selectedVariant.getStock() : product.getStock();

        if (maxStock <= 0) {
            resp.sendRedirect(productDetailUrl(req, product, "outOfStock=1"));
            return;
        }

        if (quantity > maxStock) {
            quantity = maxStock;
        }

        HttpSession session = req.getSession();
        Map<String, CartItem> cart = CartUtil.getCart(session);

        /*
         * Issue 139:
         * Chặn mua quá số lượng Flash Sale ngay khi thêm vào giỏ.
         *
         * Lưu ý:
         * - Kiểm tra theo productId, không theo cartKey, để user không thể vượt giới hạn
         *   bằng cách thêm cùng sản phẩm nhưng khác variant.
         * - Nếu user chưa đăng nhập, service vẫn giới hạn số lượng trong giỏ hiện tại.
         * - CheckoutService vẫn phải kiểm tra lại lần cuối để tránh bypass request.
         */
        int userId = getCurrentUserId(session);
        FlashSaleLimitService.LimitResult limitResult =
                flashSaleLimitService.checkCanAdd(userId, cart, productId, quantity);

        if (!limitResult.isAllowed()) {
            session.setAttribute("cartError", limitResult.getMessage());
            session.setAttribute("flashSaleLimitError", limitResult.getMessage());

            String query = "flashLimit=1&message=" + urlEncode(limitResult.getMessage());

            if (quickAdd) {
                resp.sendRedirect(req.getContextPath() + "/cart?" + query);
            } else {
                resp.sendRedirect(productDetailUrl(req, product, query));
            }

            return;
        }

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

        /*
         * Gắn thông tin giới hạn Flash Sale vào CartItem để cart.jsp có thể:
         * - Hiển thị "Flash Sale: giới hạn X sản phẩm/khách"
         * - Khóa nút tăng số lượng khi đã đạt giới hạn
         */
        try {
            flashSaleLimitService.enrichCartItems(userId, cart);
        } catch (Exception e) {
            System.out.println("[CartServlet] enrich flash sale limit skipped: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/cart?added=1");
    }

    private ProductVariant findFirstAvailableVariant(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        for (ProductVariant variant : variants) {
            if (variant != null && variant.getStock() > 0) {
                return variant;
            }
        }

        return null;
    }

    private String productDetailUrl(HttpServletRequest req, Product product, String query) {
        StringBuilder url = new StringBuilder(req.getContextPath());

        if (product.getSlug() != null && !product.getSlug().isBlank()) {
            url.append("/product/").append(product.getSlug()).append("?id=").append(product.getId());
            if (query != null && !query.isBlank()) {
                url.append("&").append(query);
            }
        } else {
            url.append("/product?id=").append(product.getId());
            if (query != null && !query.isBlank()) {
                url.append("&").append(query);
            }
        }

        return url.toString();
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

    private String urlEncode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int parseInt(String raw, int def) {
        try {
            if (raw == null || raw.isBlank()) {
                return def;
            }
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
