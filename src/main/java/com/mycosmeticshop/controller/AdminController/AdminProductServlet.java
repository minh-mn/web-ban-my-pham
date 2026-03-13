package com.mycosmeticshop.controller.AdminController;

import com.mycosmeticshop.dao.BrandDAO;
import com.mycosmeticshop.dao.CategoryDAO;
import com.mycosmeticshop.dao.ProductDAO;
import com.mycosmeticshop.dao.ProductImageDAO;
import com.mycosmeticshop.model.Brand;
import com.mycosmeticshop.model.Category;
import com.mycosmeticshop.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@MultipartConfig(
		fileSizeThreshold = 1024 * 1024,
		maxFileSize = 10 * 1024 * 1024,
		maxRequestSize = 60 * 1024 * 1024
)
@WebServlet("/admin/products")
public class AdminProductServlet extends HttpServlet {

	// DAO thao tác với bảng product
	private final ProductDAO productDAO = new ProductDAO();

	// DAO thao tác với bảng ảnh phụ của sản phẩm
	private final ProductImageDAO productImageDAO = new ProductImageDAO();

	// DAO phục vụ dropdown danh mục và thương hiệu
	private final CategoryDAO categoryDAO = new CategoryDAO();
	private final BrandDAO brandDAO = new BrandDAO();

	/* ======================================================
       THƯ MỤC LƯU ẢNH TRONG WEBAPP
       ====================================================== */
	private static final String MAIN_DIR = "/assets/images/products";
	private static final String GALLERY_DIR = "/assets/images/products/gallery";

	/* ======================================================
       JSP PATHS
       Nếu thư mục thật của bạn là /jsp/admin/products/... thì
       đổi lại 2 hằng số này cho khớp.
       ====================================================== */
	private static final String JSP_FORM = "/jsp/admin/product/product_form.jsp";
	private static final String JSP_LIST = "/jsp/admin/product/product_list.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// Thiết lập UTF-8 để tránh lỗi tiếng Việt
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		// Lấy action từ URL
		String action = req.getParameter("action");
		if (action == null) {
			action = "list";
		}

