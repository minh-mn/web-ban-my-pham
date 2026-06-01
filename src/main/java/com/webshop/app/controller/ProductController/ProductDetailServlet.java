package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import com.webshop.app.dao.OrderItemDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductImageDAO;
import com.webshop.app.dao.ProductMediaDAO;
import com.webshop.app.dao.ProductVariantDAO;
import com.webshop.app.dao.ReviewDAO;
import com.webshop.app.dao.WishlistDAO;
import com.webshop.app.model.Product;
import com.webshop.app.model.ProductMedia;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.model.Review;
import com.webshop.app.model.User;
import com.webshop.app.service.ProductPricingFacade;
import com.webshop.app.service.RecommendationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/product", "/product/*", "/products/detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductMediaDAO productMediaDAO = new ProductMediaDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final RecommendationService recommendationService = new RecommendationService(orderItemDAO, productDAO);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Product product = resolveProduct(req);

        if (product == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        product.setImages(productImageDAO.findByProductId(product.getId()));

        List<ProductMedia> productMediaList = productMediaDAO.findByProductId(product.getId());
        List<ProductVariant> variants = productVariantDAO.findActiveByProductId(product.getId());

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
        product.setAvgRating(calculateAvgRating(reviews));
        product.setFinalPrice(pricingFacade.getFinalPrice(product));

        BigDecimal basePrice = product.getFinalPrice() != null
                ? product.getFinalPrice()
                : product.getPrice();

        BigDecimal priceSaved = BigDecimal.ZERO;
        if (product.getPrice() != null && basePrice != null && product.getPrice().compareTo(basePrice) > 0) {
            priceSaved = product.getPrice().subtract(basePrice);
        }

        List<Product> boughtTogether = Collections.emptyList();
        List<Product> related = Collections.emptyList();

        try {
            boughtTogether = recommendationService.getFrequentlyBought(product.getId(), (int) product.getCategoryId());
        } catch (Exception ignored) {
            boughtTogether = Collections.emptyList();
        }

        try {
            related = recommendationService.getRelatedProducts(product.getId(), (int) product.getCategoryId());
        } catch (Exception ignored) {
            related = Collections.emptyList();
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        boolean canReviewProduct = user != null && reviewDAO.canUserReviewProduct(user.getId(), product.getId());

        boolean inWishlist = false;
        if (user != null) {
            WishlistDAO wishlistDAO = new WishlistDAO();
            inWishlist = wishlistDAO.exists(user.getId(), product.getId());
        }

        req.setAttribute("product", product);
        req.setAttribute("productMediaList", productMediaList);
        req.setAttribute("variants", variants);
        req.setAttribute("reviews", reviews);
        req.setAttribute("reviewSort", reviewSort);
        req.setAttribute("reviewRating", reviewRating);
        req.setAttribute("reviewMedia", reviewMedia);
        req.setAttribute("basePrice", basePrice);
        req.setAttribute("priceSaved", priceSaved);
        req.setAttribute("boughtTogetherProducts", boughtTogether);
        req.setAttribute("relatedProducts", related);
        req.setAttribute("canReviewProduct", canReviewProduct);
        req.setAttribute("inWishlist", inWishlist);
        req.setAttribute("pageTitle", "MyCosmetic | " + product.getTitle());
        req.setAttribute("pageCss", "product-detail.css");
        req.setAttribute("pageContent", "/jsp/product/detail.jsp");

        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private Product resolveProduct(HttpServletRequest req) throws IOException {
        int id = parseInt(req.getParameter("id"), 0);
        String slug = extractSlug(req.getPathInfo());

        Product product = null;

        if (slug != null && !slug.isBlank()) {
            product = productDAO.findBySlug(slug);
        }

        if (product == null && id > 0) {
            product = productDAO.findById(id);
        }

        return product;
    }

    private String extractSlug(String pathInfo) throws IOException {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }

        String slug = pathInfo;
        if (slug.startsWith("/")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("/")) {
            slug = slug.substring(0, slug.length() - 1);
        }

        slug = URLDecoder.decode(slug, StandardCharsets.UTF_8.name()).trim();
        return slug.isBlank() ? null : slug;
    }

    private double calculateAvgRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }

        return sum / (double) reviews.size();
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

    private int parseInt(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
