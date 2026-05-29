package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.CategoryTagDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.CategoryTag;
import com.webshop.app.model.Product;
import com.webshop.app.service.ProductPricingFacade;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoryTagDAO categoryTagDAO = new CategoryTagDAO();

    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ===== 1. ĐỌC THAM SỐ DẠNG LIST ĐỂ HỖ TRỢ MULTI-FILTER =====
        String keyword = req.getParameter("q");
        String sort = req.getParameter("sort");

        List<String> priceRangeList = parseStringList(req.getParameterValues("priceRange"));
        List<Integer> selectedCategoryList = parseIntegerList(req.getParameterValues("category"));
        List<Integer> selectedBrandList = parseIntegerList(req.getParameterValues("brand"));

        Integer minRating = parseInt(req.getParameter("rating"));

        // ===== 2. PHÂN TRANG =====
        int pageSize = 18;
        int page = parseIntOrDefault(req.getParameter("page"), 1);

        if (page < 1) {
            page = 1;
        }

        int total = productDAO.countProducts(
                keyword,
                selectedCategoryList,
                selectedBrandList,
                priceRangeList,
                minRating
        );

        int totalPages = (int) Math.ceil(total / (double) pageSize);

        if (totalPages < 1) {
            totalPages = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        // ===== 3. TẢI DANH SÁCH SẢN PHẨM =====
        List<Product> products = productDAO.findProductsPaged(
                keyword,
                selectedCategoryList,
                selectedBrandList,
                sort,
                priceRangeList,
                minRating,
                page,
                pageSize
        );

        // Tính giá khuyến mãi cuối cùng
        products.forEach(product ->
                product.setFinalPrice(pricingFacade.getFinalPrice(product))
        );

        /*
         * ===== 4. LOAD THẺ DANH MỤC CHO DANH SÁCH SẢN PHẨM =====
         * Không sửa ProductDAO để tránh ảnh hưởng luồng /products đang chạy ổn.
         * JSP có thể lấy tag theo categoryId:
         * ${categoryTagsByCategoryId[product.category.id]}
         */
        Map<Integer, List<CategoryTag>> categoryTagsByCategoryId =
                loadCategoryTagsByProductCategories(products);

        // ===== 5. DỮ LIỆU THANH SIDEBAR =====
        req.setAttribute("categories", categoryDAO.findParents());
        req.setAttribute("brands", brandDAO.findWithProductCount());

        // Giữ lại trạng thái các bộ lọc đã tick chọn trên giao diện
        req.setAttribute("priceRangeList", priceRangeList);
        req.setAttribute("selectedCategoryList", selectedCategoryList);
        req.setAttribute("selectedBrandList", selectedBrandList);
        req.setAttribute("minRating", minRating);

        // ===== 6. DỮ LIỆU PHÂN TRANG & HIỂN THỊ =====
        req.setAttribute("products", products);
        req.setAttribute("categoryTagsByCategoryId", categoryTagsByCategoryId);

        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        req.setAttribute("pageTitle", "MyCosmetic | Sản phẩm");
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== 7. RENDER =====
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /* =====================================================
       CATEGORY TAGS
    ===================================================== */

    private Map<Integer, List<CategoryTag>> loadCategoryTagsByProductCategories(List<Product> products) {
        List<Integer> categoryIds = resolveCategoryIds(products);

        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<CategoryTag>> tagMap =
                categoryTagDAO.findActiveByCategoryIds(categoryIds);

        return tagMap == null ? Collections.emptyMap() : tagMap;
    }

    private List<Integer> resolveCategoryIds(List<Product> products) {
        Set<Integer> categoryIds = new LinkedHashSet<>();

        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        for (Product product : products) {
            if (product == null || product.getCategory() == null) {
                continue;
            }

            int categoryId = product.getCategory().getId();

            if (categoryId > 0) {
                categoryIds.add(categoryId);
            }
        }

        return categoryIds.stream().collect(Collectors.toList());
    }

    /* =====================================================
       PARAM HELPERS
    ===================================================== */

    private Integer parseInt(String value) {
        try {
            return (value != null && !value.isBlank())
                    ? Integer.parseInt(value.trim())
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value != null && !value.isBlank())
                    ? Integer.parseInt(value.trim())
                    : defaultValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    /*
     * Ép kiểu an toàn:
     * - Bỏ qua null/rỗng.
     * - Bỏ qua "all".
     * - Bỏ qua chuỗi lỗi không phải số để tránh HTTP 500.
     */
    private List<Integer> parseIntegerList(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .filter(value -> !value.equalsIgnoreCase("all"))
                .map(value -> {
                    try {
                        return Integer.parseInt(value);

                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(value -> value != null && value > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> parseStringList(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }
}
