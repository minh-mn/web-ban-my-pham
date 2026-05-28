package com.webshop.app.controller.ProductController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ProductDAO productDAO = new ProductDAO();
	private final CategoryDAO categoryDAO = new CategoryDAO();
	private final BrandDAO brandDAO = new BrandDAO();

	private final ProductPricingFacade pricingFacade = new ProductPricingFacade();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		// ===== 1. ĐỌC THAM SỐ DẠNG LIST ĐỂ HỖ TRỢ MULTI-FILTER =====
		String keyword = req.getParameter("q");
		String sort = req.getParameter("sort");

		List<String> priceRangeList = parseStringList(req.getParameterValues("priceRange"));
		List<Integer> selectedCategoryList = parseIntegerList(req.getParameterValues("category"));
		List<Integer> selectedBrandList = parseIntegerList(req.getParameterValues("brand"));

		Integer minRating = parseInt(req.getParameter("rating"));

		// ===== 2. PHÂN TRANG =====
		int pageSize = 18;
		int page = parseIntOrDefault(req.getParameter("page"), 1);
		if (page < 1)
			page = 1;

		// Gọi hàm đếm sản phẩm với tham số dạng dữ liệu đầu vào mới (List)
		int total = productDAO.countProducts(keyword, selectedCategoryList, selectedBrandList, priceRangeList, minRating);
		int totalPages = (int) Math.ceil(total / (double) pageSize);
		if (totalPages < 1)
			totalPages = 1;
		if (page > totalPages)
			page = totalPages;

		// ===== 3. TẢI DANH SÁCH SẢN PHẨM =====
		List<Product> products = productDAO.findProductsPaged(
				keyword,
				selectedCategoryList,
				selectedBrandList,
				sort,
				priceRangeList,
				minRating,
				page,
				pageSize
		);

		// Tính giá khuyến mãi cuối cùng
		products.forEach(p -> p.setFinalPrice(pricingFacade.getFinalPrice(p)));

		// ===== 4. DỮ LIỆU THANH SIDEBAR =====
		req.setAttribute("categories", categoryDAO.findParents());
		req.setAttribute("brands", brandDAO.findWithProductCount());

		// Giữ lại trạng thái các bộ lọc đã tick chọn trên giao diện
		req.setAttribute("priceRangeList", priceRangeList);
		req.setAttribute("selectedCategoryList", selectedCategoryList);
		req.setAttribute("selectedBrandList", selectedBrandList);
		req.setAttribute("minRating", minRating);

		// ===== 5. DỮ LIỆU PHÂN TRANG & HIỂN THỊ =====
		req.setAttribute("products", products);
		req.setAttribute("page", page);
		req.setAttribute("totalPages", totalPages);
		req.setAttribute("total", total);
		req.setAttribute("pageSize", pageSize);

		req.setAttribute("pageTitle", "MyCosmetic | Sản phẩm");
		req.setAttribute("pageCss", "product-list.css");
		req.setAttribute("pageContent", "/jsp/product/list.jsp");

		// ===== 6. RENDER =====
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

	// Hàm ép kiểu an toàn: Tự động loại bỏ chuỗi "all" và các chuỗi lỗi
	private List<Integer> parseIntegerList(String[] values) {
		if (values == null || values.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(values)
				.filter(v -> v != null && !v.isBlank() && !v.equalsIgnoreCase("all")) // Bỏ qua chữ "all"
				.map(v -> {
					try {
						return Integer.parseInt(v);
					} catch (NumberFormatException e) {
						return null; // Tránh quăng lỗi 500 nếu gặp chuỗi lạ không phải số
					}
				})
				.filter(v -> v != null)
				.collect(Collectors.toList());
	}

	private List<String> parseStringList(String[] values) {
		if (values == null || values.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(values)
				.filter(v -> v != null && !v.isBlank())
				.collect(Collectors.toList());
	}
}
