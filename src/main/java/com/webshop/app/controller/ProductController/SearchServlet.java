package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.model.Product;
import com.webshop.app.service.ProductPricingFacade;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // ===== READ PARAMS =====
        String keyword = req.getParameter("q");
        String sort = req.getParameter("sort");

        Integer categoryId = parseInt(req.getParameter("category"));
        Integer brandId = parseInt(req.getParameter("brand"));
        Integer minRating = parseInt(req.getParameter("rating"));

        /*
         * ProductDAO hiện tại đang nhận List:
         * - List<Integer> categoryIds
         * - List<Integer> brandIds
         * - List<String> priceRanges
         */
        List<Integer> categoryIds = toIdList(categoryId);
        List<Integer> brandIds = toIdList(brandId);
        List<String> priceRanges = toStringList(req.getParameterValues("priceRange"));

        // Lấy lại 1 priceRange để giữ tương thích với JSP cũ nếu JSP đang dùng ${priceRange}
        String priceRange = priceRanges.isEmpty() ? null : priceRanges.get(0);

        // Nếu không có q thì quay về /products
        if (keyword == null || keyword.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        keyword = keyword.trim();

        // ===== PAGINATION =====
        int pageSize = 18;
        int page = parseIntOrDefault(req.getParameter("page"), 1);

        if (page < 1) {
            page = 1;
        }

        int total = productDAO.countProducts(
                keyword,
                categoryIds,
                brandIds,
                priceRanges,
                minRating
        );

        int totalPages = (int) Math.ceil(total / (double) pageSize);

        if (totalPages < 1) {
            totalPages = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        List<Product> products = productDAO.findProductsPaged(
                keyword,
                categoryIds,
                brandIds,
                sort,
                priceRanges,
                minRating,
                page,
                pageSize
        );

        // ===== FINAL PRICE =====
        products.forEach(p -> p.setFinalPrice(pricingFacade.getFinalPrice(p)));

        // ===== SIDEBAR DATA =====
        req.setAttribute("categories", categoryDAO.findParents());
        req.setAttribute("brands", brandDAO.findWithProductCount());

        // ===== KEEP FILTER STATE =====
        req.setAttribute("q", keyword);
        req.setAttribute("sort", sort);

        req.setAttribute("selectedCategory", categoryId);
        req.setAttribute("selectedBrand", brandId);
        req.setAttribute("selectedRating", minRating);

        // Dành cho JSP cũ đang dùng 1 giá trị
        req.setAttribute("priceRange", priceRange);

        // Dành cho JSP mới đang dùng nhiều checkbox/filter
        req.setAttribute("selectedCategories", categoryIds);
        req.setAttribute("selectedBrands", brandIds);
        req.setAttribute("selectedPriceRanges", priceRanges);

        // ===== PAGE DATA =====
        req.setAttribute("products", products);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        // ===== META =====
        req.setAttribute("pageTitle", "MyCosmetic | Tìm kiếm: " + keyword);
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== RENDER =====
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    private List<Integer> toIdList(Integer id) {
        if (id == null || id <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(id);
    }

    private List<String> toStringList(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value.trim());
            }
        }

        return result;
    }

    private Integer parseInt(String v) {
        try {
            return (v != null && !v.isBlank()) ? Integer.parseInt(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntOrDefault(String v, int def) {
        try {
            return (v != null && !v.isBlank()) ? Integer.parseInt(v) : def;
        } catch (Exception e) {
            return def;
        }
    }
}