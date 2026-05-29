package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
import com.webshop.app.dao.BannerDAO;
import com.webshop.app.model.Banner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/banners")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 20 * 1024 * 1024
)
public class AdminBannerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BannerDAO bannerDAO = new BannerDAO();

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp", "gif");

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
                req.getRequestDispatcher("/jsp/admin/banner/banner_form.jsp").forward(req, resp);
                return;
            }

            case "edit": {
                int id = safeParseInt(req.getParameter("id"), -1);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/banners");
                    return;
                }

                Banner banner = bannerDAO.findById(id);

                if (banner == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/banners");
                    return;
                }

                req.setAttribute("banner", banner);
                req.getRequestDispatcher("/jsp/admin/banner/banner_form.jsp").forward(req, resp);
                return;
            }

            default: {
                req.setAttribute("banners", bannerDAO.findAll());
                req.getRequestDispatcher("/jsp/admin/banner/banner_list.jsp").forward(req, resp);
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

        try {
            switch (action) {
                case "create": {
                    Banner banner = new Banner();
                    bind(req, banner, true);

                    bannerDAO.create(banner);
                    break;
                }

                case "update": {
                    int id = safeParseInt(req.getParameter("id"), -1);

                    if (id <= 0) {
                        break;
                    }

                    Banner banner = bannerDAO.findById(id);

                    if (banner != null) {
                        String oldImageUrl = banner.getImageUrl();

                        bind(req, banner, false);
                        banner.setId(id);

                        String newImageUrl = banner.getImageUrl();

                        /*
                         * Cập nhật SQL trước.
                         * Nếu update lỗi, code sẽ nhảy vào catch và KHÔNG xóa file cũ.
                         */
                        bannerDAO.update(banner);

                        /*
                         * Nếu admin upload ảnh mới, xóa file banner cũ sau khi SQL update thành công.
                         */
                        if (isChangedLocalUploadFile(oldImageUrl, newImageUrl, UploadConfig.BANNER_URL_PREFIX)) {
                            UploadConfig.deleteBannerFileByUrl(oldImageUrl);
                        }
                    }

                    break;
                }

                case "delete": {
                    int id = safeParseInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        Banner banner = bannerDAO.findById(id);
                        String oldImageUrl = banner == null ? null : banner.getImageUrl();

                        /*
                         * Xóa SQL trước.
                         * Nếu delete lỗi, code sẽ nhảy vào catch và KHÔNG xóa file vật lý.
                         */
                        bannerDAO.delete(id);

                        /*
                         * Xóa file banner vật lý sau khi SQL delete thành công.
                         */
                        UploadConfig.deleteBannerFileByUrl(oldImageUrl);
                    }

                    break;
                }

                case "toggle": {
                    int id = safeParseInt(req.getParameter("id"), -1);

                    if (id > 0) {
                        bannerDAO.toggleActive(id);
                    }

                    break;
                }

                default: {
                    break;
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/banners");
        } catch (IllegalArgumentException ex) {
            throw new ServletException(ex.getMessage(), ex);
        }
    }

    private void bind(HttpServletRequest req, Banner banner, boolean isCreate)
            throws IOException, ServletException {

        banner.setTitle(trim(req.getParameter("title")));
        banner.setLink(trim(req.getParameter("link")));
        banner.setActive(Objects.equals(req.getParameter("active"), "1"));

        /*
         * Khi update mà không upload ảnh mới thì giữ ảnh cũ.
         */
        String imageUrl = isCreate ? "" : trim(req.getParameter("existingImage"));

        Part imagePart = req.getPart("imageFile");

        if (imagePart != null && imagePart.getSize() > 0) {
            String contentType = imagePart.getContentType();

            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("File upload không hợp lệ. Chỉ chấp nhận ảnh.");
            }

            String original = Paths.get(imagePart.getSubmittedFileName())
                    .getFileName()
                    .toString();

            String ext = getExtensionLower(original);

            if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) {
                throw new IllegalArgumentException("Định dạng ảnh không hỗ trợ. Chỉ chấp nhận: png, jpg, jpeg, webp, gif.");
            }

            Files.createDirectories(UploadConfig.BANNER_DIR);

            String baseName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeName = System.currentTimeMillis() + "_" + baseName;

            Path destination = UploadConfig.resolveBannerFile(safeName);

            try (InputStream inputStream = imagePart.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            /*
             * File thật:
             * MyCosmeticShopUploads/banner/{safeName}
             *
             * Database chỉ lưu:
             * /uploads/banner/{safeName}
             */
            imageUrl = UploadConfig.toBannerUrl(safeName);
        }

        if (isCreate && (imageUrl == null || imageUrl.isBlank())) {
            imageUrl = "";
        }

        banner.setImageUrl(imageUrl);
    }

    private boolean isChangedLocalUploadFile(String oldUrl, String newUrl, String expectedPrefix) {
        String oldValue = trim(oldUrl);
        String newValue = trim(newUrl);

        if (oldValue.isBlank()) {
            return false;
        }

        if (!oldValue.startsWith(expectedPrefix)) {
            return false;
        }

        return !oldValue.equals(newValue);
    }

    private static int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String getExtensionLower(String filename) {
        int dot = filename.lastIndexOf('.');

        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }

        return filename.substring(dot + 1).toLowerCase();
    }
}