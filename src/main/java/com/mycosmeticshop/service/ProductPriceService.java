package com.mycosmeticshop.service;

import com.mycosmeticshop.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class ProductPriceService {

    public BigDecimal calculateFinalPrice(
            Product product,
            ProductDiscount productDiscount,
            BrandDiscount brandDiscount,
            PromotionEvent event
    ) {
        BigDecimal price = product.getPrice();
        LocalDate today = LocalDate.now();

        // 1️⃣ PRODUCT DISCOUNT
        if (productDiscount != null && productDiscount.isValid(today)) {
            price = applyDiscount(
                    price,
                    productDiscount.getDiscountType(),
                    productDiscount.getDiscountValue(),
                    productDiscount.getMaxDiscountAmount()
            );
        }

        // 2️⃣ BRAND DISCOUNT
        if (brandDiscount != null && brandDiscount.isValid(today)) {
            price = applyDiscount(
                    price,
                    brandDiscount.getDiscountType(),
                    brandDiscount.getDiscountValue(),
                    brandDiscount.getMaxDiscountAmount()
            );
        }

        // 3️⃣ PROMOTION EVENT
        if (event != null && event.isValid(today)) {
            price = applyDiscount(
                    price,
                    event.getDiscountType(),
                    event.getDiscountValue(),
                    event.getMaxDiscountAmount()
            );
        }

        // 4️⃣ FALLBACK (product.discountPercent)
        if (product.getDiscountPercent() > 0) {
            BigDecimal fallback = price
                    .multiply(BigDecimal.valueOf(product.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            price = price.subtract(fallback);
        }

        return price.max(BigDecimal.ZERO)
                    .setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= APPLY DISCOUNT ================= */

    private BigDecimal applyDiscount(
            BigDecimal price,
            DiscountType type,
            BigDecimal value,
            BigDecimal maxAmount
    ) {
        BigDecimal discount = BigDecimal.ZERO;

        if (type == DiscountType.PERCENT) {
            discount = price.multiply(value)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (type == DiscountType.FIXED) {
            discount = value;
        }

        if (maxAmount != null && discount.compareTo(maxAmount) > 0) {
            discount = maxAmount;
        }

        return price.subtract(discount);
    }
}
