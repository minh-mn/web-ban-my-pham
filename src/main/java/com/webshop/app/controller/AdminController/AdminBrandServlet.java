package com.webshop.app.controller.AdminController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.BrandDAO;
import com.webshop.app.model.Brand;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/brands")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class AdminBrandServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    public void init() throws ServletException {
        super.init();
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
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "edit": {
                int id = safeInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brands");
                    return;
                }

                Brand brand = brandDAO.findById(id);

                if (brand == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/brands");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("brand", brand);

                req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                        .forward(req, resp);
                break;
            }

            case "list":
            default: {
                req.setAttribute("brands", brandDAO.findAll());
                req.getRequestDispatcher("/jsp/admin/brand/brand_list.jsp")
                        .forward(req, resp);
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        /*
         * Dùng để rollback file mới nếu SQL create/update bị lỗi.
         */
        String uploadedFileUrlForRollback = null;

        try {
            switch (action) {

                case "create": {
                    String name = safe(req.getParameter("name"));
                    validateName(name);

                    String imageUrl = null;
                    Part logoPart = getLogoPart(req);

                    if (hasUploadedFile(logoPart)) {
                        imageUrl = saveBrandLogo(logoPart);
                        uploadedFileUrlForRollback = imageUrl;
                    }

                    /*
                     * Tạo SQL thành công thì không rollback file mới nữa.
                     */
                    brandDAO.create(name, imageUrl);
                    uploadedFileUrlForRollback = null;

                    break;
                }

                case "update": {
                    int id = safeInt(req.getParameter("id"), -1);
                    if (id <= 0) {
                        throw new IllegalArgumentException("ID thương hiệu không hợp lệ.");
                    }

                    String name = safe(req.getParameter("name"));
                    validateName(name);

                    Brand oldBrand = brandDAO.findById(id);
                    if (oldBrand == null) {
                        throw new IllegalArgumentException("Không tìm thấy thương hiệu cần sửa.");
                    }

                    /*
                     * Ưu tiên lấy ảnh cũ từ DB để chắc chắn đúng dữ liệu hiện tại.
                     * Nếu model Brand của bạn lưu logo trong field image thì getImage() sẽ trả về /uploads/brand/...
                     */
                    String oldImageUrl = safe(oldBrand.getImage());

                    /*
                     * Khi update mà không upload logo mới thì giữ nguyên ảnh cũ.
                     * existingImage chỉ dùng fallback nếu DB không có image.
                     */
                    String imageUrl = !oldImageUrl.isBlank()
                            ? oldImageUrl
                            : safe(req.getParameter("existingImage"));

                    Part logoPart = getLogoPart(req);

                    if (hasUploadedFile(logoPart)) {
                        imageUrl = saveBrandLogo(logoPart);
                        uploadedFileUrlForRollback = imageUrl;
                    }

                    /*
                     * Update SQL trước.
                     * Nếu SQL lỗi thì file cũ không bị xóa, file mới sẽ rollback ở catch.
                     */
                    brandDAO.update(id, name, emptyToNull(imageUrl));
                    uploadedFileUrlForRollback = null;

                    /*
                     * Nếu admin upload logo mới và SQL update thành công,
                     * xóa logo cũ trong MyCosmeticShopUploads/brand.
                     */
                    if (isChangedLocalUploadFile(oldImageUrl, imageUrl, UploadConfig.BRAND_URL_PREFIX)) {
                        UploadConfig.deleteBrandFileByUrl(oldImageUrl);
                    }

                    break;
                }

                case "delete": {
                    int id = safeInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        Brand oldBrand = brandDAO.findById(id);
                        String oldImageUrl = oldBrand == null ? null : oldBrand.getImage();

                        /*
                         * Xóa SQL trước.
                         * Nếu brand đang được product tham chiếu và SQL lỗi,
                         * code sẽ nhảy vào catch nên KHÔNG xóa file logo.
                         */
                        brandDAO.delete(id);

                        /*
                         * SQL xóa thành công mới xóa file vật lý.
                         */
                        UploadConfig.deleteBrandFileByUrl(oldImageUrl);
                    }

                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/brands");

        } catch (IllegalArgumentException ex) {
            if (uploadedFileUrlForRollback != null) {
                UploadConfig.deleteBrandFileByUrl(uploadedFileUrlForRollback);
            }

            forwardBackToForm(req, resp, action, ex.getMessage());

        } catch (Exception ex) {
            if (uploadedFileUrlForRollback != null) {
                UploadConfig.deleteBrandFileByUrl(uploadedFileUrlForRollback);
            }

            throw new ServletException("AdminBrandServlet error", ex);
        }
    }

    /* ===================== FORM ERROR ===================== */

    private void forwardBackToForm(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   String action,
                                   String errorMessage)
            throws ServletException, IOException {

        req.setAttribute("error", errorMessage);

        Brand brand = new Brand();
        brand.setId(safeInt(req.getParameter("id"), 0));
        brand.setName(safe(req.getParameter("name")));
        brand.setImage(safe(req.getParameter("existingImage")));

        if ("update".equals(action)) {
            req.setAttribute("mode", "edit");
        } else {
            req.setAttribute("mode", "create");
        }

        req.setAttribute("brand", brand);

        req.getRequestDispatcher("/jsp/admin/brand/brand_form.jsp")
                .forward(req, resp);
    }

    /* ===================== UPLOAD LOGO ===================== */

    private Part getLogoPart(HttpServletRequest req) throws IOException, ServletException {
        Part part = req.getPart("imageFile");

        if (part == null) {
            part = req.getPart("image");
        }

        return part;
    }

    private boolean hasUploadedFile(Part part) {
        if (part == null || part.getSize() <= 0) {
            return false;
        }

        String submittedFileName = part.getSubmittedFileName();
        return submittedFileName != null && !submittedFileName.isBlank();
    }

    private String saveBrandLogo(Part part) throws IOException {

        String submittedFileName = Paths.get(part.getSubmittedFileName())
                .getFileName()
                .toString();

        String extension = getExtension(submittedFileName);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Logo chỉ hỗ trợ JPG, JPEG, PNG, WEBP hoặc GIF."
            );
        }

        String contentType = part.getContentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("File không phải ảnh hợp lệ.");
        }

        // ĐÚNG THƯ MỤC BẠN MUỐN
        String uploadDir = getServletContext().getRealPath("")
                + File.separator + "assets"
                + File.separator + "images"
                + File.separator + "brand";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // GIỮ NGUYÊN TÊN FILE
        String fileName = submittedFileName;

        String fullPath = uploadDir + File.separator + fileName;

        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
        }

        // LƯU VÀO DB
        return "/assets/images/brand/" + fileName;
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
            throw new IllegalArgumentException(
                    "Tên file logo không hợp lệ. Vui lòng chọn ảnh JPG, JPEG, PNG, WEBP hoặc GIF."
            );
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT)
                .trim();

        if (extension.isBlank()) {
            throw new IllegalArgumentException(
                    "File logo không có phần mở rộng hợp lệ."
            );
        }

        return extension;
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

    /* ===================== HELPERS ===================== */

    private int safeInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }

            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String emptyToNull(String s) {
        String value = safe(s);
        return value.isEmpty() ? null : value;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống.");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException("Tên thương hiệu không được vượt quá 150 ký tự.");
        }
    }
}
