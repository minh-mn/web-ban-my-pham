package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.webshop.app.dao.*;
import com.webshop.app.model.*;
import com.webshop.app.service.ProductPricingFacade;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final CategoryTagDAO categoryTagDAO = new CategoryTagDAO();
    private final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();

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

        /*
         * Hỗ trợ cả 3 kiểu URL:
         * - /products?category=14          : dùng cho sidebar filter và Danh mục hot mới.
         * - /products?categoryIds=14       : tương thích link cũ đang có trên trang chủ.
         * - /products?categoryId=14        : tương thích nếu file khác dùng dạng số ít.
         *
         * Khi bấm vào danh mục cha như "Son Môi", hệ thống sẽ tự mở rộng sang các danh mục con
         * như Son Lót, Son Thỏi, Son Kem, Son Bóng/Son Tint,... để danh sách sản phẩm hiện đúng.
         */
        List<Integer> selectedCategoryList = parseCategoryRequest(req);

        /*
         * Load category tree sớm để:
         * - Mở rộng danh mục cha sang danh mục con trước khi count/query sản phẩm.
         * - Giữ đúng tên danh mục trong phần "Đang lọc".
         */
        List<Category> categories = categoryDAO.findParents();
        List<Integer> productCategoryFilterList = expandCategoryIdsForProductFilter(categories, selectedCategoryList);

        /*
         * Collection Son Môi / Trang điểm môi:
         * DB hiện tại có thể chưa gắn đúng cây category con cho son môi,
         * nên nếu chỉ lọc cứng theo category_id sẽ ra 0 sản phẩm.
         * Trường hợp này chuyển sang tìm theo bộ từ khóa sản phẩm môi để trang hiển thị chuyên nghiệp hơn.
         */
        String hotCollectionKey = resolveHotCollectionKey(categories, selectedCategoryList);
        boolean useLipCollectionSearch = "lip-collection".equals(hotCollectionKey);

        /*
         * Fix trắng trang:
         * Không gọi DAO custom nữa. Với collection Son Môi/Trang điểm môi,
         * dùng lại hàm DAO sẵn có bằng keyword "son" và bỏ category_id rỗng.
         * Cách này an toàn hơn vì không phụ thuộc cấu trúc bảng mới.
         */
        /*
         * Sau khi database đã có cây danh mục Son Môi đúng:
         * - Không dùng keyword "son" nữa, vì sẽ lọc nhầm sản phẩm không thuộc collection.
         * - Chỉ lọc theo category cha/con của Son Môi để trang này chỉ chứa sản phẩm môi.
         */
        String effectiveKeyword = keyword;
        List<Integer> effectiveCategoryFilterList = productCategoryFilterList;

        List<Integer> selectedBrandList = parseIntegerList(req.getParameterValues("brand"));

        Integer minRating = parseInt(req.getParameter("rating"));

        WishlistDAO wishlistDAO = new WishlistDAO();

        User user = getCurrentUser(req);

        Set<Integer> wishlistIds = new HashSet<>();

        if (user != null) {
            wishlistIds = new HashSet<>(wishlistDAO.findProductIdsByUser(user.getId()));
        }

        req.setAttribute("wishlistIds", wishlistIds);

        // ===== 2. PHÂN TRANG =====
        // Grid danh sách sản phẩm đang hiển thị 4 cột trên desktop.
        // Dùng 24 sản phẩm/trang để mỗi trang đủ 6 hàng với layout 4 cột, tránh chưa đủ hàng đã sang trang mới.
        int pageSize = 24;
        int page = parseIntOrDefault(req.getParameter("page"), 1);

        if (page < 1) {
            page = 1;
        }

        int total = productDAO.countProducts(
                effectiveKeyword,
                effectiveCategoryFilterList,
                selectedBrandList,
                priceRangeList,
                minRating
        );

        /*
         * Issue 133:
         * Lưu lịch sử tìm kiếm khi user tìm bằng q trên trang /products.
         * Chỉ lưu khi user đã đăng nhập và keyword thật sự có nội dung.
         * Các thao tác lọc không có keyword sẽ không lưu để tránh rác lịch sử.
         */
        saveSearchHistoryIfLoggedIn(req, keyword, total);

        int totalPages = (int) Math.ceil(total / (double) pageSize);

        if (totalPages < 1) {
            totalPages = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        // ===== 3. TẢI DANH SÁCH SẢN PHẨM =====
        List<Product> products = productDAO.findProductsPaged(
                effectiveKeyword,
                effectiveCategoryFilterList,
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
        List<Brand> brands = brandDAO.findWithProductCount();

        Category primaryCategory = useLipCollectionSearch
                ? resolveLipPrimaryCategory(categories)
                : resolvePrimaryCategoryForSidebar(categories, selectedCategoryList);

        req.setAttribute("categories", categories);
        req.setAttribute("brands", brands);
        req.setAttribute("primaryCategory", primaryCategory);

        // ===== 7. GIỮ LẠI TRẠNG THÁI FILTER =====
        req.setAttribute("priceRangeList", priceRangeList);
        req.setAttribute("selectedCategoryList", selectedCategoryList);
        req.setAttribute("selectedCategoryFilterList", productCategoryFilterList);
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

        req.setAttribute("collectionTitle", resolveCollectionTitle(categories, selectedCategoryList));
        req.setAttribute("collectionDesc", resolveCollectionDescription(categories, selectedCategoryList, total));
        req.setAttribute("pageTitle", "MyCosmetic | " + resolveCollectionTitle(categories, selectedCategoryList));
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== 11. RENDER =====
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }


    /* =====================================================
       SEARCH HISTORY
    ===================================================== */

    private void saveSearchHistoryIfLoggedIn(HttpServletRequest req, String keyword, int total) {
        if (isBlank(keyword)) {
            return;
        }

        User currentUser = getCurrentUser(req);

        if (currentUser == null || currentUser.getId() <= 0) {
            return;
        }

        try {
            searchHistoryDAO.saveSearch(
                    currentUser.getId(),
                    keyword.trim(),
                    total,
                    buildSearchUrl(req)
            );
        } catch (Exception e) {
            /*
             * Không để lỗi lưu lịch sử làm hỏng trang danh sách sản phẩm.
             * Nếu bảng user_search_history chưa được tạo, /products vẫn phải chạy bình thường.
             */
            System.out.println("[ProductListServlet] save search history error: " + e.getMessage());
        }
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object rawUser = session.getAttribute("user");

        if (rawUser instanceof User) {
            return (User) rawUser;
        }

        return null;
    }

    private String buildSearchUrl(HttpServletRequest req) {
        String queryString = req.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return "/products";
        }

        return "/products?" + queryString;
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
            LinkedHashSet<Integer> selectedCategorySet = new LinkedHashSet<>(selectedCategoryIds);

            for (Integer categoryId : selectedCategoryIds) {
                if (categoryId == null || categoryId <= 0) {
                    continue;
                }

                Category tagCategory = findCategoryById(categories, categoryId);

                if (tagCategory != null && hasSelectedDescendant(tagCategory, selectedCategorySet)) {
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
        boolean sameParam = paramName.equals(removeParamName);

        /*
         * Tương thích link cũ:
         * category, categoryId, categoryIds đều được xem là cùng một bộ lọc danh mục.
         * Nhờ vậy khi bấm dấu x ở "Đang lọc", link từ Danh mục hot cũng được xóa đúng.
         */
        if (!sameParam && isCategoryParam(removeParamName) && isCategoryParam(paramName)) {
            sameParam = true;
        }

        if (!sameParam) {
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

            case "best-selling":
                return "Bán chạy nhất";

            case "updated_desc":
                return "Mới cập nhật";

            case "newest":
                return "Mới nhất";

            default:
                return sort.trim();
        }
    }


    /* =====================================================
       CATEGORY URL / PARENT CATEGORY FILTER HELPERS
    ===================================================== */

    private List<Integer> parseCategoryRequest(HttpServletRequest req) {
        if (req == null) {
            return Collections.emptyList();
        }

        LinkedHashSet<Integer> categoryIds = new LinkedHashSet<>();

        categoryIds.addAll(parseIntegerList(req.getParameterValues("category")));
        categoryIds.addAll(parseIntegerList(req.getParameterValues("categoryId")));
        categoryIds.addAll(parseIntegerList(req.getParameterValues("categoryIds")));

        return new ArrayList<>(categoryIds);
    }

    private List<Integer> expandCategoryIdsForProductFilter(
            List<Category> categories,
            List<Integer> selectedCategoryIds
    ) {
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Integer> selectedSet = new LinkedHashSet<>(selectedCategoryIds);
        LinkedHashSet<Integer> result = new LinkedHashSet<>();

        for (Integer selectedId : selectedCategoryIds) {
            if (selectedId == null || selectedId <= 0) {
                continue;
            }

            Category selectedCategory = findCategoryById(categories, selectedId);

            if (selectedCategory == null) {
                result.add(selectedId);
                continue;
            }

            boolean selectedCategoryHasChildren = selectedCategory.getChildren() != null
                    && !selectedCategory.getChildren().isEmpty();

            /*
             * Lọc đúng theo checkbox:
             * - Nếu chỉ chọn danh mục cha Son Môi: mở rộng ra toàn bộ danh mục con.
             * - Nếu vừa có danh mục cha ẩn + người dùng tick danh mục con: KHÔNG mở rộng cha,
             *   chỉ lấy đúng danh mục con được tick.
             * Nhờ vậy /products?category=22 hiển thị toàn bộ son môi,
             * còn /products?category=22&category=30 chỉ hiển thị Son Kem.
             */
            if (selectedCategoryHasChildren) {
                boolean hasSelectedChild = hasSelectedDescendant(selectedCategory, selectedSet);

                if (hasSelectedChild) {
                    continue;
                }

                collectCategoryAndChildren(selectedCategory, result);
                result.addAll(resolveSemanticHotCategoryIds(categories, selectedCategory));
                continue;
            }

            result.add(selectedId);
        }

        return new ArrayList<>(result);
    }

    private boolean hasSelectedDescendant(Category parent, Set<Integer> selectedIds) {
        if (parent == null || selectedIds == null || selectedIds.isEmpty()
                || parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return false;
        }

        for (Category child : parent.getChildren()) {
            if (child == null) {
                continue;
            }

            if (selectedIds.contains(child.getId())) {
                return true;
            }

            if (hasSelectedDescendant(child, selectedIds)) {
                return true;
            }
        }

        return false;
    }

    private void collectCategoryAndChildren(Category category, Set<Integer> output) {
        if (category == null || output == null) {
            return;
        }

        if (category.getId() > 0) {
            output.add(category.getId());
        }

        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }

        for (Category child : category.getChildren()) {
            collectCategoryAndChildren(child, output);
        }
    }

    private List<Integer> resolveSemanticHotCategoryIds(
            List<Category> categories,
            Category selectedCategory
    ) {
        if (selectedCategory == null) {
            return Collections.emptyList();
        }

        String selectedKey = normalizeCategoryKey(
                selectedCategory.getName() + " " + selectedCategory.getSlug()
        );

        if (selectedKey.isEmpty()) {
            return Collections.emptyList();
        }

        List<Category> flatCategories = flattenCategories(categories);

        if (flatCategories.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Integer> ids = new LinkedHashSet<>();

        for (Category category : flatCategories) {
            if (category == null || category.getId() <= 0) {
                continue;
            }

            String categoryKey = normalizeCategoryKey(
                    category.getName() + " " + category.getSlug()
            );

            if (matchesHotCategoryGroup(selectedKey, categoryKey)) {
                ids.add(category.getId());
            }
        }

        return new ArrayList<>(ids);
    }

    private boolean matchesHotCategoryGroup(String selectedKey, String categoryKey) {
        if (selectedKey == null || categoryKey == null) {
            return false;
        }

        // ===== SON MÔI / TRANG ĐIỂM MÔI =====
        if (selectedKey.contains("son-moi")
                || selectedKey.contains("trang-diem-moi")) {

            return categoryKey.equals("son-moi")
                    || categoryKey.startsWith("son-")
                    || categoryKey.contains("son-lot")
                    || categoryKey.contains("son-thoi")
                    || categoryKey.contains("son-kem")
                    || categoryKey.contains("son-bong")
                    || categoryKey.contains("son-tint")
                    || categoryKey.contains("son-duong")
                    || categoryKey.contains("tri-tham-moi")
                    || categoryKey.contains("mat-na-moi")
                    || categoryKey.contains("tay-da-chet-moi");
        }

        // ===== MÁ HỒNG =====
        if (selectedKey.contains("ma-hong") || selectedKey.contains("blush")) {
            return categoryKey.contains("ma-hong")
                    || categoryKey.contains("blush");
        }

        // ===== PHẤN MẮT =====
        if (selectedKey.contains("phan-mat") || selectedKey.contains("eyeshadow")) {
            return categoryKey.contains("phan-mat")
                    || categoryKey.contains("eyeshadow")
                    || categoryKey.contains("eye-shadow");
        }

        // ===== PHẤN NƯỚC =====
        if (selectedKey.contains("phan-nuoc") || selectedKey.contains("cushion")) {
            return categoryKey.contains("phan-nuoc")
                    || categoryKey.contains("cushion");
        }

        // ===== XỊT KHÓA NỀN =====
        if (selectedKey.contains("xit-khoa-nen")) {
            return categoryKey.contains("xit-khoa-nen")
                    || categoryKey.contains("setting-spray")
                    || categoryKey.contains("fixing-spray");
        }

        // ===== PHỤ KIỆN =====
        if (selectedKey.contains("phu-kien")) {
            return categoryKey.contains("phu-kien")
                    || categoryKey.contains("co-trang-diem")
                    || categoryKey.contains("mut-trang-diem")
                    || categoryKey.contains("dung-cu");
        }

        // ===== TẨY TRANG =====
        if (selectedKey.contains("tay-trang")) {
            return categoryKey.contains("tay-trang")
                    || categoryKey.contains("cleansing")
                    || categoryKey.contains("makeup-remover");
        }

        // ===== KEM CHỐNG NẮNG =====
        if (selectedKey.contains("kem-chong-nang")) {
            return categoryKey.contains("kem-chong-nang")
                    || categoryKey.contains("chong-nang")
                    || categoryKey.contains("sunscreen");
        }

        // ===== SỮA RỬA MẶT =====
        if (selectedKey.contains("sua-rua-mat")) {
            return categoryKey.contains("sua-rua-mat")
                    || categoryKey.contains("rua-mat")
                    || categoryKey.contains("cleanser");
        }

        // ===== NƯỚC HOA HỒNG / TONER =====
        if (selectedKey.contains("nuoc-hoa-hong")
                || selectedKey.contains("toner")
                || selectedKey.contains("lotion")) {

            return categoryKey.contains("nuoc-hoa-hong")
                    || categoryKey.contains("toner")
                    || categoryKey.contains("lotion");
        }

        // ===== TINH CHẤT DƯỠNG / SERUM / ESSENCE =====
        if (selectedKey.contains("tinh-chat")
                || selectedKey.contains("serum")
                || selectedKey.contains("essence")) {

            return categoryKey.contains("tinh-chat")
                    || categoryKey.contains("serum")
                    || categoryKey.contains("essence")
                    || categoryKey.contains("ampoule");
        }

        // ===== KEM DƯỠNG / GEL DƯỠNG =====
        if (selectedKey.contains("kem-duong")
                || selectedKey.contains("gel-duong")) {

            return categoryKey.contains("kem-duong")
                    || categoryKey.contains("gel-duong")
                    || categoryKey.contains("moisturizer")
                    || categoryKey.contains("cream");
        }

        // ===== MẶT NẠ =====
        if (selectedKey.contains("mat-na")) {
            return categoryKey.contains("mat-na")
                    || categoryKey.contains("mask");
        }

        return false;
    }

    private List<Category> flattenCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        List<Category> result = new ArrayList<>();

        for (Category category : categories) {
            collectFlatCategory(category, result);
        }

        return result;
    }

    private void collectFlatCategory(Category category, List<Category> output) {
        if (category == null || output == null) {
            return;
        }

        output.add(category);

        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }

        for (Category child : category.getChildren()) {
            collectFlatCategory(child, output);
        }
    }

    private Category findCategoryById(List<Category> categories, Integer id) {
        if (id == null || id <= 0 || categories == null || categories.isEmpty()) {
            return null;
        }

        for (Category category : categories) {
            if (category == null) {
                continue;
            }

            if (category.getId() == id) {
                return category;
            }

            Category child = findCategoryById(category.getChildren(), id);

            if (child != null) {
                return child;
            }
        }

        return null;
    }

    private String normalizeCategoryKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = java.text.Normalizer.normalize(
                value.trim(),
                java.text.Normalizer.Form.NFD
        );

        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'D');
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+|-+$", "");

        return normalized;
    }

    private boolean isCategoryParam(String paramName) {
        if (paramName == null) {
            return false;
        }

        return "category".equals(paramName)
                || "categoryId".equals(paramName)
                || "categoryIds".equals(paramName);
    }

    private String resolveCollectionTitle(
            List<Category> categories,
            List<Integer> selectedCategoryIds
    ) {
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return "Tất cả sản phẩm";
        }

        if (selectedCategoryIds.size() == 1) {
            if (isLipCategoryOrChild(categories, selectedCategoryIds.get(0))) {
                return "Trang điểm môi";
            }

            Category category = findCategoryById(categories, selectedCategoryIds.get(0));

            if (category != null && !isBlank(category.getName())) {
                return category.getName();
            }
        }

        return "Sản phẩm theo danh mục";
    }

    private String resolveCollectionDescription(
            List<Category> categories,
            List<Integer> selectedCategoryIds,
            int total
    ) {
        String title = resolveCollectionTitle(categories, selectedCategoryIds);

        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return "Khám phá toàn bộ sản phẩm đang hoạt động tại MyCosmetic.";
        }

        if ("Trang điểm môi".equalsIgnoreCase(title)) {
            return "Khám phá bộ sưu tập son môi, son dưỡng và chăm sóc môi được chọn lọc để hiển thị chuyên nghiệp hơn giống trang collection thực tế.";
        }

        return "Đang hiển thị " + total + " sản phẩm thuộc danh mục " + title
                + ". Bạn có thể lọc thêm theo thương hiệu, mức giá và đánh giá.";
    }

    private boolean isLipCategoryOrChild(List<Category> categories, Integer categoryId) {
        Category category = findCategoryById(categories, categoryId);

        if (category == null) {
            return false;
        }

        String key = normalizeCategoryKey(category.getName() + " " + category.getSlug());

        if (key.contains("son-moi") || key.contains("trang-diem-moi")) {
            return true;
        }

        Category parent = findParentCategoryByChildId(categories, categoryId);

        if (parent == null) {
            return false;
        }

        String parentKey = normalizeCategoryKey(parent.getName() + " " + parent.getSlug());

        return parentKey.contains("son-moi") || parentKey.contains("trang-diem-moi");
    }

    private String resolveHotCollectionKey(
            List<Category> categories,
            List<Integer> selectedCategoryIds
    ) {
        if (selectedCategoryIds == null || selectedCategoryIds.size() != 1) {
            return null;
        }

        Category category = findCategoryById(categories, selectedCategoryIds.get(0));

        if (category == null) {
            return null;
        }

        String key = normalizeCategoryKey(category.getName() + " " + category.getSlug());

        if (key.contains("son-moi")
                || key.contains("trang-diem-moi")
                || key.equals("moi")
                || key.contains("lip")) {
            return "lip-collection";
        }

        return null;
    }

    private List<String> buildSemanticCollectionKeywords(String hotCollectionKey) {
        if (!"lip-collection".equals(hotCollectionKey)) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                "son",
                "môi",
                "moi",
                "lip",
                "dưỡng môi",
                "duong moi",
                "son dưỡng",
                "son duong",
                "lip balm",
                "tint"
        );
    }

    private Category resolveLipPrimaryCategory(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        for (Category category : flattenCategories(categories)) {
            if (category == null) {
                continue;
            }

            String key = normalizeCategoryKey(category.getName() + " " + category.getSlug());

            if (key.contains("son-moi") || key.contains("trang-diem-moi")) {
                return category;
            }
        }

        return null;
    }

    private Category resolvePrimaryCategoryForSidebar(
            List<Category> categories,
            List<Integer> selectedCategoryIds
    ) {
        if (categories == null || categories.isEmpty()
                || selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return null;
        }

        for (Integer selectedId : selectedCategoryIds) {
            if (selectedId == null || selectedId <= 0) {
                continue;
            }

            Category selected = findCategoryById(categories, selectedId);

            if (selected == null) {
                continue;
            }

            if (selected.getChildren() != null && !selected.getChildren().isEmpty()) {
                return selected;
            }

            Category parent = findParentCategoryByChildId(categories, selectedId);

            if (parent != null) {
                return parent;
            }
        }

        return null;
    }

    private Category findParentCategoryByChildId(List<Category> categories, Integer childId) {
        if (categories == null || categories.isEmpty() || childId == null || childId <= 0) {
            return null;
        }

        for (Category parent : categories) {
            if (parent == null || parent.getChildren() == null) {
                continue;
            }

            for (Category child : parent.getChildren()) {
                if (child != null && child.getId() == childId) {
                    return parent;
                }
            }

            Category nestedParent = findParentCategoryByChildId(parent.getChildren(), childId);

            if (nestedParent != null) {
                return nestedParent;
            }
        }

        return null;
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

        List<Integer> result = new ArrayList<>();

        for (String rawValue : values) {
            if (isBlank(rawValue)) {
                continue;
            }

            String[] parts = rawValue.split(",");

            for (String part : parts) {
                if (isBlank(part)) {
                    continue;
                }

                String value = part.trim();

                if (value.equalsIgnoreCase("all")) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(value);

                    if (id > 0 && !result.contains(id)) {
                        result.add(id);
                    }

                } catch (NumberFormatException ignored) {
                    // Bỏ qua giá trị lỗi để tránh HTTP 500.
                }
            }
        }

        return result;
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
