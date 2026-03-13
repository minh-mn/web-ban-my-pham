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

// ===== Java core =====
import java.io.IOException;
import java.util.List;

/*
 * Servlet hiển thị danh sách sản phẩm
 * URL truy cập: /products
 *
 * Chức năng:
 * - Đọc các tham số tìm kiếm / lọc / sắp xếp
 * - Hỗ trợ phân trang
 * - Tải danh sách sản phẩm theo điều kiện
 * - Tính giá cuối cùng cho từng sản phẩm
 * - Tải dữ liệu sidebar như danh mục và thương hiệu
 * - Gửi dữ liệu sang JSP để hiển thị
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

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
	 * Hiển thị danh sách sản phẩm có hỗ trợ tìm kiếm, lọc, sắp xếp và phân trang
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
		String keyword = req.getParameter("q");
		String sort = req.getParameter("sort");
		String priceRange = req.getParameter("priceRange");

		Integer categoryId = parseInt(req.getParameter("category"));
		Integer brandId = parseInt(req.getParameter("brand"));
		Integer minRating = parseInt(req.getParameter("rating"));

		// =====================================================
		// 2) XỬ LÝ PHÂN TRANG
		// =====================================================
		int pageSize = 18;

		// Trang hiện tại, mặc định là 1
		int page = parseIntOrDefault(req.getParameter("page"), 1);
		if (page < 1) {
			page = 1;
		}

		// Đếm tổng số sản phẩm theo điều kiện lọc
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
		// 3) TẢI DANH SÁCH SẢN PHẨM THEO TRANG
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
		/*
		 * finalPrice có thể bao gồm:
		 * - giá gốc
		 * - giá khuyến mãi
		 * - logic giảm giá hiện hành
		 */
		products.forEach(p -> p.setFinalPrice(pricingFacade.getFinalPrice(p)));

		// =====================================================
		// 5) TẢI DỮ LIỆU SIDEBAR
		// =====================================================
		// Danh mục cha
		req.setAttribute("categories", categoryDAO.findParents());

		// Thương hiệu kèm số lượng sản phẩm
		req.setAttribute("brands", brandDAO.findWithProductCount());

		// Giữ lại trạng thái filter hiện tại
		req.setAttribute("priceRange", priceRange);
		req.setAttribute("selectedBrand", brandId);

		// =====================================================
		// 6) GỬI DỮ LIỆU TRANG SANG JSP
		// =====================================================
		req.setAttribute("products", products);
		req.setAttribute("page", page);
		req.setAttribute("totalPages", totalPages);
		req.setAttribute("total", total);
		req.setAttribute("pageSize", pageSize);

		// =====================================================
		// 7) THIẾT LẬP THÔNG TIN TRANG
		// =====================================================
		req.setAttribute("pageTitle", "MyCosmetic | Sản phẩm");
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