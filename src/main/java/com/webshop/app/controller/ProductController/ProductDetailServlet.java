package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.webshop.app.dao.HomeSectionDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductImageDAO;
import com.webshop.app.dao.ProductMediaDAO;
import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.model.Product;
import com.webshop.app.model.ProductMedia;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;
import com.webshop.app.service.ProductPricingFacade;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/product/*")
public class ProductDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final HomeSectionDAO homeSectionDAO = new HomeSectionDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    // Issue 123: DAO lấy media chi tiết sản phẩm gồm ảnh/video
    private final ProductMediaDAO productMediaDAO = new ProductMediaDAO();

    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();

        if (path == null || "/".equals(path) || path.length() <= 1) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        String slug = path.substring(1);

        if (slug.endsWith("/")) {
            slug = slug.substring(0, slug.length() - 1);
        }

        slug = URLDecoder.decode(slug, StandardCharsets.UTF_8.name());

        Product product = productDAO.findBySlug(slug);

        if (product == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // Issue 27: tăng lượt xem để phục vụ nhóm "Sản phẩm được xem nhiều" ở trang chủ.
        try {
            homeSectionDAO.increaseViewCount(product.getId());
            product.setViewCount(homeSectionDAO.findViewCountByProductId(product.getId()));
        } catch (RuntimeException ignored) {
            // Không chặn trang chi tiết nếu DB chưa chạy patch view_count.
        }

        /*
         * Gallery ảnh cũ của sản phẩm.
         */
        product.setImages(productImageDAO.findByProductId(product.getId()));

        /*
         * Issue 123:
         * Lấy danh sách media chi tiết sản phẩm gồm ảnh/video.
         * JSP detail.jsp sẽ dùng productMediaList để hiển thị.
         */
        List<ProductMedia> productMediaList = productMediaDAO.findByProductId(product.getId());

        List<ProductVariant> variants =
                productVariantDAO.findActiveByProductId(product.getId());

        Integer reviewRating = parseNullableRating(req.getParameter("reviewRating"));
        String reviewSort = req.getParameter("reviewSort");
        String reviewMedia = req.getParameter("reviewMedia");

        List<Review> reviews = reviewDAO.findByProductId(
                product.getId(),
                reviewSort,
                reviewRating,
                reviewMedia
        );

        int reviewCount = reviews != null ? reviews.size() : 0;
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
        product.setFinalPrice(pricingFacade.getFinalPrice(product));

        req.setAttribute("product", product);
        req.setAttribute("productMediaList", productMediaList);
        req.setAttribute("reviews", reviews);
        req.setAttribute("variants", variants);
        req.setAttribute("reviewSort", reviewSort);
        req.setAttribute("reviewRating", reviewRating);
        req.setAttribute("reviewMedia", reviewMedia);

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        boolean canReviewProduct =
                user != null && reviewDAO.canUserReviewProduct(user.getId(), product.getId());

        req.setAttribute("canReviewProduct", canReviewProduct);

        req.setAttribute("pageTitle", "MyCosmetic | " + product.getTitle());
        req.setAttribute("pageCss", "product-detail.css");
        req.setAttribute("pageContent", "/jsp/product/detail.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp")
                .forward(req, resp);
    }

    private Integer parseNullableRating(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }

            int rating = Integer.parseInt(value.trim());

            return rating >= 1 && rating <= 5 ? rating : null;
        } catch (Exception e) {
            return null;
        }
    }
}