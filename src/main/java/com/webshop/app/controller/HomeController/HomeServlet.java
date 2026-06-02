package com.webshop.app.controller.HomeController;

import com.webshop.app.dao.BannerDAO;
import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.EventDAO;
import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.WishlistDAO;
import com.webshop.app.model.Banner;
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.Event;
import com.webshop.app.model.FlashSale;
import com.webshop.app.model.FlashSaleItem;
import com.webshop.app.model.Product;
import com.webshop.app.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Flash Deal trang chủ chỉ chạy vào:
     * - Ngày đôi theo tháng: 1/1, 2/2, 3/3, ..., 12/12
     * - Ngày 25 hằng tháng
     */
    private static final ZoneId FLASH_DEAL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MONTHLY_FLASH_DEAL_DAY = 25;
    private static final int HOME_PRODUCT_LIMIT = 12;
    private static final int FEATURED_BRAND_LIMIT_PER_BRAND = 9;
    private static final int FEATURED_BRAND_MAX_COUNT = 6;

    private final BannerDAO bannerDAO = new BannerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CouponDAO couponDAO = new CouponDAO();
    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();
    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        loadBanners(req);

        List<Brand> brands = loadMenuData(req);

        loadVouchers(req);

        /*
         * Sản phẩm trang chủ:
         * - featuredProducts: nhóm nổi bật / giảm sâu / fallback chung.
         * - bestSellingProducts: nhóm Bán chạy riêng, lấy theo lượt bán thật.
         * - discoverProducts: nhóm Khám phá.
         * - featuredBrandProducts: nhóm sản phẩm cho Thương hiệu nổi bật.
         */
        List<Product> featuredProducts = loadFeaturedProducts();
        List<Product> bestSellingProducts = loadBestSellingProducts(featuredProducts);
        List<Product> discoverProducts = loadDiscoverProducts(featuredProducts);
        List<Product> featuredBrandProducts = loadFeaturedBrandProducts(brands, featuredProducts);
        List<Brand> featuredHomeBrands = loadFeaturedHomeBrands(brands, featuredBrandProducts);
        List<Product> realNewProducts = loadNewProducts();

        req.setAttribute("products", featuredProducts);
        req.setAttribute("featuredProducts", featuredProducts);
        req.setAttribute("bestSellingProducts", bestSellingProducts);
        req.setAttribute("mostViewedProducts", featuredProducts);
        req.setAttribute("newProducts", realNewProducts);
        req.setAttribute("discoverProducts", discoverProducts);
        req.setAttribute("featuredBrandProducts", featuredBrandProducts);
        req.setAttribute("featuredHomeBrands", featuredHomeBrands);

        loadFlashDeal(req, featuredProducts);

        loadEvents(req);
        loadWishlistIds(req);

        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");
        req.setAttribute("pageCss", "home.css");
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void loadBanners(HttpServletRequest req) {
        try {
            List<Banner> banners = bannerDAO.findActiveBanners();
            req.setAttribute("banners", banners);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("banners", new ArrayList<Banner>());
        }
    }

    private List<Brand> loadMenuData(HttpServletRequest req) {
        List<Brand> brands = new ArrayList<>();

        try {
            List<Category> categories = categoryDAO.findActiveTree();
            req.setAttribute("categories", categories);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("categories", new ArrayList<Category>());
        }

        try {
            brands = brandDAO.findAllWithProductCount();
            req.setAttribute("brands", brands);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("brands", new ArrayList<Brand>());
        }

        try {
            /*
             * Danh mục hot: cố định theo bộ 13 mục, loại bỏ Blind Box / Hộp Mù
             * trong DAO nếu đã cấu hình.
             */
            List<Category> hotCategories = categoryDAO.findHotCategoriesForHome(13);
            req.setAttribute("hotCategories", hotCategories);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("hotCategories", new ArrayList<Category>());
        }

        return brands;
    }

    private void loadVouchers(HttpServletRequest req) {
        try {
            List<Coupon> vouchers = couponDAO.findActiveCouponsForHome();
            req.setAttribute("vouchers", vouchers);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("vouchers", new ArrayList<Coupon>());
        }
    }

    private List<Product> loadFeaturedProducts() {
        try {
            List<Product> products = productDAO.findFeaturedTop12BestSellerDeepDiscount();

            if (products != null && !products.isEmpty()) {
                return products;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        try {
            List<Product> products = productDAO.findFeaturedTop12DeepDiscount();

            if (products != null) {
                return products;
            }
        } catch (RuntimeException nestedEx) {
            nestedEx.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<Product> loadBestSellingProducts(List<Product> fallbackProducts) {
        try {
            List<Product> products = productDAO.findHomeBestSellingProducts(HOME_PRODUCT_LIMIT);

            if (products != null && !products.isEmpty()) {
                return products;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        return fallbackProducts != null ? fallbackProducts : new ArrayList<>();
    }

    private List<Product> loadDiscoverProducts(List<Product> fallbackProducts) {
        try {
            List<Product> discoverProducts = productDAO.findHomeDiscoverProducts();

            if (discoverProducts != null && !discoverProducts.isEmpty()) {
                return discoverProducts;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        return fallbackProducts != null ? fallbackProducts : new ArrayList<>();
    }

    private List<Product> loadNewProducts() {
        try {
            List<Product> products = productDAO.findProductsPaged(
                    null,
                    null,
                    null,
                    "created_desc",
                    null,
                    null,
                    1,
                    HOME_PRODUCT_LIMIT
            );

            if (products != null) {
                return products;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<Product> loadFeaturedBrandProducts(List<Brand> brands, List<Product> fallbackProducts) {
        if (brands == null || brands.isEmpty()) {
            return fallbackProducts != null ? fallbackProducts : new ArrayList<>();
        }

        List<Integer> brandIds = new ArrayList<>();

        for (Brand brand : brands) {
            if (brand == null || brand.getId() <= 0) {
                continue;
            }

            brandIds.add(brand.getId());

            if (brandIds.size() >= FEATURED_BRAND_MAX_COUNT) {
                break;
            }
        }

        if (brandIds.isEmpty()) {
            return fallbackProducts != null ? fallbackProducts : new ArrayList<>();
        }

        try {
            /*
             * Lấy 9 sản phẩm mỗi thương hiệu:
             * - Trang chủ chỉ hiển thị tối đa 8.
             * - Sản phẩm thứ 9 dùng để biết cần hiện nút Xem thêm hay không.
             */
            List<Product> products = productDAO.findFeaturedProductsByBrandIds(
                    brandIds,
                    FEATURED_BRAND_LIMIT_PER_BRAND
            );

            if (products != null && !products.isEmpty()) {
                return products;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        return fallbackProducts != null ? fallbackProducts : new ArrayList<>();
    }

    private List<Brand> loadFeaturedHomeBrands(List<Brand> brands, List<Product> featuredBrandProducts) {
        List<Brand> result = new ArrayList<>();

        if (brands == null || brands.isEmpty()) {
            return result;
        }

        Set<Long> brandIdsWithProducts = new HashSet<>();

        if (featuredBrandProducts != null) {
            for (Product product : featuredBrandProducts) {
                if (product != null && product.getBrandId() > 0) {
                    brandIdsWithProducts.add(product.getBrandId());
                }
            }
        }

        for (Brand brand : brands) {
            if (brand == null || brand.getId() <= 0) {
                continue;
            }

            if (!brandIdsWithProducts.isEmpty() && !brandIdsWithProducts.contains((long) brand.getId())) {
                continue;
            }

            result.add(brand);

            if (result.size() >= FEATURED_BRAND_MAX_COUNT) {
                break;
            }
        }

        if (!result.isEmpty()) {
            return result;
        }

        for (Brand brand : brands) {
            if (brand == null || brand.getId() <= 0) {
                continue;
            }

            result.add(brand);

            if (result.size() >= FEATURED_BRAND_MAX_COUNT) {
                break;
            }
        }

        return result;
    }

    private void loadFlashDeal(HttpServletRequest req, List<Product> fallbackProducts) {
        LocalDate today = LocalDate.now(FLASH_DEAL_ZONE);
        boolean shouldShowFlashDeal = isFlashDealCampaignDay(today);

        req.setAttribute("shouldShowFlashDeal", shouldShowFlashDeal);
        req.setAttribute("flashDealToday", today);

        if (!shouldShowFlashDeal) {
            setEmptyFlashDealAttributes(req);
            return;
        }

        List<Product> flashSaleProducts = loadFlashSaleProducts(req);

        if (!flashSaleProducts.isEmpty()) {
            req.setAttribute("flashSaleProducts", flashSaleProducts);
            req.setAttribute("deepDiscountProducts", flashSaleProducts);
            return;
        }

        /*
         * Chỉ fallback trong đúng ngày campaign để block không trống khi admin chưa tạo
         * active flash sale hoặc chưa thêm flash_sale_items.
         */
        List<Product> fallback = fallbackProducts != null ? fallbackProducts : new ArrayList<Product>();

        req.setAttribute("flashSaleProducts", fallback);
        req.setAttribute("deepDiscountProducts", fallback);
    }

    private boolean isFlashDealCampaignDay(LocalDate date) {
        if (date == null) {
            return false;
        }

        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        boolean doubleDay = day == month;
        boolean monthlyPaydaySale = day == MONTHLY_FLASH_DEAL_DAY;

        return doubleDay || monthlyPaydaySale;
    }

    private void setEmptyFlashDealAttributes(HttpServletRequest req) {
        req.setAttribute("activeFlashSale", null);
        req.setAttribute("fsItems", new ArrayList<FlashSaleItem>());
        req.setAttribute("flashSaleProducts", new ArrayList<Product>());
        req.setAttribute("deepDiscountProducts", new ArrayList<Product>());
    }

    private List<Product> loadFlashSaleProducts(HttpServletRequest req) {
        List<Product> flashSaleProducts = new ArrayList<>();
        List<FlashSaleItem> fsItems = new ArrayList<>();

        try {
            FlashSale activeFlashSale = flashSaleDAO.findActiveFlashSale();
            req.setAttribute("activeFlashSale", activeFlashSale);

            if (activeFlashSale == null) {
                req.setAttribute("fsItems", fsItems);
                return flashSaleProducts;
            }

            fsItems = flashSaleItemDAO.findByFlashSale(activeFlashSale.getId());
            req.setAttribute("fsItems", fsItems);

            for (FlashSaleItem item : fsItems) {
                if (item == null || item.getProduct() == null) {
                    continue;
                }

                Product product = item.getProduct();
                BigDecimal originalPrice = product.getPrice();
                BigDecimal flashPrice = BigDecimal.valueOf(item.getFlashPrice());

                if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    originalPrice = flashPrice;
                    product.setPrice(originalPrice);
                }

                if (flashPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    flashPrice = originalPrice;
                }

                product.setFinalPrice(flashPrice);
                product.setSoldQuantity(item.getSoldQuantity());

                if (originalPrice.compareTo(BigDecimal.ZERO) > 0
                        && flashPrice.compareTo(originalPrice) < 0) {

                    int discountPercent = originalPrice
                            .subtract(flashPrice)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(originalPrice, 0, RoundingMode.HALF_UP)
                            .intValue();

                    product.setDiscountPercent(discountPercent);
                }

                flashSaleProducts.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("fsItems", fsItems);
        }

        return flashSaleProducts;
    }

    private void loadEvents(HttpServletRequest req) {
        try {
            List<Event> recentEvents = eventDAO.getRecentEvents(2);
            req.setAttribute("recentEvents", recentEvents);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("recentEvents", new ArrayList<Event>());
        }
    }

    private void loadWishlistIds(HttpServletRequest req) {
        Set<Integer> wishlistIds = new HashSet<>();

        try {
            User user = (User) req.getSession().getAttribute("user");

            if (user != null) {
                WishlistDAO wishlistDAO = new WishlistDAO();
                wishlistIds = new HashSet<>(wishlistDAO.findProductIdsByUser(user.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute("wishlistIds", wishlistIds);
    }
}
