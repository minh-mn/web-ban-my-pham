package com.mycosmeticshop.controller.HomeController;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import com.mycosmeticshop.dao.BannerDAO;
import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.model.Banner;
import com.mycosmeticshop.model.Product;

/*
 * Servlet xử lý trang chủ của website
 * URL truy cập: /home
 *
 * Chức năng:
 * - Lấy danh sách banner đang hoạt động
 * - Lấy danh sách sản phẩm nổi bật để hiển thị ở trang chủ
 * - Gửi dữ liệu sang JSP để render giao diện
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để lấy dữ liệu banner
    private final BannerDAO bannerDAO = new BannerDAO();

    // DAO dùng để lấy dữ liệu sản phẩm
    private final ProductDAO productDAO = new ProductDAO();

    /*
     * Phương thức GET
     * Hiển thị trang chủ
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // =====================================================
        // 1) LẤY DỮ LIỆU BANNER
        // =====================================================
        // Lấy tất cả banner đang hoạt động để hiển thị ở trang chủ
        List<Banner> banners = bannerDAO.findActiveBanners();
        req.setAttribute("banners", banners);

        // =====================================================
        // 2) LẤY DỮ LIỆU SẢN PHẨM NỔI BẬT
        // =====================================================
        /*
         * Chỉ lấy 12 sản phẩm nổi bật
         *
         * Tiêu chí ưu tiên:
         * 1. Bán chạy (sold_qty cao)
         * 2. Giảm giá sâu
         * 3. Sản phẩm mới hơn
         */
        List<Product> featuredProducts;

        try {
            // Truy vấn ưu tiên best seller + deep discount
            featuredProducts = productDAO.findFeaturedTop12BestSellerDeepDiscount();

        } catch (RuntimeException ex) {
            /*
             * Fallback:
             * Nếu database chưa có đủ bảng liên quan đến đơn hàng
             * như store_order hoặc store_order_item,
             * hoặc query gặp lỗi runtime,
             * thì dùng phương án thay thế chỉ dựa trên giảm giá sâu.
             */
            featuredProducts = productDAO.findFeaturedTop12DeepDiscount();
        }

        // Gửi danh sách sản phẩm sang JSP
        req.setAttribute("products", featuredProducts);

        // =====================================================
        // 3) THIẾT LẬP THÔNG TIN TRANG
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Trang chủ");

        /*
         * CSS của trang home
         * Nếu base.jsp load CSS theo dạng:
         * contextPath + /assets/css/${pageCss}
         * thì chỉ cần truyền tên file
         */
        req.setAttribute("pageCss", "home.css");

        // =====================================================
        // 4) CHỈ ĐỊNH NỘI DUNG TRANG
        // =====================================================
        req.setAttribute("pageContent", "/jsp/home/home.jsp");

        // =====================================================
        // 5) RENDER QUA LAYOUT CHUNG
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}