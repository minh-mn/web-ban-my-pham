package com.webshop.app.controller.AdminController;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductImageDAO;
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig(
		fileSizeThreshold = 1024 * 1024,
		maxFileSize = 10 * 1024 * 1024,
		maxRequestSize = 60 * 1024 * 1024
)
@WebServlet("/admin/products")
public class AdminProductServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ProductDAO productDAO = new ProductDAO();
	private final ProductImageDAO productImageDAO = new ProductImageDAO();

	private final CategoryDAO categoryDAO = new CategoryDAO();
	private final BrandDAO brandDAO = new BrandDAO();

	/*
	 * Public URL lưu trong database.
	 * File vật lý sẽ lưu trong MyCosmeticShopUploads/product và MyCosmeticShopUploads/product/gallery.
	 */
	private static final String PRODUCT_PUBLIC_PREFIX = "/uploads/product";
	private static final String PRODUCT_GALLERY_PUBLIC_PREFIX = "/uploads/product/gallery";

	private static final String JSP_FORM = "/jsp/admin/products/product_form.jsp";
	private static final String JSP_LIST = "/jsp/admin/products/product_list.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		String action = req.getParameter("action");

		if (action == null) {
			action = "list";
		}

		switch (action) {
			case "new": {
				loadDropdowns(req);
				req.getRequestDispatcher(JSP_FORM).forward(req, resp);
				return;
			}

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

				product.setImages(productImageDAO.findByProductId(product.getId()));
				req.setAttribute("product", product);

				loadDropdowns(req);
				req.getRequestDispatcher(JSP_FORM).forward(req, resp);
				return;
			}

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
				case "create": {
					Product product = buildFromRequest(req);

					String mainImage = saveIfPresent(
							req,
							"imageMain",
							UploadConfig.PRODUCT_DIR,
							PRODUCT_PUBLIC_PREFIX
					);

					if (mainImage != null) {
						product.setImage(mainImage);
					}

					int newId = productDAO.create(product);

					List<String> gallery = saveMultiIfPresent(
							req,
							"imageGallery",
							UploadConfig.PRODUCT_GALLERY_DIR,
							PRODUCT_GALLERY_PUBLIC_PREFIX
					);

					if (!gallery.isEmpty()) {
						int order = 0;

						for (String imagePath : gallery) {
							productImageDAO.insert(newId, imagePath, order++);
						}
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				case "update": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id <= 0) {
						resp.sendRedirect(req.getContextPath() + "/admin/products");
						return;
					}

					Product product = buildFromRequest(req);
					product.setId(id);

					String existingMain = req.getParameter("existingImage");

					String mainImage = saveIfPresent(
							req,
							"imageMain",
							UploadConfig.PRODUCT_DIR,
							PRODUCT_PUBLIC_PREFIX
					);

					if (mainImage != null) {
						product.setImage(mainImage);
					} else {
						product.setImage(existingMain);
					}

					productDAO.update(product);

					List<String> gallery = saveMultiIfPresent(
							req,
							"imageGallery",
							UploadConfig.PRODUCT_GALLERY_DIR,
							PRODUCT_GALLERY_PUBLIC_PREFIX
					);

					if (!gallery.isEmpty()) {
						productImageDAO.deleteByProductId(id);

						int order = 0;

						for (String imagePath : gallery) {
							productImageDAO.insert(id, imagePath, order++);
						}
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				case "delete": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id > 0) {
						productDAO.deleteReviewsByProductId(id);
						productImageDAO.deleteByProductId(id);
						productDAO.delete(id);
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				default: {
					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}
			}

		} catch (IllegalArgumentException ex) {
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

		} catch (Exception e) {
			throw new ServletException("AdminProductServlet error", e);
		}
	}

	/* ===================== DROPDOWNS ===================== */

	private void loadDropdowns(HttpServletRequest req) {
		req.setAttribute("categories", categoryDAO.findAll());
		req.setAttribute("brands", brandDAO.findAll());
	}

	/* ===================== BUILD / VALIDATE ===================== */

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

		Integer categoryId = parseIntObj(req.getParameter("categoryId"));

		if (categoryId == null || categoryId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn danh mục.");
		}

		Category category = new Category();
		category.setId(categoryId);
		product.setCategory(category);

		Integer brandId = parseIntObj(req.getParameter("brandId"));

		if (brandId == null || brandId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn thương hiệu.");
		}

		Brand brand = new Brand();
		brand.setId(brandId);
		product.setBrand(brand);

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
			throw new IllegalArgumentException("Giảm giá phải nằm trong khoảng 0-100%.");
		}

		if (product.getStock() < 0) {
			throw new IllegalArgumentException("Tồn kho không hợp lệ.");
		}

		return product;
	}

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

	/* ===================== UPLOAD HELPERS ===================== */

	private String saveIfPresent(HttpServletRequest req, String partName, Path uploadDir, String publicPrefix)
			throws Exception {

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

		return savePartToUploadFolder(part, uploadDir, publicPrefix);
	}

	private List<String> saveMultiIfPresent(HttpServletRequest req, String partName, Path uploadDir, String publicPrefix)
			throws Exception {

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

			result.add(savePartToUploadFolder(part, uploadDir, publicPrefix));
		}

		return result;
	}

	private String savePartToUploadFolder(Part part, Path uploadDir, String publicPrefix)
			throws Exception {

		String submitted = getSubmittedFileName(part);

		if (submitted == null || submitted.isBlank()) {
			return null;
		}

		String contentType = part.getContentType();

		if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
			throw new IllegalArgumentException("File upload không hợp lệ. Chỉ chấp nhận file ảnh.");
		}

		String ext = getExtensionLower(submitted);

		if (!isAllowedImageExtension(ext)) {
			throw new IllegalArgumentException("Định dạng ảnh không hỗ trợ. Chỉ chấp nhận: png, jpg, jpeg, webp, gif.");
		}

		Path safeUploadDir = uploadDir.toAbsolutePath().normalize();
		Files.createDirectories(safeUploadDir);

		String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		Path destination = safeUploadDir.resolve(newName).normalize();

		if (!destination.startsWith(safeUploadDir)) {
			throw new IllegalArgumentException("Đường dẫn upload không hợp lệ.");
		}

		try (InputStream inputStream = part.getInputStream()) {
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		}

		return publicPrefix + "/" + newName;
	}

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

	private String getExtensionLower(String filename) {
		int dot = filename.lastIndexOf('.');

		if (dot < 0 || dot == filename.length() - 1) {
			return "";
		}

		return filename.substring(dot + 1).toLowerCase();
	}

	private boolean isAllowedImageExtension(String ext) {
		return "png".equals(ext)
				|| "jpg".equals(ext)
				|| "jpeg".equals(ext)
				|| "webp".equals(ext)
				|| "gif".equals(ext);
	}

	/* ===================== PARSE HELPERS ===================== */

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