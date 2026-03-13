package com.mycosmeticshop.controller.ProductController;

import com.mycosmeticshop.dao.BrandDAO;
import com.mycosmeticshop.dao.CategoryDAO;
import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.model.Product;
import com.mycosmeticshop.service.ProductPricingFacade;

// ===== Jakarta Servlet API (Tomcat 10+ dùng jakarta thay cho javax) =====
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/*
 * Servlet xử lý chức năng tìm kiếm sản phẩm
 * URL truy cập: /search
 *
 * Chức năng:
 * - Nhận từ khóa tìm kiếm từ request
 * - Hỗ trợ lọc theo category, brand, rating, price range
 * - Hỗ trợ sắp xếp và phân trang
 * - Tính giá cuối cùng cho từng sản phẩm
 * - Dùng chung giao diện list.jsp với trang /products
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO dùng để lấy dữ liệu sản phẩm
    private final ProductDAO productDAO = new ProductDAO();

    // DAO dùng để lấy dữ liệu danh mục
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // DAO dùng để lấy dữ liệu thương hiệu
    private final BrandDAO brandDAO = new BrandDAO();

    // Service dùng để tính giá cuối cùng của sản phẩm
    private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

    /*
     * Phương thức GET
     * Xử lý tìm kiếm sản phẩm
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Thiết lập encoding UTF-8 để tránh lỗi tiếng Việt
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // =====================================================
        // 1) ĐỌC CÁC THAM SỐ TÌM KIẾM / LỌC / SẮP XẾP
        // =====================================================
        // Đồng bộ tham số với /products để dùng chung list.jsp
        String keyword = req.getParameter("q");
        String sort = req.getParameter("sort");
        String priceRange = req.getParameter("priceRange");

        Integer categoryId = parseInt(req.getParameter("category"));
        Integer brandId = parseInt(req.getParameter("brand"));
        Integer minRating = parseInt(req.getParameter("rating"));

        // Nếu không có từ khóa tìm kiếm thì quay về /products
        if (keyword == null || keyword.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        // Chuẩn hóa từ khóa tìm kiếm
        keyword = keyword.trim();

        // =====================================================
        // 2) XỬ LÝ PHÂN TRANG
        // =====================================================
        int pageSize = 18;

        // Trang hiện tại, mặc định là 1
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        // Đếm tổng số sản phẩm phù hợp điều kiện tìm kiếm
        int total = productDAO.countProducts(keyword, categoryId, brandId, priceRange, minRating);

        // Tính tổng số trang
        int totalPages = (int) Math.ceil(total / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }

        // Nếu page vượt quá tổng số trang thì kéo về trang cuối
        if (page > totalPages) {
            page = totalPages;
        }

        // =====================================================
        // 3) TẢI DANH SÁCH SẢN PHẨM THEO ĐIỀU KIỆN TÌM KIẾM
        // =====================================================
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

        // =====================================================
        // 4) TÍNH GIÁ CUỐI CÙNG CHO TỪNG SẢN PHẨM
        // =====================================================
        products.forEach(p -> p.setFinalPrice(pricingFacade.getFinalPrice(p)));

        // =====================================================
        // 5) TẢI DỮ LIỆU SIDEBAR
        // =====================================================
        // Danh mục cha
        req.setAttribute("categories", categoryDAO.findParents());

        // Thương hiệu kèm số lượng sản phẩm
        req.setAttribute("brands", brandDAO.findWithProductCount());

        // Giữ lại trạng thái filter đang chọn
        req.setAttribute("priceRange", priceRange);
        req.setAttribute("selectedBrand", brandId);

        // Có thể giữ thêm để JSP dễ render lại form lọc
        req.setAttribute("selectedCategory", categoryId);
        req.setAttribute("selectedRating", minRating);
        req.setAttribute("sort", sort);

        // =====================================================
        // 6) GỬI DỮ LIỆU TRANG SANG JSP
        // =====================================================
        req.setAttribute("products", products);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("total", total);
        req.setAttribute("pageSize", pageSize);

        // Giữ lại từ khóa để hiển thị trên giao diện
        req.setAttribute("q", keyword);

        // =====================================================
        // 7) THIẾT LẬP THÔNG TIN TRANG
        // =====================================================
        req.setAttribute("pageTitle", "MyCosmetic | Tìm kiếm: " + keyword);
        req.setAttribute("pageCss", "product-list.css");
        req.setAttribute("pageContent", "/jsp/product/list.jsp");

        // =====================================================
        // 8) RENDER QUA BASE LAYOUT
        // =====================================================
        req.getRequestDispatcher("/jsp/common/base.jsp").forward(req, resp);
    }

    /*
     * Hàm parse String -> Integer
     * Trả về null nếu giá trị rỗng hoặc không hợp lệ
     */
    private Integer parseInt(String v) {
        try {
            return (v != null && !v.isBlank()) ? Integer.parseInt(v) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Hàm parse String -> int
     * Nếu lỗi thì trả về giá trị mặc định
     */
    private int parseIntOrDefault(String v, int def) {
        try {
            return (v != null && !v.isBlank()) ? Integer.parseInt(v) : def;
        } catch (Exception e) {
            return def;
        }
    }
}