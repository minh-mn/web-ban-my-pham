package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        // ===== 1. ĐỌC THAM SỐ FILTER =====
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

        // ===== 4. TÍNH GIÁ SAU KHUYẾN MÃI =====
        products.forEach(product ->
                product.setFinalPrice(pricingFacade.getFinalPrice(product))
        );

        // ===== 5. LOAD THẺ DANH MỤC CHO PRODUCT CARD =====
        Map<Integer, List<CategoryTag>> categoryTagsByCategoryId =
                loadCategoryTagsByProductCategories(products);

        // ===== 6. DỮ LIỆU SIDEBAR =====
        List<Category> categories = categoryDAO.findParents();
        List<Brand> brands = brandDAO.findWithProductCount();

        req.setAttribute("categories", categories);
        req.setAttribute("brands", brands);

        // ===== 7. GIỮ LẠI TRẠNG THÁI FILTER =====
        req.setAttribute("priceRangeList", priceRangeList);
        req.setAttribute("selectedCategoryList", selectedCategoryList);
        req.setAttribute("selectedBrandList", selectedBrandList);
        req.setAttribute("minRating", minRating);

        /*
         * ===== 8. LABEL HIỂN THỊ CHO PHẦN "ĐANG LỌC" =====
         * Các attribute này giữ lại để list.jsp cũ hoặc block hiển thị label vẫn dùng được.
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

        /*
         * ===== 9. ACTIVE FILTER TAGS CÓ NÚT XÓA RIÊNG =====
         * Mỗi tag có:
         * - label: nội dung hiển thị.
         * - removeUrl: URL đã bỏ riêng filter đó.
         */
        req.setAttribute(
                "activeFilterTags",
                buildActiveFilterTags(
                        req,
                        categories,
                        brands,
                        keyword,
                        selectedCategoryList,
                        selectedBrandList,
                        priceRangeList,
                        minRating,
                        sort
                )
        );

        // ===== 10. DỮ LIỆU PHÂN TRANG & HIỂN THỊ =====
        req.setAttribute("products", products);
        req.setAttribute("categoryTagsByCategoryId", categoryTagsByCategoryId);

        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        req.setAttribute("pageTitle", "MyCosmetic | Sản phẩm");
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== 11. RENDER =====
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /* =====================================================
       ACTIVE FILTER TAGS
    ===================================================== */

    private List<ActiveFilterTag> buildActiveFilterTags(
            HttpServletRequest req,
            List<Category> categories,
            List<Brand> brands,
            String keyword,
            List<Integer> selectedCategoryIds,
            List<Integer> selectedBrandIds,
            List<String> priceRangeList,
            Integer minRating,
            String sort
    ) {
        List<ActiveFilterTag> tags = new ArrayList<>();

        if (!isBlank(keyword)) {
            tags.add(new ActiveFilterTag(
                    "Từ khóa: " + keyword.trim(),
                    buildRemoveUrl(req, "q", null)
            ));
        }

        if (selectedCategoryIds != null) {
            for (Integer categoryId : selectedCategoryIds) {
                if (categoryId == null || categoryId <= 0) {
                    continue;
                }

                String name = findCategoryNameById(categories, categoryId);
                String label = !isBlank(name) ? name : "#" + categoryId;

                tags.add(new ActiveFilterTag(
                        "Danh mục: " + label,
                        buildRemoveUrl(req, "category", String.valueOf(categoryId))
                ));
            }
        }

        if (selectedBrandIds != null) {
            for (Integer brandId : selectedBrandIds) {
                if (brandId == null || brandId <= 0) {
                    continue;
                }

                String name = findBrandNameById(brands, brandId);
                String label = !isBlank(name) ? name : "#" + brandId;

                tags.add(new ActiveFilterTag(
                        "Thương hiệu: " + label,
                        buildRemoveUrl(req, "brand", String.valueOf(brandId))
                ));
            }
        }

        if (priceRangeList != null) {
            for (String priceRange : priceRangeList) {
                if (isBlank(priceRange)) {
                    continue;
                }

                String value = priceRange.trim();

                tags.add(new ActiveFilterTag(
                        "Giá: " + resolveSinglePriceRangeLabel(value),
                        buildRemoveUrl(req, "priceRange", value)
                ));
            }
        }

        if (minRating != null && minRating > 0) {
            tags.add(new ActiveFilterTag(
                    "Đánh giá: Từ " + minRating + " sao",
                    buildRemoveUrl(req, "rating", null)
            ));
        }

        if (!isBlank(sort)) {
            tags.add(new ActiveFilterTag(
                    "Sắp xếp: " + resolveSortLabel(sort),
                    buildRemoveUrl(req, "sort", null)
            ));
        }

        return tags;
    }

    private String buildRemoveUrl(
            HttpServletRequest req,
            String removeParamName,
            String removeParamValue
    ) {
        StringBuilder url = new StringBuilder();
        url.append(req.getContextPath()).append("/products");

        List<String> queryParts = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String paramName = entry.getKey();
            String[] values = entry.getValue();

            if ("page".equals(paramName)) {
                continue;
            }

            if (values == null || values.length == 0) {
                continue;
            }

            for (String value : values) {
                if (value == null) {
                    continue;
                }

                if (shouldSkipParam(paramName, value, removeParamName, removeParamValue)) {
                    continue;
                }

                queryParts.add(urlEncode(paramName) + "=" + urlEncode(value));
            }
        }

        if (!queryParts.isEmpty()) {
            url.append("?").append(String.join("&", queryParts));
        }

        return url.toString();
    }

    private boolean shouldSkipParam(
            String paramName,
            String value,
            String removeParamName,
            String removeParamValue
    ) {
        if (!paramName.equals(removeParamName)) {
            return false;
        }

        /*
         * removeParamValue == null:
         * Dùng cho filter chỉ có một giá trị như q, rating, sort.
         * Khi xóa thì xóa toàn bộ param đó.
         */
        if (removeParamValue == null) {
            return true;
        }

        /*
         * removeParamValue != null:
         * Dùng cho filter có thể chọn nhiều như category, brand, priceRange.
         * Chỉ xóa đúng giá trị được bấm x.
         */
        return removeParamValue.equals(value);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public static class ActiveFilterTag {

        private final String label;
        private final String removeUrl;

        public ActiveFilterTag(String label, String removeUrl) {
            this.label = label;
            this.removeUrl = removeUrl;
        }

        public String getLabel() {
            return label;
        }

        public String getRemoveUrl() {
            return removeUrl;
        }
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

            if (!isBlank(name)) {
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

            if (!isBlank(name)) {
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
            if (!isBlank(priceRange)) {
                labels.add(resolveSinglePriceRangeLabel(priceRange));
            }
        }

        return labels;
    }

    private String resolveSinglePriceRangeLabel(String priceRange) {
        if (priceRange == null) {
            return "";
        }

        switch (priceRange.trim()) {
            case "lt500":
            case "0_500":
            case "under_500":
                return "0 - 500.000đ";

            case "500_1000":
                return "500.000 - 1.000.000đ";

            case "gt1000":
            case "over_1000":
                return "Trên 1.000.000đ";

            case "under-200":
                return "Dưới 200.000đ";

            case "200-500":
                return "200.000 - 500.000đ";

            case "500-1000":
                return "500.000 - 1.000.000đ";

            case "over-1000":
                return "Trên 1.000.000đ";

            default:
                return priceRange.trim();
        }
    }

    private String resolveRatingLabel(Integer minRating) {
        if (minRating == null || minRating <= 0) {
            return "";
        }

        return "Từ " + minRating + " sao";
    }

    private String resolveSortLabel(String sort) {
        if (isBlank(sort)) {
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
            return !isBlank(value) ? Integer.parseInt(value.trim()) : null;

        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return !isBlank(value) ? Integer.parseInt(value.trim()) : defaultValue;

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
                .filter(value -> !isBlank(value))
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
                .filter(value -> !isBlank(value))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}