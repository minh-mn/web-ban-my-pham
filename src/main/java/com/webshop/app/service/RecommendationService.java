package com.webshop.app.service;

import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {

    private final OrderItemDAO orderItemDAO;
    private final ProductDAO productDAO;

    public RecommendationService(OrderItemDAO orderItemDAO, ProductDAO productDAO) {
        this.orderItemDAO = orderItemDAO;
        this.productDAO = productDAO;
    }

    /**
     * Lấy sản phẩm MUA KÈM: Định dạng tham số cấu hình lại theo kiểu int
     */
    public List<Product> getFrequentlyBought(int productId, int categoryId) {
        List<Integer> ids = orderItemDAO.findFrequentlyBoughtTogether(productId);

        if (ids == null || ids.isEmpty()) {
            return productDAO.findRelatedByCategory(categoryId, productId, 4);
        }

        return productDAO.findByIdsOrdered(ids);
    }

    /**
     * Lấy sản phẩm LIÊN QUAN
     */
    public List<Product> getRelatedProducts(int productId, int categoryId) {
        List<Product> byCategory = productDAO.findRelatedByCategory(categoryId, productId, 6);
        List<Product> byTag = productDAO.findRelatedByTag(productId, 6);

        // Sử dụng Map với Key là Integer thay vì Long
        Map<Integer, Product> uniqueMap = new LinkedHashMap<>();

        if (byCategory != null) {
            byCategory.forEach(p -> uniqueMap.put(p.getId(), p));
        }
        if (byTag != null) {
            byTag.forEach(p -> uniqueMap.put(p.getId(), p));
        }

        return new ArrayList<>(uniqueMap.values());
    }
}