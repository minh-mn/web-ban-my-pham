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
     * Lấy sản phẩm mua kèm.
     * Nếu chưa có dữ liệu mua kèm thì fallback về sản phẩm cùng danh mục.
     */
    public List<Product> getFrequentlyBought(int productId, int categoryId) {
        List<Integer> ids = new ArrayList<>();

        try {
            ids = orderItemDAO.findFrequentlyBoughtTogether(productId);
        } catch (RuntimeException e) {
            ids = new ArrayList<>();
        }

        if (ids == null || ids.isEmpty()) {
            return productDAO.findRelatedByCategory(categoryId, productId, 4);
        }

        return productDAO.findByIdsOrdered(ids);
    }

    /**
     * Lấy sản phẩm liên quan.
     * Không để trang chi tiết sản phẩm lỗi 500 nếu bảng tag chưa được tạo trong database.
     */
    public List<Product> getRelatedProducts(int productId, int categoryId) {
        List<Product> byCategory = new ArrayList<>();
        List<Product> byTag = new ArrayList<>();

        try {
            byCategory = productDAO.findRelatedByCategory(categoryId, productId, 6);
        } catch (RuntimeException e) {
            byCategory = new ArrayList<>();
        }

        try {
            byTag = productDAO.findRelatedByTag(productId, 6);
        } catch (RuntimeException e) {
            byTag = new ArrayList<>();
        }

        Map<Integer, Product> uniqueMap = new LinkedHashMap<>();

        if (byCategory != null) {
            byCategory.forEach(product -> uniqueMap.put(product.getId(), product));
        }

        if (byTag != null) {
            byTag.forEach(product -> uniqueMap.put(product.getId(), product));
        }

        return new ArrayList<>(uniqueMap.values());
    }
}