		switch (action) {

			// =========================
			// HIỂN THỊ FORM THÊM SẢN PHẨM
			// =========================
			case "new": {
				loadDropdowns(req);
				req.getRequestDispatcher(JSP_FORM).forward(req, resp);
				return;
			}

			// =========================
			// HIỂN THỊ FORM SỬA SẢN PHẨM
			// =========================
			case "edit": {
				int id = parseInt(req.getParameter("id"), -1);

				if (id <= 0) {
					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				Product product = productDAO.findByIdAdmin(id);

				if (product == null) {
					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				// Load danh sách ảnh phụ
				product.setImages(productImageDAO.findByProductId(product.getId()));

				req.setAttribute("product", product);
				loadDropdowns(req);

				req.getRequestDispatcher(JSP_FORM).forward(req, resp);
				return;
			}

			// =========================
			// HIỂN THỊ DANH SÁCH SẢN PHẨM
			// =========================
			default: {
				String keyword = req.getParameter("keyword");
				Integer categoryId = parseIntObj(req.getParameter("categoryId"));
				Integer brandId = parseIntObj(req.getParameter("brandId"));
				String sort = req.getParameter("sort");

				List<Product> products = productDAO.findProductsAdmin(keyword, categoryId, brandId, sort);
				req.setAttribute("products", products);

				loadDropdowns(req);
				req.getRequestDispatcher(JSP_LIST).forward(req, resp);
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setCharacterEncoding("UTF-8");

		String action = req.getParameter("action");
		if (action == null) {
			action = "create";
		}

		try {
			switch (action) {

				// =========================
				// TẠO MỚI SẢN PHẨM
				// =========================
				case "create": {
					Product product = buildFromRequest(req);

					// Ảnh chính
					String mainImage = saveIfPresent(req, "imageMain", MAIN_DIR);
					if (mainImage != null) {
						product.setImage(mainImage);
					}

					// Lưu sản phẩm, lấy id mới
					int newId = productDAO.create(product);

					// Ảnh gallery
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", GALLERY_DIR);
					if (!gallery.isEmpty()) {
						int displayOrder = 0;
						for (String imgPath : gallery) {
							productImageDAO.insert(newId, imgPath, displayOrder++);
						}
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				// =========================
				// CẬP NHẬT SẢN PHẨM
				// =========================
				case "update": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id <= 0) {
						resp.sendRedirect(req.getContextPath() + "/admin/products");
						return;
					}

					Product product = buildFromRequest(req);
					product.setId(id);

					// Nếu có upload ảnh chính mới thì dùng ảnh mới, không thì giữ ảnh cũ
					String existingMain = req.getParameter("existingImage");
					String mainImage = saveIfPresent(req, "imageMain", MAIN_DIR);

					if (mainImage != null) {
						product.setImage(mainImage);
					} else {
						product.setImage(existingMain);
					}

					productDAO.update(product);

					// Nếu có upload gallery mới thì thay toàn bộ gallery cũ
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", GALLERY_DIR);
					if (!gallery.isEmpty()) {
						productImageDAO.deleteByProductId(id);

						int displayOrder = 0;
						for (String imgPath : gallery) {
							productImageDAO.insert(id, imgPath, displayOrder++);
						}
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				// =========================
				// XÓA SẢN PHẨM
				// =========================
				case "delete": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id > 0) {
						// Xóa review trước để tránh lỗi khóa ngoại
						productDAO.deleteReviewsByProductId(id);

						// Xóa ảnh phụ trong DB
						productImageDAO.deleteByProductId(id);

						// Xóa sản phẩm trong DB
						productDAO.delete(id);
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				default:
					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
			}

		} catch (IllegalArgumentException ex) {
			// Nếu validate lỗi thì quay lại form và giữ dữ liệu đã nhập
			req.setAttribute("error", ex.getMessage());
			loadDropdowns(req);

			Product product = buildFromRequestSafe(req);

			if ("update".equalsIgnoreCase(action)) {
				int id = parseInt(req.getParameter("id"), 0);
				product.setId(id);
				product.setImage(req.getParameter("existingImage"));

				if (id > 0) {
					product.setImages(productImageDAO.findByProductId(id));
				}
			}

			req.setAttribute("product", product);
			req.getRequestDispatcher(JSP_FORM).forward(req, resp);
			return;

		} catch (Exception e) {
			throw new ServletException("AdminProductServlet error", e);
		}
	}

	/* ======================================================
       LOAD DROPDOWN DATA
       ====================================================== */
	private void loadDropdowns(HttpServletRequest req) {
		req.setAttribute("categories", categoryDAO.findAll());
		req.setAttribute("brands", brandDAO.findAll());
	}

	/* ======================================================
       BUILD / VALIDATE
       ====================================================== */
	private Product buildFromRequest(HttpServletRequest req) {
		Product product = new Product();

		String title = safe(req.getParameter("title"));
		String slug = safe(req.getParameter("slug"));

		product.setTitle(title);
		product.setSlug(slug);
		product.setDescription(req.getParameter("description"));

		product.setPrice(parseBigDecimal(req.getParameter("price"), BigDecimal.ZERO));
		product.setDiscountPercent(parseInt(req.getParameter("discountPercent"), 0));
		product.setStock(parseInt(req.getParameter("stock"), 0));

		boolean active = "1".equals(req.getParameter("active"))
				|| "on".equalsIgnoreCase(req.getParameter("active"))
				|| "true".equalsIgnoreCase(req.getParameter("active"));
		product.setActive(active);

		// Category
		Integer categoryId = parseIntObj(req.getParameter("categoryId"));
		if (categoryId == null || categoryId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn danh mục.");
		}

		Category category = new Category();
		category.setId(categoryId);
		product.setCategory(category);

		// Brand
		Integer brandId = parseIntObj(req.getParameter("brandId"));
		if (brandId == null || brandId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn thương hiệu.");
		}

		Brand brand = new Brand();
		brand.setId(brandId);
		product.setBrand(brand);

		// Validate
		if (title.isBlank()) {
			throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
		}

		if (slug.isBlank()) {
			throw new IllegalArgumentException("Slug không được để trống.");
		}

		if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Giá không hợp lệ.");
		}

		if (product.getDiscountPercent() < 0 || product.getDiscountPercent() > 100) {
			throw new IllegalArgumentException("Giảm giá phải nằm trong khoảng 0 - 100%.");
		}

		if (product.getStock() < 0) {
			throw new IllegalArgumentException("Tồn kho không hợp lệ.");
		}

		return product;
	}

	// Bản safe để giữ dữ liệu khi form bị lỗi
	private Product buildFromRequestSafe(HttpServletRequest req) {
		Product product = new Product();

		product.setTitle(safe(req.getParameter("title")));
		product.setSlug(safe(req.getParameter("slug")));
		product.setDescription(req.getParameter("description"));

		product.setPrice(parseBigDecimal(req.getParameter("price"), BigDecimal.ZERO));
		product.setDiscountPercent(parseInt(req.getParameter("discountPercent"), 0));
		product.setStock(parseInt(req.getParameter("stock"), 0));

		boolean active = "1".equals(req.getParameter("active"))
				|| "on".equalsIgnoreCase(req.getParameter("active"))
				|| "true".equalsIgnoreCase(req.getParameter("active"));
		product.setActive(active);

		Integer categoryId = parseIntObj(req.getParameter("categoryId"));
		if (categoryId != null && categoryId > 0) {
			Category category = new Category();
			category.setId(categoryId);
			product.setCategory(category);
		}

		Integer brandId = parseIntObj(req.getParameter("brandId"));
		if (brandId != null && brandId > 0) {
			Brand brand = new Brand();
			brand.setId(brandId);
			product.setBrand(brand);
		}

		return product;
	}

    /* ======================================================
       UPLOAD HELPERS
       ====================================================== */

	// Lưu 1 file nếu có upload
	private String saveIfPresent(HttpServletRequest req, String partName, String folder) throws Exception {
		Part part;
		try {
			part = req.getPart(partName);
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("File upload quá lớn.");
		}

		if (part == null || part.getSize() <= 0) {
			return null;
		}

		String submitted = getSubmittedFileName(part);
		if (submitted == null || submitted.isBlank()) {
			return null;
		}

		return savePartToWebFolder(req, part, folder);
	}

	// Lưu nhiều file nếu có upload gallery
	private List<String> saveMultiIfPresent(HttpServletRequest req, String partName, String folder) throws Exception {
		Collection<Part> parts;
		try {
			parts = req.getParts();
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("File upload quá lớn.");
		}

		List<String> result = new ArrayList<>();

		for (Part part : parts) {
			if (!partName.equals(part.getName())) {
				continue;
			}

			if (part.getSize() <= 0) {
				continue;
			}

			String submitted = getSubmittedFileName(part);
			if (submitted == null || submitted.isBlank()) {
				continue;
			}

			result.add(savePartToWebFolder(req, part, folder));
		}

		return result;
	}

	// Lưu file vào thư mục thật trong webapp
	private String savePartToWebFolder(HttpServletRequest req, Part part, String folder) throws Exception {
		String submitted = getSubmittedFileName(part);
		String ext = "";

		int dot = submitted.lastIndexOf('.');
		if (dot >= 0) {
			ext = submitted.substring(dot);
		}

		// Tạo tên file ngẫu nhiên để tránh trùng
		String newName = UUID.randomUUID().toString().replace("-", "") + ext;

		String realPath = req.getServletContext().getRealPath(folder);
		if (realPath == null) {
			throw new IllegalStateException("Không lấy được realPath để lưu file.");
		}

		Path dir = Paths.get(realPath);
		Files.createDirectories(dir);

		part.write(dir.resolve(newName).toString());

		return folder + "/" + newName;
	}

	// Lấy tên file gốc từ header content-disposition
	private String getSubmittedFileName(Part part) {
		String contentDisposition = part.getHeader("content-disposition");
		if (contentDisposition == null) {
			return null;
		}

		for (String token : contentDisposition.split(";")) {
			token = token.trim();

			if (token.startsWith("filename=")) {
				String fileName = token.substring("filename=".length()).trim().replace("\"", "");
				return Paths.get(fileName).getFileName().toString();
			}
		}

		return null;
	}

    /* ======================================================
       PARSE HELPERS
       ====================================================== */

	private int parseInt(String s, int fallback) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return fallback;
		}
	}

	private Integer parseIntObj(String s) {
		try {
			if (s == null || s.isBlank()) {
				return null;
			}
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String s, BigDecimal fallback) {
		try {
			if (s == null || s.isBlank()) {
				return fallback;
			}
			return new BigDecimal(s.trim());
		} catch (Exception e) {
			return fallback;
		}
	}

	private String safe(String s) {
		return s == null ? "" : s.trim();
	}
}