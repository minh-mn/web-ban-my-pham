package com.webshop.app.controller.HomeController;

import java.io.IOException;
import java.util.List;

import com.webshop.app.dao.BannerDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Banner;
import com.webshop.app.model.Product;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ================= DATA =================
        List<Banner> banners = bannerDAO.findActiveBanners();
        req.setAttribute("banners", banners);

        // ✅ CHỈ LẤY 12 SẢN PHẨM NỔI BẬT:
        // tiêu chí: bán chạy (sold_qty) -> giảm giá sâu -> mới nhất
        List<Product> featuredProducts;

        try {
            featuredProducts = productDAO.findFeaturedTop12BestSellerDeepDiscount();
        } catch (RuntimeException ex) {
            // Fallback nếu DB chưa có store_order/store_order_item (hoặc query lỗi)
            featuredProducts = productDAO.findFeaturedTop12DeepDiscount();
        }

        req.setAttribute("products", featuredProducts);

        // ================= META =================
        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");

        // ✅ JSP include CSS
        req.setAttribute("pageCss", "home.css");

        // ================= CONTENT =================
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        // ================= LAYOUT =================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}
