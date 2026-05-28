package com.webshop.app.controller.HomeController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.*;
import com.webshop.app.model.*;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // DATA 

        // 1. Lấy Banner
        List<Banner> banners = bannerDAO.findActiveBanners();
        req.setAttribute("banners", banners);

        // 2. Lấy Voucher
        List<Coupon> vouchers = couponDAO.findActiveCouponsForHome();
        req.setAttribute("vouchers", vouchers);

        // 3. Lấy Sản phẩm
        // tiêu chí: bán chạy (sold_qty) -> giảm giá sâu -> mới nhất
        List<Product> featuredProducts;

        try {
            featuredProducts = productDAO.findFeaturedTop12BestSellerDeepDiscount();
        } catch (RuntimeException ex) {
            // Fallback nếu DB chưa có store_order/store_order_item (hoặc query lỗi)
            featuredProducts = productDAO.findFeaturedTop12DeepDiscount();
        }

        req.setAttribute("products", featuredProducts);

        // Trong HomeServlet.java, tại phương thức doGet()
        FlashSale activeFS = flashSaleDAO.findActiveFlashSale();

        if (activeFS != null) {
            List<FlashSaleItem> fsItems = flashSaleItemDAO.findByFlashSale(activeFS.getId());

            req.setAttribute("activeFlashSale", activeFS);
            req.setAttribute("fsItems", fsItems);
        }

        List<Event> recentEvents = eventDAO.getRecentEvents(2);
        req.setAttribute("recentEvents", recentEvents);

        //  META 
        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");

        // JSP include CSS
        req.setAttribute("pageCss", "home.css");

        //  CONTENT 
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        //  LAYOUT 
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
