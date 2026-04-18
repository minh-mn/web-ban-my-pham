package com.webshop.app.controller.AdminController;

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

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024)
@WebServlet("/admin/products")
public class AdminProductServlet extends HttpServlet {

	private final ProductDAO productDAO = new ProductDAO();
	private final ProductImageDAO productImageDAO = new ProductImageDAO();

	// ✅ dropdown sources
	private final CategoryDAO categoryDAO = new CategoryDAO();
	private final BrandDAO brandDAO = new BrandDAO();

	private static final String MAIN_DIR = "/assets/images/products";
	private static final String GALLERY_DIR = "/assets/images/products/gallery";

	private static final String JSP_FORM = "/jsp/admin/products/product_form.jsp";
	private static final String JSP_LIST = "/jsp/admin/products/product_list.jsp";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		String action = req.getParameter("action");
		if (action == null)
			action = "list";

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

			Product p = productDAO.findByIdAdmin(id);
			if (p == null) {
				resp.sendRedirect(req.getContextPath() + "/admin/products");
				return;
			}

			p.setImages(productImageDAO.findByProductId(p.getId()));
			req.setAttribute("product", p);

			loadDropdowns(req);
			req.getRequestDispatcher(JSP_FORM).forward(req, resp);
			return;
		}

		default: { // list
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setCharacterEncoding("UTF-8");

		String action = req.getParameter("action");
		if (action == null)
			action = "create";

		try {
			switch (action) {

			case "create": {
				Product p = buildFromRequest(req);

				String mainImage = saveIfPresent(req, "imageMain", MAIN_DIR);
				if (mainImage != null)
					p.setImage(mainImage);

				int newId = productDAO.create(p);

				List<String> gallery = saveMultiIfPresent(req, "imageGallery", GALLERY_DIR);
				if (!gallery.isEmpty()) {
					int order = 0;
					for (String imgPath : gallery) {
						productImageDAO.insert(newId, imgPath, order++);
					}
				}

				// ✅ tạo xong -> về list
				resp.sendRedirect(req.getContextPath() + "/admin/products");
				return;
			}

			case "update": {
				int id = parseInt(req.getParameter("id"), -1);
				if (id <= 0) {
					resp.sendRedirect(req.getContextPath() + "/admin/products");
					return;
				}

				Product p = buildFromRequest(req);
				p.setId(id);

				String existingMain = req.getParameter("existingImage");
				String mainImage = saveIfPresent(req, "imageMain", MAIN_DIR);
				if (mainImage != null)
					p.setImage(mainImage);
				else
					p.setImage(existingMain);

				productDAO.update(p);

				List<String> gallery = saveMultiIfPresent(req, "imageGallery", GALLERY_DIR);
				if (!gallery.isEmpty()) {
					productImageDAO.deleteByProductId(id);
					int order = 0;
					for (String imgPath : gallery) {
						productImageDAO.insert(id, imgPath, order++);
					}
				}

				// ✅ lưu thay đổi -> về list
				resp.sendRedirect(req.getContextPath() + "/admin/products");
				return;
			}

			case "delete": {
				int id = parseInt(req.getParameter("id"), -1);
				if (id > 0) {

					// ✅ xóa review trước (tránh lỗi FK)
					productDAO.deleteReviewsByProductId(id);

					// ✅ xóa gallery ảnh (DB)
					productImageDAO.deleteByProductId(id);

					// ✅ xóa product (DB)
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
			req.setAttribute("error", ex.getMessage());
			loadDropdowns(req);

			Product p = buildFromRequestSafe(req);

			if ("update".equalsIgnoreCase(action)) {
				int id = parseInt(req.getParameter("id"), 0);
				p.setId(id);
				p.setImage(req.getParameter("existingImage"));
				if (id > 0) {
					p.setImages(productImageDAO.findByProductId(id));
				}
			}

			req.setAttribute("product", p);
			req.getRequestDispatcher(JSP_FORM).forward(req, resp);
			return;

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
		Product p = new Product();

		String title = safe(req.getParameter("title"));
		String slug = safe(req.getParameter("slug"));

		p.setTitle(title);
		p.setSlug(slug);
		p.setDescription(req.getParameter("description"));

		p.setPrice(parseBigDecimal(req.getParameter("price"), BigDecimal.ZERO));
		p.setDiscountPercent(parseInt(req.getParameter("discountPercent"), 0));
		p.setStock(parseInt(req.getParameter("stock"), 0));

		boolean active = "1".equals(req.getParameter("active")) || "on".equalsIgnoreCase(req.getParameter("active"))
				|| "true".equalsIgnoreCase(req.getParameter("active"));
		p.setActive(active);

		Integer categoryId = parseIntObj(req.getParameter("categoryId"));
		if (categoryId == null || categoryId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn danh mục.");
		}
		Category c = new Category();
		c.setId(categoryId);
		p.setCategory(c);

		Integer brandId = parseIntObj(req.getParameter("brandId"));
		if (brandId == null || brandId <= 0) {
			throw new IllegalArgumentException("Vui lòng chọn thương hiệu.");
		}
		Brand b = new Brand();
		b.setId(brandId);
		p.setBrand(b);

		if (title.isBlank())
			throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
		if (slug.isBlank())
			throw new IllegalArgumentException("Slug không được để trống.");
		if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0)
			throw new IllegalArgumentException("Giá không hợp lệ.");
		if (p.getDiscountPercent() < 0 || p.getDiscountPercent() > 100)
			throw new IllegalArgumentException("Giảm giá phải nằm trong khoảng 0-100%.");
		if (p.getStock() < 0)
			throw new IllegalArgumentException("Tồn kho không hợp lệ.");

		return p;
	}

	private Product buildFromRequestSafe(HttpServletRequest req) {
		Product p = new Product();

		p.setTitle(safe(req.getParameter("title")));
		p.setSlug(safe(req.getParameter("slug")));
		p.setDescription(req.getParameter("description"));

		p.setPrice(parseBigDecimal(req.getParameter("price"), BigDecimal.ZERO));
		p.setDiscountPercent(parseInt(req.getParameter("discountPercent"), 0));
		p.setStock(parseInt(req.getParameter("stock"), 0));

		boolean active = "1".equals(req.getParameter("active")) || "on".equalsIgnoreCase(req.getParameter("active"))
				|| "true".equalsIgnoreCase(req.getParameter("active"));
		p.setActive(active);

		Integer categoryId = parseIntObj(req.getParameter("categoryId"));
		if (categoryId != null && categoryId > 0) {
			Category c = new Category();
			c.setId(categoryId);
			p.setCategory(c);
		}

		Integer brandId = parseIntObj(req.getParameter("brandId"));
		if (brandId != null && brandId > 0) {
			Brand b = new Brand();
			b.setId(brandId);
			p.setBrand(b);
		}

		return p;
	}

	/* ===================== UPLOAD HELPERS ===================== */

	private String saveIfPresent(HttpServletRequest req, String partName, String folder) throws Exception {
		Part part;
		try {
			part = req.getPart(partName);
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("File upload quá lớn.");
		}
		if (part == null || part.getSize() <= 0)
			return null;

		String submitted = getSubmittedFileName(part);
		if (submitted == null || submitted.isBlank())
			return null;

		return savePartToWebFolder(req, part, folder);
	}

	private List<String> saveMultiIfPresent(HttpServletRequest req, String partName, String folder) throws Exception {
		Collection<Part> parts;
		try {
			parts = req.getParts();
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("File upload quá lớn.");
		}

		List<String> result = new ArrayList<>();
		for (Part p : parts) {
			if (!partName.equals(p.getName()))
				continue;
			if (p.getSize() <= 0)
				continue;

			String submitted = getSubmittedFileName(p);
			if (submitted == null || submitted.isBlank())
				continue;

			result.add(savePartToWebFolder(req, p, folder));
		}
		return result;
	}

	private String savePartToWebFolder(HttpServletRequest req, Part part, String folder) throws Exception {
		String submitted = getSubmittedFileName(part);
		String ext = "";

		int dot = submitted.lastIndexOf('.');
		if (dot >= 0)
			ext = submitted.substring(dot);

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

	private String getSubmittedFileName(Part part) {
		String cd = part.getHeader("content-disposition");
		if (cd == null)
			return null;
		for (String token : cd.split(";")) {
			token = token.trim();
			if (token.startsWith("filename=")) {
				String fileName = token.substring("filename=".length()).trim().replace("\"", "");
				return Paths.get(fileName).getFileName().toString();
			}
		}
		return null;
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
			if (s == null || s.isBlank())
				return null;
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String s, BigDecimal fallback) {
		try {
			if (s == null || s.isBlank())
				return fallback;
			return new BigDecimal(s.trim());
		} catch (Exception e) {
			return fallback;
		}
	}

	private String safe(String s) {
		return s == null ? "" : s.trim();
	}
}
