package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.*;
import com.webshop.app.model.Brand;
import com.webshop.app.model.Category;
import com.webshop.app.model.Product;
import com.webshop.app.model.ProductVariant;
import com.webshop.app.service.AuditLogService;
import com.webshop.app.utils.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

	private final NotificationDAO notificationDAO = new NotificationDAO();
	private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

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

				req.setAttribute("variants", productVariantDAO.findActiveByProductId(id));

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

		/*
		 * Dùng để rollback file mới nếu quá trình create/update bị lỗi sau khi upload.
		 * File cũ chỉ bị xóa sau khi SQL update/delete thành công.
		 */
		List<String> uploadedFileUrlsForRollback = new ArrayList<>();

		try {
			switch (action) {
				case "create": {
					Product product = buildFromRequest(req);

					String mainImage = saveIfPresent(req, "imageMain", false);

					if (mainImage != null) {
						product.setImage(mainImage);
						uploadedFileUrlsForRollback.add(mainImage);
					}

					int newId = productDAO.create(product);

					// LƯU BIẾN THỂ CHO SẢN PHẨM MỚI
					saveVariants(req, newId);

					/*
					 * Gallery ảnh của sản phẩm.
					 * Input name bên JSP: imageGallery
					 */
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", true);

					if (!gallery.isEmpty()) {
						uploadedFileUrlsForRollback.addAll(gallery);

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
							uploadedFileUrlsForRollback.add(media.url);
							productMediaDAO.insert(newId, media.url, media.mediaType, order++);
						}
					}

					uploadedFileUrlsForRollback.clear();

					AuditLogService.logCreate(
							req,
							"PRODUCT",
							"Product",
							newId,
							product.getTitle(),
							"Đã thêm sản phẩm mới: " + product.getTitle(),
							AuditLogService.changes(
									"Tên: " + product.getTitle(),
									"Giá: " + AuditLogService.formatMoney(product.getPrice()),
									"Giảm giá: " + product.getDiscountPercent() + "%",
									"Tồn kho: " + product.getStock(),
									"Trạng thái: " + (product.isActive() ? "Đang bán" : "Ẩn")
							)
					);

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				case "update": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id <= 0) {
						resp.sendRedirect(req.getContextPath() + "/admin/products");
						return;
					}

					Product oldProduct = productDAO.findByIdAdmin(id);
					String oldMainImage = oldProduct == null ? null : oldProduct.getImage();

					Product product = buildFromRequest(req);
					product.setId(id);

					/*
					 * Nếu không upload ảnh đại diện mới thì giữ nguyên ảnh cũ.
					 * Ưu tiên ảnh từ DB để tránh existingImage bị sửa trên form.
					 */
					String existingMain = oldMainImage;
					if (existingMain == null || existingMain.isBlank()) {
						existingMain = req.getParameter("existingImage");
					}

					String mainImage = saveIfPresent(req, "imageMain", false);

					if (mainImage != null) {
						product.setImage(mainImage);
						uploadedFileUrlsForRollback.add(mainImage);
					} else {
						product.setImage(existingMain);
					}

					/*
					 * Update SQL trước.
					 * Nếu update lỗi, ảnh mới sẽ rollback, ảnh cũ không bị xóa.
					 */
					boolean isUpdated = productDAO.update(product);

					saveVariants(req, id);

					// "BẪY" GỬI THÔNG BÁO GIẢM GIÁ (Thêm mới)
					if (isUpdated) {
						// Kiểm tra điều kiện giảm giá (Thay getDiscountPercent() bằng getter thực tế của bạn)
						if (product.getDiscountPercent() > 0) {

							// Dùng Thread để chạy ngầm, không làm treo màn hình Admin
							final int pId = product.getId();
							final String pName = product.getTitle();

							new Thread(() -> {
								notificationDAO.sendWishlistDiscountNotification(pId, pName);
							}).start();
						}
					}

					if (isUpdated && oldProduct != null) {
						String oldValues = AuditLogService.changes(
								AuditLogService.change("Tên", oldProduct.getTitle(), product.getTitle()),
								AuditLogService.moneyChange("Giá", oldProduct.getPrice(), product.getPrice()),
								AuditLogService.change("Giảm giá", oldProduct.getDiscountPercent() + "%", product.getDiscountPercent() + "%"),
								AuditLogService.change("Tồn kho", oldProduct.getStock(), product.getStock()),
								AuditLogService.change("Trạng thái", oldProduct.isActive() ? "Đang bán" : "Ẩn", product.isActive() ? "Đang bán" : "Ẩn")
						);

						if (oldValues != null) {
							AuditLogService.logUpdate(
									req,
									"PRODUCT",
									"Product",
									id,
									product.getTitle(),
									"Đã cập nhật sản phẩm: " + product.getTitle(),
									oldValues,
									AuditLogService.changes(
											"Tên hiện tại: " + product.getTitle(),
											"Giá hiện tại: " + AuditLogService.formatMoney(product.getPrice()),
											"Giảm giá hiện tại: " + product.getDiscountPercent() + "%",
											"Tồn kho hiện tại: " + product.getStock(),
											"Trạng thái hiện tại: " + (product.isActive() ? "Đang bán" : "Ẩn")
									)
							);
						}
					}

					/*
					 * Nếu đổi ảnh đại diện mới và SQL update thành công,
					 * xóa file ảnh đại diện cũ trong MyCosmeticShopUploads/product.
					 */
					if (mainImage != null && isChangedLocalUploadFile(oldMainImage, mainImage, UploadConfig.PRODUCT_URL_PREFIX)) {
						UploadConfig.deleteProductFileByUrl(oldMainImage);
					}

					/*
					 * Xóa gallery ảnh được tick trong form.
					 * Hỗ trợ 2 tên parameter để tránh lệch JSP:
					 * - deleteImageIds
					 * - deleteGalleryImageIds
					 */
					deleteSelectedProductGalleryImages(req, id);

					/*
					 * Issue 123:
					 * Xóa media chi tiết ảnh/video được tick trong form.
					 */
					deleteSelectedProductMedia(req, id);

					/*
					 * Gallery ảnh sản phẩm.
					 * Khi sửa sản phẩm, ảnh mới sẽ được append vào gallery hiện có.
					 */
					List<String> gallery = saveMultiIfPresent(req, "imageGallery", true);

					if (!gallery.isEmpty()) {
						uploadedFileUrlsForRollback.addAll(gallery);

						int order = productImageDAO.findByProductId(id).size();

						for (String imagePath : gallery) {
							productImageDAO.insert(id, imagePath, order++);
						}
					}

					/*
					 * Issue 123:
					 * Thêm mới nhiều ảnh/video chi tiết sản phẩm.
					 */
					List<MediaUploadResult> mediaFiles = saveProductMediaIfPresent(req, "productMedia");

					if (!mediaFiles.isEmpty()) {
						int order = productMediaDAO.findByProductId(id).size();

						for (MediaUploadResult media : mediaFiles) {
							uploadedFileUrlsForRollback.add(media.url);
							productMediaDAO.insert(id, media.url, media.mediaType, order++);
						}
					}

					uploadedFileUrlsForRollback.clear();

					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				case "hide": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id <= 0) {
						resp.sendRedirect(req.getContextPath() + "/admin/products");
						return;
					}

					Product oldProduct = productDAO.findByIdAdmin(id);

					if (oldProduct == null) {
						resp.sendRedirect(req.getContextPath() + "/admin/products?status=not_found");
						return;
					}

					boolean updated = productDAO.updateActiveStatus(id, false);

					if (updated) {
						AuditLogService.logSoftDelete(
								req,
								"PRODUCT",
								"Product",
								id,
								oldProduct.getTitle(),
								"Đã ẩn sản phẩm từ danh sách quản trị.",
								"Trạng thái: " + (oldProduct.isActive() ? "Đang bán" : "Ẩn"),
								"Trạng thái: Ẩn"
						);
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products?status=hidden");
					return;
				}

				case "show": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id <= 0) {
						resp.sendRedirect(req.getContextPath() + "/admin/products");
						return;
					}

					Product oldProduct = productDAO.findByIdAdmin(id);

					if (oldProduct == null) {
						resp.sendRedirect(req.getContextPath() + "/admin/products?status=not_found");
						return;
					}

					boolean updated = productDAO.updateActiveStatus(id, true);

					if (updated) {
						AuditLogService.logStatusChange(
								req,
								"PRODUCT",
								"Product",
								id,
								oldProduct.getTitle(),
								"Đã mở khóa sản phẩm và cho phép hiển thị lại.",
								"Trạng thái: " + (oldProduct.isActive() ? "Đang bán" : "Ẩn"),
								"Trạng thái: Đang bán"
						);
					}

					resp.sendRedirect(req.getContextPath() + "/admin/products?status=shown");
					return;
				}

				case "delete": {
					int id = parseInt(req.getParameter("id"), -1);

					if (id > 0) {
						/*
						 * Lấy danh sách file trước khi xóa SQL.
						 * Vì hard delete trong ProductDAO sẽ xóa các record liên quan.
						 */
						Product oldProduct = productDAO.findByIdAdmin(id);
						String oldMainImage = oldProduct == null ? null : oldProduct.getImage();

						List<String> oldGalleryUrls = findProductGalleryImageUrls(id);
						List<String> oldMediaUrls = findProductMediaUrls(id);

						ProductDAO.DeleteMode deleteMode = productDAO.deleteOrDeactivateSafely(id);

						if (deleteMode == ProductDAO.DeleteMode.SOFT_DELETED) {
							/*
							 * Product đã có trong đơn hàng:
							 * Chỉ ẩn sản phẩm, không xóa file để bảo toàn dữ liệu lịch sử.
							 */
							AuditLogService.logSoftDelete(
									req,
									"PRODUCT",
									"Product",
									id,
									oldProduct == null ? null : oldProduct.getTitle(),
									"Đã ẩn sản phẩm vì sản phẩm đã phát sinh dữ liệu đơn hàng.",
									oldProduct == null ? null : "Trạng thái: " + (oldProduct.isActive() ? "Đang bán" : "Ẩn"),
									"Trạng thái: Ẩn"
							);

							resp.sendRedirect(req.getContextPath() + "/admin/products?delete=soft");
							return;
						}

						if (deleteMode == ProductDAO.DeleteMode.HARD_DELETED) {
							/*
							 * Product chưa có đơn hàng:
							 * SQL đã xóa thành công thì xóa tiếp file vật lý.
							 */
							AuditLogService.logDelete(
									req,
									"PRODUCT",
									"Product",
									id,
									oldProduct == null ? null : oldProduct.getTitle(),
									"Đã xóa sản phẩm khỏi hệ thống.",
									oldProduct == null ? null : AuditLogService.changes(
											"Tên: " + oldProduct.getTitle(),
											"Giá: " + AuditLogService.formatMoney(oldProduct.getPrice()),
											"Tồn kho: " + oldProduct.getStock()
									)
							);

							deleteProductPhysicalFiles(oldMainImage, oldGalleryUrls, oldMediaUrls);

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
			rollbackUploadedFiles(uploadedFileUrlsForRollback);

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
			rollbackUploadedFiles(uploadedFileUrlsForRollback);
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

		String newName = Paths.get(submitted).getFileName().toString();

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

		String newName = Paths.get(submitted).getFileName().toString();		Path destination = UploadConfig.resolveProductMediaFile(newName).toAbsolutePath().normalize();

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

	/*
	 * Xóa từng ảnh gallery được tick trong form.
	 * Xử lý đủ:
	 * - Xóa dòng store_productimage
	 * - Xóa file vật lý trong MyCosmeticShopUploads/product/gallery
	 */
	private void deleteSelectedProductGalleryImages(HttpServletRequest req, int productId) {
		Set<Integer> imageIds = getDeleteIdSet(req, "deleteImageIds", "deleteGalleryImageIds");

		if (imageIds.isEmpty()) {
			return;
		}

		for (Integer imageId : imageIds) {
			if (imageId == null || imageId <= 0) {
				continue;
			}

			String imageUrl = findProductGalleryImageUrlById(productId, imageId);
			boolean deletedSql = deleteProductGalleryImageById(productId, imageId);

			if (deletedSql) {
				UploadConfig.deleteProductGalleryFileByUrl(imageUrl);
			}
		}
	}

	/*
	 * Xóa từng media chi tiết ảnh/video được tick trong form.
	 * Xử lý đủ:
	 * - Xóa dòng store_productmedia
	 * - Xóa file vật lý trong MyCosmeticShopUploads/product/media
	 */
	private void deleteSelectedProductMedia(HttpServletRequest req, int productId) {
		Set<Integer> mediaIds = getDeleteIdSet(req, "deleteMediaIds");

		if (mediaIds.isEmpty()) {
			return;
		}

		for (Integer mediaId : mediaIds) {
			if (mediaId == null || mediaId <= 0) {
				continue;
			}

			String mediaUrl = findProductMediaUrlById(productId, mediaId);
			boolean deletedSql = deleteProductMediaById(productId, mediaId);

			if (deletedSql) {
				UploadConfig.deleteProductMediaFileByUrl(mediaUrl);
			}
		}
	}

	private Set<Integer> getDeleteIdSet(HttpServletRequest req, String... parameterNames) {
		Set<Integer> ids = new LinkedHashSet<>();

		if (parameterNames == null || parameterNames.length == 0) {
			return ids;
		}

		for (String parameterName : parameterNames) {
			String[] values = req.getParameterValues(parameterName);

			if (values == null || values.length == 0) {
				continue;
			}

			for (String rawValue : values) {
				int id = parseInt(rawValue, -1);

				if (id > 0) {
					ids.add(id);
				}
			}
		}

		return ids;
	}

	private static final class MediaUploadResult {
		private final String url;
		private final String mediaType;

		private MediaUploadResult(String url, String mediaType) {
			this.url = url;
			this.mediaType = mediaType;
		}
	}

	/* ===================== PRODUCT FILE DELETE HELPERS ===================== */

	private void deleteProductPhysicalFiles(String mainImageUrl,
	                                        List<String> galleryUrls,
	                                        List<String> mediaUrls) {

		UploadConfig.deleteProductFileByUrl(mainImageUrl);

		if (galleryUrls != null) {
			for (String imageUrl : galleryUrls) {
				UploadConfig.deleteProductGalleryFileByUrl(imageUrl);
			}
		}

		if (mediaUrls != null) {
			for (String mediaUrl : mediaUrls) {
				UploadConfig.deleteProductMediaFileByUrl(mediaUrl);
			}
		}
	}

	private void rollbackUploadedFiles(List<String> uploadedFileUrls) {
		if (uploadedFileUrls == null || uploadedFileUrls.isEmpty()) {
			return;
		}

		for (String fileUrl : uploadedFileUrls) {
			UploadConfig.deleteUploadFileByUrl(fileUrl);
		}

		uploadedFileUrls.clear();
	}

	private boolean isChangedLocalUploadFile(String oldUrl, String newUrl, String expectedPrefix) {
		String oldValue = safe(oldUrl);
		String newValue = safe(newUrl);

		if (oldValue.isBlank()) {
			return false;
		}

		if (!oldValue.startsWith(expectedPrefix)) {
			return false;
		}

		return !oldValue.equals(newValue);
	}

	/* ===================== PRODUCT IMAGE / MEDIA SQL HELPERS ===================== */

	private List<String> findProductGalleryImageUrls(int productId) {
		List<String> urls = new ArrayList<>();

		String sql =
				"SELECT image " +
						"FROM store_productimage " +
						"WHERE product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					urls.add(rs.getString("image"));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.findProductGalleryImageUrls error", e);
		}

		return urls;
	}

	private String findProductGalleryImageUrlById(int productId, int imageId) {
		String sql =
				"SELECT image " +
						"FROM store_productimage " +
						"WHERE id = ? AND product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, imageId);
			ps.setInt(2, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("image");
				}
			}

			return null;

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.findProductGalleryImageUrlById error", e);
		}
	}

	private boolean deleteProductGalleryImageById(int productId, int imageId) {
		String sql =
				"DELETE FROM store_productimage " +
						"WHERE id = ? AND product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, imageId);
			ps.setInt(2, productId);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.deleteProductGalleryImageById error", e);
		}
	}

	private List<String> findProductMediaUrls(int productId) {
		List<String> urls = new ArrayList<>();

		String sql =
				"SELECT media_url " +
						"FROM store_productmedia " +
						"WHERE product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, productId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					urls.add(rs.getString("media_url"));
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.findProductMediaUrls error", e);
		}

		return urls;
	}

	private String findProductMediaUrlById(int productId, int mediaId) {
		String sql =
				"SELECT media_url " +
						"FROM store_productmedia " +
						"WHERE id = ? AND product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, mediaId);
			ps.setInt(2, productId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("media_url");
				}
			}

			return null;

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.findProductMediaUrlById error", e);
		}
	}

	private boolean deleteProductMediaById(int productId, int mediaId) {
		String sql =
				"DELETE FROM store_productmedia " +
						"WHERE id = ? AND product_id = ?";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, mediaId);
			ps.setInt(2, productId);

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new RuntimeException("AdminProductServlet.deleteProductMediaById error", e);
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

	private String getArrayValue(String[] values, int index) {
		if (values == null || index < 0 || index >= values.length || values[index] == null) {
			return "";
		}

		return values[index].trim();
	}

	private String firstNonBlank(String first, String second) {
		if (first != null && !first.trim().isEmpty()) {
			return first.trim();
		}

		return second == null ? "" : second.trim();
	}

	private void saveVariants(HttpServletRequest req, int productId) {
		// 1. Xóa các biến thể cũ để cập nhật lại danh sách mới
		productVariantDAO.deleteByProductId(productId);

		// 2. Lấy dữ liệu từ Request
		String[] skus = req.getParameterValues("v_sku[]");
		String[] sizes = req.getParameterValues("v_size[]");
		String[] colors = req.getParameterValues("v_color[]");
		String[] legacyTypes = req.getParameterValues("v_type[]");
		String[] prices = req.getParameterValues("v_price[]");
		String[] stocks = req.getParameterValues("v_stock[]");
		String[] minStocks = req.getParameterValues("v_min_stock[]");

		// 3. Insert biến thể mới
		if (sizes != null && sizes.length > 0) {
			for (int i = 0; i < sizes.length; i++) {
				String sku = getArrayValue(skus, i);
				String size = getArrayValue(sizes, i);
				String color = firstNonBlank(getArrayValue(colors, i), getArrayValue(legacyTypes, i));
				String price = getArrayValue(prices, i);
				String stock = getArrayValue(stocks, i);
				String minStock = getArrayValue(minStocks, i);

				// Bỏ qua nếu SKU, size và màu đều bị người dùng để trống
				if (!sku.isBlank() || !size.isBlank() || !color.isBlank()) {
					ProductVariant v = new ProductVariant();
					v.setProductId(productId);
					v.setSku(sku);
					v.setSize(size);
					v.setColor(color);
					v.setType(color);

					// Parse an toàn để tránh lỗi NumberFormatException nếu ô bị bỏ trống
					v.setExtraPrice(parseBigDecimal(price, BigDecimal.ZERO));
					v.setStock(parseInt(stock, 0));
					v.setMinStock(parseInt(minStock, ProductVariant.DEFAULT_MIN_STOCK));

					productVariantDAO.insert(v);
				}
			}
		}
	}
}
