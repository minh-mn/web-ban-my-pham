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
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Banner
        List<Banner> banners = bannerDAO.findActiveBanners();
        req.setAttribute("banners", banners);

        // 2. Danh mục + thương hiệu
        try {
            List<Category> categories = categoryDAO.findActiveTree();
            List<Brand> brands = brandDAO.findAllWithProductCount();

            req.setAttribute("categories", categories);
            req.setAttribute("brands", brands);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Voucher
        List<Coupon> vouchers = couponDAO.findActiveCouponsForHome();
        req.setAttribute("vouchers", vouchers);

        // 4. Sản phẩm nổi bật / fallback cho các section trang chủ
        List<Product> featuredProducts;

        try {
            featuredProducts = productDAO.findFeaturedTop12BestSellerDeepDiscount();
        } catch (RuntimeException ex) {
            featuredProducts = productDAO.findFeaturedTop12DeepDiscount();
        }

        req.setAttribute("products", featuredProducts);
        req.setAttribute("featuredProducts", featuredProducts);
        req.setAttribute("bestSellingProducts", featuredProducts);
        req.setAttribute("mostViewedProducts", featuredProducts);
        req.setAttribute("newProducts", featuredProducts);

        // 5. Flash Sale thật: lấy từ flash_sales + flash_sale_items
        FlashSale activeFlashSale = flashSaleDAO.findActiveFlashSale();
        List<FlashSaleItem> fsItems = new ArrayList<>();
        List<Product> flashSaleProducts = new ArrayList<>();

        if (activeFlashSale != null) {
            fsItems = flashSaleItemDAO.findByFlashSale(activeFlashSale.getId());

            for (FlashSaleItem item : fsItems) {
                Product product = item.getProduct();

                if (product == null) {
                    continue;
                }

                applyFlashSaleInfoToProduct(product, item);
                flashSaleProducts.add(product);
            }
        }

        req.setAttribute("activeFlashSale", activeFlashSale);
        req.setAttribute("fsItems", fsItems);
        req.setAttribute("flashSaleProducts", flashSaleProducts);

        // Giữ lại tên biến cũ để JSP cũ không bị rỗng nếu còn gọi deepDiscountProducts
        req.setAttribute("deepDiscountProducts", flashSaleProducts);

        // 6. Sự kiện
        List<Event> recentEvents = eventDAO.getRecentEvents(2);
        req.setAttribute("recentEvents", recentEvents);

        // 7. Wishlist của user đang đăng nhập
        User user = (User) req.getSession().getAttribute("user");
        Set<Integer> wishlistIds = new HashSet<>();

        if (user != null) {
            wishlistIds = new HashSet<>(wishlistDAO.findProductIdsByUser(user.getId()));
        }

        req.setAttribute("wishlistIds", wishlistIds);

        // 8. Meta + layout
        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");
        req.setAttribute("pageCss", "home.css");
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void applyFlashSaleInfoToProduct(Product product, FlashSaleItem item) {
        BigDecimal originalPrice = product.getPrice();
        BigDecimal flashPrice = BigDecimal.valueOf(item.getFlashPrice());

        product.setFinalPrice(flashPrice);
        product.setSoldQuantity(item.getSoldQuantity());
        product.setStock(Math.max(item.getQuantity() - item.getSoldQuantity(), 0));

        if (originalPrice != null
                && originalPrice.compareTo(BigDecimal.ZERO) > 0
                && flashPrice.compareTo(originalPrice) < 0) {

            int discountPercent = originalPrice
                    .subtract(flashPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 0, RoundingMode.HALF_UP)
                    .intValue();

            product.setDiscountPercent(discountPercent);
        }
    }
}
