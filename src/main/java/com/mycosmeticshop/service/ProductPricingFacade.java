package com.mycosmeticshop.service;

import com.mycosmeticshop.dao.*;
import com.mycosmeticshop.model.*;

public class ProductPricingFacade {

    private final ProductDiscountDAO productDiscountDAO = new ProductDiscountDAO();
    private final BrandDiscountDAO brandDiscountDAO = new BrandDiscountDAO();
    private final PromotionEventService promotionEventService = new PromotionEventService();
    private final ProductPriceService priceService = new ProductPriceService();

    public java.math.BigDecimal getFinalPrice(Product product) {

        ProductDiscount pd =
            productDiscountDAO.findActiveByProductId(product.getId());

        BrandDiscount bd = null;
        if (product.getBrand() != null) {
            bd = brandDiscountDAO
                    .findBestActiveByBrandId(product.getBrand().getId());
        }

        PromotionEvent pe =
            promotionEventService.findBestForProduct(product);

        return priceService.calculateFinalPrice(
                product,
                pd,
                bd,
                pe
        );
    }
}
