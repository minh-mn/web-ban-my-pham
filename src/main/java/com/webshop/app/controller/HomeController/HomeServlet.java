package com.webshop.app.controller.HomeController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.webshop.app.dao.BannerDAO;
import com.webshop.app.dao.CouponDAO;
import com.webshop.app.dao.EventDAO;
import com.webshop.app.dao.FlashSaleDAO;
import com.webshop.app.dao.FlashSaleItemDAO;
import com.webshop.app.dao.HomeSectionDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Banner;
import com.webshop.app.model.Category;
import com.webshop.app.model.Coupon;
import com.webshop.app.model.Event;
import com.webshop.app.model.FlashSale;
import com.webshop.app.model.FlashSaleItem;
import com.webshop.app.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int HOME_PRODUCT_LIMIT = 8;
    private static final int HOT_CATEGORY_LIMIT = 8;

    private final BannerDAO bannerDAO = new BannerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final HomeSectionDAO homeSectionDAO = new HomeSectionDAO();
    private final CouponDAO couponDAO = new CouponDAO();
    private final FlashSaleDAO flashSaleDAO = new FlashSaleDAO();
    private final FlashSaleItemDAO flashSaleItemDAO = new FlashSaleItemDAO();
    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        loadBanners(req);
        loadVouchers(req);
        loadHomeProductSections(req);
        loadFlashSale(req);
        loadRecentEvents(req);

        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");
        req.setAttribute("pageCss", "home.css");
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private void loadBanners(HttpServletRequest req) {
        try {
            List<Banner> banners = bannerDAO.findActiveBanners();
            req.setAttribute("banners", banners);
        } catch (RuntimeException e) {
            req.setAttribute("banners", new ArrayList<Banner>());
            req.setAttribute("homeBannerError", e.getMessage());
        }
    }

    private void loadVouchers(HttpServletRequest req) {
        try {
            List<Coupon> vouchers = couponDAO.findActiveCouponsForHome();
            req.setAttribute("vouchers", vouchers);
        } catch (RuntimeException e) {
            req.setAttribute("vouchers", new ArrayList<Coupon>());
            req.setAttribute("homeVoucherError", e.getMessage());
        }
    }

    private void loadHomeProductSections(HttpServletRequest req) {
        try {
            List<Product> featuredProducts = homeSectionDAO.findFeaturedProducts(HOME_PRODUCT_LIMIT);
            List<Product> bestSellingProducts = homeSectionDAO.findBestSellingProducts(HOME_PRODUCT_LIMIT);
            List<Product> deepDiscountProducts = homeSectionDAO.findDeepDiscountProducts(HOME_PRODUCT_LIMIT);
            List<Product> mostViewedProducts = homeSectionDAO.findMostViewedProducts(HOME_PRODUCT_LIMIT);
            List<Product> newProducts = homeSectionDAO.findNewProducts(HOME_PRODUCT_LIMIT);
            List<Category> hotCategories = homeSectionDAO.findHotCategories(HOT_CATEGORY_LIMIT);

            req.setAttribute("featuredProducts", featuredProducts);
            req.setAttribute("bestSellingProducts", bestSellingProducts);
            req.setAttribute("deepDiscountProducts", deepDiscountProducts);
            req.setAttribute("mostViewedProducts", mostViewedProducts);
            req.setAttribute("newProducts", newProducts);
            req.setAttribute("hotCategories", hotCategories);

            // Giữ tên attribute cũ để những JSP khác còn gọi ${products} không bị lỗi.
            req.setAttribute("products", featuredProducts);

        } catch (RuntimeException e) {
            loadFallbackFeaturedProducts(req, e);
        }
    }

    private void loadFallbackFeaturedProducts(HttpServletRequest req, RuntimeException rootError) {
        List<Product> fallbackProducts;

        try {
            fallbackProducts = productDAO.findFeaturedTop12BestSellerDeepDiscount();
        } catch (RuntimeException ex) {
            fallbackProducts = productDAO.findFeaturedTop12DeepDiscount();
        }

        req.setAttribute("featuredProducts", fallbackProducts);
        req.setAttribute("bestSellingProducts", new ArrayList<Product>());
        req.setAttribute("deepDiscountProducts", new ArrayList<Product>());
        req.setAttribute("mostViewedProducts", new ArrayList<Product>());
        req.setAttribute("newProducts", new ArrayList<Product>());
        req.setAttribute("hotCategories", new ArrayList<Category>());
        req.setAttribute("products", fallbackProducts);
        req.setAttribute("homeSectionError", rootError.getMessage());
    }

    private void loadFlashSale(HttpServletRequest req) {
        try {
            FlashSale activeFlashSale = flashSaleDAO.findActiveFlashSale();

            if (activeFlashSale != null) {
                List<FlashSaleItem> flashSaleItems = flashSaleItemDAO.findByFlashSale(activeFlashSale.getId());
                req.setAttribute("activeFlashSale", activeFlashSale);
                req.setAttribute("fsItems", flashSaleItems);
            }
        } catch (RuntimeException e) {
            req.setAttribute("homeFlashSaleError", e.getMessage());
        }
    }

    private void loadRecentEvents(HttpServletRequest req) {
        try {
            List<Event> recentEvents = eventDAO.getRecentEvents(2);
            req.setAttribute("recentEvents", recentEvents);
        } catch (RuntimeException e) {
            req.setAttribute("recentEvents", new ArrayList<Event>());
            req.setAttribute("homeEventError", e.getMessage());
        }
    }
}
