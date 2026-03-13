package com.mycosmeticshop.controller.ProductController;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.dao.ProductImageDAO;
import com.mycosmeticshop.dao.ReviewDAO;
import com.mycosmeticshop.model.Product;
import com.mycosmeticshop.model.Review;
import com.mycosmeticshop.service.ProductPricingFacade;

/*
 * Servlet hiển thị trang chi tiết sản phẩm
 * URL truy cập: /product/*
 *
 * Ví dụ:
 * - /product/son-duong-moi
 * - /product/kem-chong-nang-abc
 *
 * Chức năng:
 * - Đọc slug từ URL
 * - Tìm sản phẩm theo slug
 * - Tải danh sách ảnh sản phẩm
 * - Tải danh sách đánh giá
 * - Tính lại số lượng review và điểm rating trung bình
 * - Tính giá cuối cùng của sản phẩm
 * - Gửi dữ liệu sang JSP để hiển thị
 */
@WebServlet("/product/*")
public class ProductDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để lấy dữ liệu sản phẩm
    private final ProductDAO productDAO = new ProductDAO();

    // DAO dùng để lấy danh sách ảnh của sản phẩm
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    // DAO dùng để lấy danh sách đánh giá sản phẩm
    private final ReviewDAO reviewDAO = new ReviewDAO();

    // Service dùng để xử lý giá cuối cùng của sản phẩm
    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

    /*
     * Phương thức GET
     * Hiển thị trang chi tiết sản phẩm theo slug
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) LẤY PATH INFO TỪ URL
        // =====================================================
        String path = req.getPathInfo();

        /*
         * Nếu URL không có slug hợp lệ
         * ví dụ:
         * - /product
         * - /product/
         * thì chuyển về trang danh sách sản phẩm
         */
        if (path == null || "/".equals(path) || path.length() <= 1) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 2) PARSE SLUG
        // =====================================================
        /*
         * Ví dụ:
         * path = /son-duong-moi
         * slug = son-duong-moi
         */
        String slug = path.substring(1);

        // Nếu slug có dấu "/" cuối thì bỏ đi
        if (slug.endsWith("/")) {
            slug = slug.substring(0, slug.length() - 1);
        }

        // Decode URL để xử lý ký tự đặc biệt hoặc tiếng Việt encode
        slug = URLDecoder.decode(slug, StandardCharsets.UTF_8.name());

        // =====================================================
        // 3) TÌM SẢN PHẨM THEO SLUG
        // =====================================================
        Product product = productDAO.findBySlug(slug);

        // Nếu không tìm thấy sản phẩm -> quay về trang danh sách
        if (product == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // =====================================================
        // 4) TẢI GALLERY ẢNH SẢN PHẨM
        // =====================================================
        product.setImages(productImageDAO.findByProductId(product.getId()));

        // =====================================================
        // 5) TẢI DANH SÁCH REVIEW
        // =====================================================
        List<Review> reviews = reviewDAO.findByProductId(product.getId());

        // =====================================================
        // 6) ĐỒNG BỘ SỐ LƯỢNG REVIEW VÀ ĐIỂM ĐÁNH GIÁ TRUNG BÌNH
        // =====================================================
        /*
         * Tránh trường hợp product.avgRating hoặc product.reviewCount
         * chưa được join đúng từ DAO sản phẩm
         * nên tính lại trực tiếp từ danh sách review.
         */
        int reviewCount = (reviews != null) ? reviews.size() : 0;
        product.setReviewCount(reviewCount);

        double avgRating = 0.0;

        if (reviewCount > 0) {
            int sum = 0;

            for (Review r : reviews) {
                sum += r.getRating();
            }

            avgRating = sum / (double) reviewCount;
        }

        product.setAvgRating(avgRating);

        // =====================================================
        // 7) TÍNH GIÁ CUỐI CÙNG CỦA SẢN PHẨM
        // =====================================================
        /*
         * finalPrice có thể bao gồm:
         * - giá gốc
         * - giá khuyến mãi
         * - logic giảm giá hiện hành
         */
        product.setFinalPrice(pricingFacade.getFinalPrice(product));

        // =====================================================
        // 8) GỬI DỮ LIỆU SANG JSP
        // =====================================================
        req.setAttribute("product", product);
        req.setAttribute("reviews", reviews);

        // =====================================================
        // 9) THIẾT LẬP THÔNG TIN TRANG
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | " + product.getTitle());

        // CSS của trang chi tiết sản phẩm
        req.setAttribute("pageCss", "product-detail.css");

        // Nội dung chính của trang
        req.setAttribute("pageContent", "/jsp/product/detail.jsp");

        // =====================================================
        // 10) RENDER QUA BASE LAYOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }
}