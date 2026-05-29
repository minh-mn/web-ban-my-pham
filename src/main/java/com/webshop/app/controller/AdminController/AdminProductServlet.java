package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.BrandDAO;
import com.webshop.app.dao.CategoryDAO;
import com.webshop.app.dao.ProductDAO;
import com.webshop.app.dao.ProductImageDAO;
import com.webshop.app.dao.ProductMediaDAO;
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;

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
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig(
		fileSizeThreshold = 1024 * 1024,
		maxFileSize = 50 * 1024 * 1024,
		maxRequestSize = 220 * 1024 * 1024
)
@WebServlet("/admin/products")
public class AdminProductServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ProductDAO productDAO = new ProductDAO();
	private final ProductImageDAO productImageDAO = new ProductImageDAO();

	// Issue 123: DAO quản lý media chi tiết sản phẩm: ảnh/video
	private final ProductMediaDAO productMediaDAO = new ProductMediaDAO();

	private final CategoryDAO categoryDAO = new CategoryDAO();
	private final BrandDAO brandDAO = new BrandDAO();

	private static final String JSP_FORM = "/jsp/admin/products/product_form.jsp";
	private static final String JSP_LIST = "/jsp/admin/products/product_list.jsp";

	@Override
	public void init() throws ServletException {
		super.init();

		/*
		 * Đảm bảo các thư mục upload tồn tại:
		 * - MyCosmeticShopUploads/product
		 * - MyCosmeticShopUploads/product/gallery
		 * - MyCosmeticShopUploads/product/media
		 */
		UploadConfig.ensureUploadDirectories();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		String action = req.getParameter("action");

		if (action == null || action.isBlank()) {
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

				// Issue 123: load media chi tiết ảnh/video để hiển thị ở form sửa
				req.setAttribute("productMediaList", productMediaDAO.findByProductId(product.getId()));

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

		if (action == null || action.isBlank()) {
			action = "create";
		}

		try {
			switch (action) {
				case "create": {
					Product product = buildFromRequest(req);

					String mainImage = saveIfPresent(req, "imageMain", false);

					if (mainImage != null) {
						product.setImage(mainImage);
					}

					int newId = productDAO.create(product);

					/*
					 * Gallery ảnh cũ của sản phẩm.
					 * Input name bên JSP: imageGallery
					 */
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", true);

					if (!gallery.isEmpty()) {
						int order = 0;

						for (String imagePath : gallery) {
							productImageDAO.insert(newId, imagePath, order++);
						}
					}

					/*
					 * Issue 123:
					 * Media chi tiết sản phẩm gồm nhiều ảnh/video.
					 * Input name bên JSP: productMedia
					 */
					List<MediaUploadResult> mediaFiles = saveProductMediaIfPresent(req, "productMedia");

					if (!mediaFiles.isEmpty()) {
						int order = 0;

						for (MediaUploadResult media : mediaFiles) {
							productMediaDAO.insert(newId, media.url, media.mediaType, order++);
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

					/*
					 * Nếu không upload ảnh đại diện mới thì giữ nguyên ảnh cũ.
					 * Không tự normalize ảnh cũ để tránh đổi DB sang /uploads/ khi file thật chưa được copy.
					 */
					String existingMain = req.getParameter("existingImage");

					String mainImage = saveIfPresent(req, "imageMain", false);

					if (mainImage != null) {
						product.setImage(mainImage);
					} else {
						product.setImage(existingMain);
					}

					productDAO.update(product);

					/*
					 * Gallery ảnh sản phẩm.
					 * Bản cũ khi upload gallery mới sẽ xóa toàn bộ gallery cũ.
					 * Bản này chuyển sang append để đúng yêu cầu "thêm được nhiều ảnh hơn".
					 */
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", true);

					if (!gallery.isEmpty()) {
						int order = productImageDAO.findByProductId(id).size();

						for (String imagePath : gallery) {
							productImageDAO.insert(id, imagePath, order++);
						}
					}

					/*
					 * Issue 123:
					 * Xóa media chi tiết đã chọn, nếu JSP có checkbox name="deleteMediaIds".
					 */
					deleteSelectedProductMedia(req, id);

					/*
					 * Issue 123:
					 * Thêm mới nhiều ảnh/video chi tiết sản phẩm.
					 */
					List<MediaUploadResult> mediaFiles = saveProductMediaIfPresent(req, "productMedia");

					if (!mediaFiles.isEmpty()) {
						int order = productMediaDAO.findByProductId(id).size();

						for (MediaUploadResult media : mediaFiles) {
							productMediaDAO.insert(id, media.url, media.mediaType, order++);
						}
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				case "delete": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id > 0) {
						ProductDAO.DeleteMode deleteMode = productDAO.deleteOrDeactivateSafely(id);

						if (deleteMode == ProductDAO.DeleteMode.SOFT_DELETED) {
							resp.sendRedirect(req.getContextPath() + "/admin/products?delete=soft");
							return;
						}

						if (deleteMode == ProductDAO.DeleteMode.HARD_DELETED) {
							resp.sendRedirect(req.getContextPath() + "/admin/products?delete=hard");
							return;
						}

						resp.sendRedirect(req.getContextPath() + "/admin/products?delete=not_found");
						return;
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
					req.setAttribute("productMediaList", productMediaDAO.findByProductId(id));
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

		/*
		 * Issue 123:
		 * description có thể nhập HTML đơn giản từ form để mô tả chi tiết hơn.
		 * Phần hiển thị ngoài detail.jsp sẽ quyết định escape hay render HTML.
		 */
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

	/* ===================== UPLOAD IMAGE HELPERS ===================== */

	private String saveIfPresent(HttpServletRequest req, String partName, boolean gallery)
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

		return saveImagePartToUploadFolder(part, gallery);
	}

	private List<String> saveMultiIfPresent(HttpServletRequest req, String partName, boolean gallery)
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

			result.add(saveImagePartToUploadFolder(part, gallery));
		}

		return result;
	}

	private String saveImagePartToUploadFolder(Part part, boolean gallery)
			throws Exception {

		String submitted = getSubmittedFileName(part);

		if (submitted == null || submitted.isBlank()) {
			return null;
		}

		String contentType = part.getContentType();

		if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			throw new IllegalArgumentException("File upload không hợp lệ. Chỉ chấp nhận file ảnh.");
		}

		String ext = getExtensionLower(submitted);

		if (!isAllowedImageExtension(ext)) {
			throw new IllegalArgumentException("Định dạng ảnh không hỗ trợ. Chỉ chấp nhận: png, jpg, jpeg, webp, gif.");
		}

		Path uploadDir = gallery
				? UploadConfig.PRODUCT_GALLERY_DIR.toAbsolutePath().normalize()
				: UploadConfig.PRODUCT_DIR.toAbsolutePath().normalize();

		Files.createDirectories(uploadDir);

		String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

		Path destination = gallery
				? UploadConfig.resolveProductGalleryFile(newName).toAbsolutePath().normalize()
				: UploadConfig.resolveProductFile(newName).toAbsolutePath().normalize();

		if (!destination.startsWith(uploadDir)) {
			throw new IllegalArgumentException("Đường dẫn upload không hợp lệ.");
		}

		try (InputStream inputStream = part.getInputStream()) {
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		}

		/*
		 * Database chỉ lưu URL public:
		 * - /uploads/product/{fileName}
		 * - /uploads/product/gallery/{fileName}
		 */
		if (gallery) {
			return UploadConfig.toProductGalleryUrl(newName);
		}

		return UploadConfig.toProductUrl(newName);
	}

	/* ===================== ISSUE 123 - PRODUCT MEDIA HELPERS ===================== */

	private List<MediaUploadResult> saveProductMediaIfPresent(HttpServletRequest req, String partName)
			throws Exception {

		Collection<Part> parts;

		try {
			parts = req.getParts();
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("File upload quá lớn.");
		}

		List<MediaUploadResult> result = new ArrayList<>();

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

			result.add(saveProductMediaPartToUploadFolder(part));
		}

		return result;
	}

	private MediaUploadResult saveProductMediaPartToUploadFolder(Part part)
			throws Exception {

		String submitted = getSubmittedFileName(part);

		if (submitted == null || submitted.isBlank()) {
			return null;
		}

		String ext = getExtensionLower(submitted);

		boolean imageExt = isAllowedImageExtension(ext);
		boolean videoExt = isAllowedVideoExtension(ext);

		if (!imageExt && !videoExt) {
			throw new IllegalArgumentException(
					"Định dạng media không hỗ trợ. Chỉ chấp nhận ảnh: png, jpg, jpeg, webp, gif hoặc video: mp4, webm, mov, m4v."
			);
		}

		String contentType = part.getContentType();
		String lowerContentType = contentType == null
				? ""
				: contentType.toLowerCase(Locale.ROOT);

		String mediaType;

		if (imageExt) {
			if (lowerContentType.startsWith("video/")) {
				throw new IllegalArgumentException("File media không hợp lệ: phần mở rộng ảnh nhưng nội dung là video.");
			}

			mediaType = "IMAGE";
		} else {
			if (lowerContentType.startsWith("image/")) {
				throw new IllegalArgumentException("File media không hợp lệ: phần mở rộng video nhưng nội dung là ảnh.");
			}

			mediaType = "VIDEO";
		}

		Path uploadDir = UploadConfig.PRODUCT_MEDIA_DIR.toAbsolutePath().normalize();
		Files.createDirectories(uploadDir);

		String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		Path destination = UploadConfig.resolveProductMediaFile(newName).toAbsolutePath().normalize();

		if (!destination.startsWith(uploadDir)) {
			throw new IllegalArgumentException("Đường dẫn upload media không hợp lệ.");
		}

		try (InputStream inputStream = part.getInputStream()) {
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		}

		/*
		 * Database chỉ lưu URL public:
		 * - /uploads/product/media/{fileName}
		 */
		String publicUrl = UploadConfig.toProductMediaUrl(newName);

		return new MediaUploadResult(publicUrl, mediaType);
	}

	private void deleteSelectedProductMedia(HttpServletRequest req, int productId) {
		String[] deleteIds = req.getParameterValues("deleteMediaIds");

		if (deleteIds == null || deleteIds.length == 0) {
			return;
		}

		for (String rawId : deleteIds) {
			int mediaId = parseInt(rawId, -1);

			if (mediaId > 0) {
				productMediaDAO.deleteByIdAndProductId(mediaId, productId);
			}
		}
	}

	private static final class MediaUploadResult {
		private final String url;
		private final String mediaType;

		private MediaUploadResult(String url, String mediaType) {
			this.url = url;
			this.mediaType = mediaType;
		}
	}

	/* ===================== FILE HELPERS ===================== */

	private String getSubmittedFileName(Part part) {
		if (part == null) {
			return null;
		}

		String submitted = part.getSubmittedFileName();

		if (submitted == null || submitted.isBlank()) {
			return null;
		}

		return Paths.get(submitted)
				.getFileName()
				.toString();
	}

	private String getExtensionLower(String filename) {
		if (filename == null || filename.isBlank()) {
			return "";
		}

		int dot = filename.lastIndexOf('.');

		if (dot < 0 || dot == filename.length() - 1) {
			return "";
		}

		return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private boolean isAllowedImageExtension(String ext) {
		return "png".equals(ext)
				|| "jpg".equals(ext)
				|| "jpeg".equals(ext)
				|| "webp".equals(ext)
				|| "gif".equals(ext);
	}

	private boolean isAllowedVideoExtension(String ext) {
		return "mp4".equals(ext)
				|| "webm".equals(ext)
				|| "mov".equals(ext)
				|| "m4v".equals(ext);
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