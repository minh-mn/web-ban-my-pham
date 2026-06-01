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

        // 1. Banner
        List<Banner> banners = bannerDAO.findActiveBanners();
        req.setAttribute("banners", banners);

        // 2. Menu / danh mục / thương hiệu
        try {
            List<Category> categories = categoryDAO.findActiveTree();
            req.setAttribute("categories", categories);

            List<Brand> brands = brandDAO.findAllWithProductCount();
            req.setAttribute("brands", brands);

            // Danh mục hot: cố định theo bộ 13 mục, loại bỏ Blind Box / Hộp Mù.
            List<Category> hotCategories = categoryDAO.findHotCategoriesForHome(13);
            req.setAttribute("hotCategories", hotCategories);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Voucher
        try {
            List<Coupon> vouchers = couponDAO.findActiveCouponsForHome();
            req.setAttribute("vouchers", vouchers);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("vouchers", new ArrayList<Coupon>());
        }

        // 4. Sản phẩm trang chủ
        List<Product> featuredProducts = loadFeaturedProducts();
        List<Product> discoverProducts = loadDiscoverProducts(featuredProducts);

        // Các section bên dưới dùng các biến này. Nếu DAO chưa tách riêng từng nhóm,
        // dùng featuredProducts làm fallback để tránh section bị mất khỏi trang chủ.
        req.setAttribute("products", featuredProducts);
        req.setAttribute("featuredProducts", featuredProducts);
        req.setAttribute("discoverProducts", discoverProducts);
        req.setAttribute("bestSellingProducts", featuredProducts);
        req.setAttribute("mostViewedProducts", featuredProducts);
        req.setAttribute("newProducts", featuredProducts);

        // 5. Flash Deal ở trang chủ
        List<Product> flashSaleProducts = loadFlashSaleProducts(req);

        if (!flashSaleProducts.isEmpty()) {
            req.setAttribute("flashSaleProducts", flashSaleProducts);
            req.setAttribute("deepDiscountProducts", flashSaleProducts);
        } else {
            // Fallback để block FLASH DEAL không biến mất khi chưa có active flash_sale_items.
            req.setAttribute("flashSaleProducts", featuredProducts);
            req.setAttribute("deepDiscountProducts", featuredProducts);
        }

        // 6. Sự kiện hot
        try {
            List<Event> recentEvents = eventDAO.getRecentEvents(2);
            req.setAttribute("recentEvents", recentEvents);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("recentEvents", new ArrayList<Event>());
        }

        // 7. Wishlist state
        loadWishlistIds(req);

        // 8. Meta / layout
        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");
        req.setAttribute("pageCss", "home.css");
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private List<Product> loadFeaturedProducts() {
        try {
            return productDAO.findFeaturedTop12BestSellerDeepDiscount();
        } catch (RuntimeException ex) {
            try {
                return productDAO.findFeaturedTop12DeepDiscount();
            } catch (RuntimeException nestedEx) {
                nestedEx.printStackTrace();
                return new ArrayList<>();
            }
        }
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
        }

        return flashSaleProducts;
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
