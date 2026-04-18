package com.webshop.app.controller.ProductController;

import java.io.IOException;
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

        // ===== READ PARAMS (đồng bộ với /products) =====
        String keyword = req.getParameter("q");
        String sort = req.getParameter("sort");
        String priceRange = req.getParameter("priceRange");

        Integer categoryId = parseInt(req.getParameter("category"));
        Integer brandId    = parseInt(req.getParameter("brand"));
        Integer minRating  = parseInt(req.getParameter("rating"));

        // Nếu không có q thì quay về /products
        if (keyword == null || keyword.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }
        keyword = keyword.trim();

        // ===== PAGINATION (giống /products) =====
        int pageSize = 18;
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (page < 1) page = 1;

        // Nếu DAO của bạn đã có countProducts và findProductsPaged như ProductListServlet
        int total = productDAO.countProducts(keyword, categoryId, brandId, priceRange, minRating);
        int totalPages = (int) Math.ceil(total / (double) pageSize);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<Product> products = productDAO.findProductsPaged(
                keyword,
                categoryId,
                brandId,
                sort,
                priceRange,
                minRating,
                page,
                pageSize
        );

        // final price
        products.forEach(p -> p.setFinalPrice(pricingFacade.getFinalPrice(p)));

        // ===== SIDEBAR DATA (để hiện đủ danh mục + brand) =====
        req.setAttribute("categories", categoryDAO.findParents());
        req.setAttribute("brands", brandDAO.findWithProductCount());

        // giữ trạng thái filter đang chọn
        req.setAttribute("priceRange", priceRange);
        req.setAttribute("selectedBrand", brandId);

        // ===== PAGE DATA (để list.jsp dùng chung) =====
        req.setAttribute("products", products);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        // để list.jsp lấy hiển thị tiêu đề/giữ keyword
        req.setAttribute("q", keyword);

        // ===== META =====
        req.setAttribute("pageTitle", "MyCosmetic | Tìm kiếm: " + keyword);
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // ===== RENDER =====
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
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
