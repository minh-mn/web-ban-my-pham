package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.util.ArrayList;
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
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
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

        products.forEach(product ->
                product.setFinalPrice(pricingFacade.getFinalPrice(product))
        );

        /*
         * ===== 4. LOAD THẺ DANH MỤC CHO DANH SÁCH SẢN PHẨM =====
         * Không sửa ProductDAO để tránh ảnh hưởng luồng /products đang chạy ổn.
         */
        Map<Integer, List<CategoryTag>> categoryTagsByCategoryId =
                loadCategoryTagsByProductCategories(products);

        // ===== 5. DỮ LIỆU THANH SIDEBAR =====
        List<Category> categories = categoryDAO.findParents();
        List<Brand> brands = brandDAO.findWithProductCount();

        req.setAttribute("categories", categories);
        req.setAttribute("brands", brands);

        // ===== 6. GIỮ LẠI TRẠNG THÁI FILTER =====
        req.setAttribute("priceRangeList", priceRangeList);
        req.setAttribute("selectedCategoryList", selectedCategoryList);
        req.setAttribute("selectedBrandList", selectedBrandList);
        req.setAttribute("minRating", minRating);

        /*
         * ===== 7. LABEL HIỂN THỊ CHO PHẦN "ĐANG LỌC" =====
         * Giúp JSP hiển thị đúng tên thay vì ID:
         * category=4 -> Serum/Essence (Tinh Chất Dưỡng)
         * brand=1 -> Mary & May
         * priceRange=500_1000 -> 500.000 - 1.000.000đ
         */
        req.setAttribute(
                "selectedCategoryNames",
                resolveSelectedCategoryNames(categories, selectedCategoryList)
        );

        req.setAttribute(
                "selectedBrandNames",
                resolveSelectedBrandNames(brands, selectedBrandList)
        );

        req.setAttribute(
                "selectedPriceRangeLabels",
                resolveSelectedPriceRangeLabels(priceRangeList)
        );

        req.setAttribute(
                "selectedRatingLabel",
                resolveRatingLabel(minRating)
        );

        req.setAttribute(
                "selectedSortLabel",
                resolveSortLabel(sort)
        );

        // ===== 8. DỮ LIỆU PHÂN TRANG & HIỂN THỊ =====
        req.setAttribute("products", products);
        req.setAttribute("categoryTagsByCategoryId", categoryTagsByCategoryId);

        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        req.setAttribute("pageTitle", "MyCosmetic | Sản phẩm");
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== 9. RENDER =====
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

        return new ArrayList<>(categoryIds);
    }

    /* =====================================================
       ACTIVE FILTER LABELS
    ===================================================== */

    private List<String> resolveSelectedCategoryNames(
            List<Category> categories,
            List<Integer> selectedCategoryIds
    ) {
        List<String> names = new ArrayList<>();

        if (categories == null || categories.isEmpty()
                || selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return names;
        }

        for (Integer selectedId : selectedCategoryIds) {
            String name = findCategoryNameById(categories, selectedId);

            if (name != null && !name.isBlank()) {
                names.add(name);
            } else {
                names.add("Danh mục #" + selectedId);
            }
        }

        return names;
    }

    private String findCategoryNameById(List<Category> categories, Integer id) {
        if (id == null || id <= 0 || categories == null || categories.isEmpty()) {
            return null;
        }

        for (Category category : categories) {
            if (category == null) {
                continue;
            }

            if (category.getId() == id) {
                return category.getName();
            }

            String childName = findCategoryNameById(category.getChildren(), id);

            if (childName != null) {
                return childName;
            }
        }

        return null;
    }

    private List<String> resolveSelectedBrandNames(
            List<Brand> brands,
            List<Integer> selectedBrandIds
    ) {
        List<String> names = new ArrayList<>();

        if (brands == null || brands.isEmpty()
                || selectedBrandIds == null || selectedBrandIds.isEmpty()) {
            return names;
        }

        for (Integer selectedId : selectedBrandIds) {
            String name = findBrandNameById(brands, selectedId);

            if (name != null && !name.isBlank()) {
                names.add(name);
            } else {
                names.add("Thương hiệu #" + selectedId);
            }
        }

        return names;
    }

    private String findBrandNameById(List<Brand> brands, Integer id) {
        if (id == null || id <= 0 || brands == null || brands.isEmpty()) {
            return null;
        }

        for (Brand brand : brands) {
            if (brand != null && brand.getId() == id) {
                return brand.getName();
            }
        }

        return null;
    }

    private List<String> resolveSelectedPriceRangeLabels(List<String> priceRangeList) {
        List<String> labels = new ArrayList<>();

        if (priceRangeList == null || priceRangeList.isEmpty()) {
            return labels;
        }

        for (String priceRange : priceRangeList) {
            if (priceRange == null || priceRange.isBlank()) {
                continue;
            }

            switch (priceRange.trim()) {
                case "lt500":
                case "0_500":
                case "under_500":
                    labels.add("0 - 500.000đ");
                    break;

                case "500_1000":
                    labels.add("500.000 - 1.000.000đ");
                    break;

                case "gt1000":
                case "over_1000":
                    labels.add("Trên 1.000.000đ");
                    break;

                case "under-200":
                    labels.add("Dưới 200.000đ");
                    break;

                case "200-500":
                    labels.add("200.000 - 500.000đ");
                    break;

                case "500-1000":
                    labels.add("500.000 - 1.000.000đ");
                    break;

                case "over-1000":
                    labels.add("Trên 1.000.000đ");
                    break;

                default:
                    labels.add(priceRange.trim());
                    break;
            }
        }

        return labels;
    }

    private String resolveRatingLabel(Integer minRating) {
        if (minRating == null || minRating <= 0) {
            return "";
        }

        return "Từ " + minRating + " sao";
    }

    private String resolveSortLabel(String sort) {
        if (sort == null || sort.isBlank()) {
            return "";
        }

        switch (sort.trim()) {
            case "price_asc":
                return "Giá tăng dần";

            case "price_desc":
                return "Giá giảm dần";

            case "rating_desc":
                return "Đánh giá cao";

            case "newest":
                return "Mới nhất";

            default:
                return sort.trim();
        }
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