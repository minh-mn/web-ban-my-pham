package com.webshop.app.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadConfig {

    /*
     * =========================
     * PHYSICAL UPLOAD DIRECTORY
     * =========================
     *
     * File ảnh/file upload thật lưu trong:
     * MyCosmeticShopUploads/
     *
     * Database KHÔNG lưu đường dẫn vật lý kiểu:
     * D:/...
     * C:/...
     *
     * Database chỉ lưu URL public dạng:
     *
     * /uploads/banner/...
     * /uploads/product/...
     * /uploads/product/gallery/...
     * /uploads/policy/...
     */
    public static final Path BASE_DIR = Paths.get(
            System.getProperty(
                    "mycosmetic.upload.dir",
                    Paths.get(System.getProperty("user.dir"), "MyCosmeticShopUploads").toString()
            )
    ).toAbsolutePath().normalize();

    public static final Path BANNER_DIR = BASE_DIR.resolve("banner");
    public static final Path PRODUCT_DIR = BASE_DIR.resolve("product");
    public static final Path PRODUCT_GALLERY_DIR = PRODUCT_DIR.resolve("gallery");
    public static final Path POLICY_DIR = BASE_DIR.resolve("policy");

    /*
     * =========================
     * PUBLIC URL PREFIX
     * =========================
     *
     * Các giá trị này dùng để lưu vào database.
     */
    public static final String UPLOAD_URL_PREFIX = "/uploads";

    public static final String BANNER_URL_PREFIX = UPLOAD_URL_PREFIX + "/banner/";
    public static final String PRODUCT_URL_PREFIX = UPLOAD_URL_PREFIX + "/product/";
    public static final String PRODUCT_GALLERY_URL_PREFIX = UPLOAD_URL_PREFIX + "/product/gallery/";
    public static final String POLICY_URL_PREFIX = UPLOAD_URL_PREFIX + "/policy/";

    private UploadConfig() {
    }

    /*
     * Tạo đủ thư mục upload nếu chưa tồn tại.
     */
    public static void ensureUploadDirectories() {
        try {
            Files.createDirectories(BASE_DIR);
            Files.createDirectories(BANNER_DIR);
            Files.createDirectories(PRODUCT_DIR);
            Files.createDirectories(PRODUCT_GALLERY_DIR);
            Files.createDirectories(POLICY_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directories at: " + BASE_DIR, e);
        }
    }

    /*
     * =========================
     * BUILD PUBLIC URL FOR DATABASE
     * =========================
     */

    public static String toBannerUrl(String fileName) {
        return BANNER_URL_PREFIX + cleanFileName(fileName);
    }

    public static String toProductUrl(String fileName) {
        return PRODUCT_URL_PREFIX + cleanFileName(fileName);
    }

    public static String toProductGalleryUrl(String fileName) {
        return PRODUCT_GALLERY_URL_PREFIX + cleanFileName(fileName);
    }

    public static String toPolicyUrl(String fileName) {
        return POLICY_URL_PREFIX + cleanFileName(fileName);
    }

    /*
     * =========================
     * RESOLVE PHYSICAL FILE PATH
     * =========================
     */

    public static Path resolveBannerFile(String fileName) {
        return BANNER_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolveProductFile(String fileName) {
        return PRODUCT_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolveProductGalleryFile(String fileName) {
        return PRODUCT_GALLERY_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolvePolicyFile(String fileName) {
        return POLICY_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    /*
     * =========================
     * NORMALIZE EXISTING IMAGE URL
     * =========================
     *
     * Dùng khi code nhận ảnh từ form/database để tránh lưu lẫn:
     * /assets/images/product/...
     * /assets/images/banner/...
     * products/...
     * banners/...
     */

    public static String normalizeProductImageUrl(String image) {
        return normalizeUploadUrl(image, PRODUCT_URL_PREFIX);
    }

    public static String normalizeProductGalleryImageUrl(String image) {
        return normalizeUploadUrl(image, PRODUCT_GALLERY_URL_PREFIX);
    }

    public static String normalizeBannerImageUrl(String image) {
        return normalizeUploadUrl(image, BANNER_URL_PREFIX);
    }

    private static String normalizeUploadUrl(String image, String targetPrefix) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        String value = image.trim().replace("\\", "/");

        /*
         * Giữ nguyên URL ngoài hoặc data URI.
         */
        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")) {
            return value;
        }

        /*
         * Nếu đã đúng chuẩn /uploads/... thì giữ nguyên.
         */
        if (value.startsWith(UPLOAD_URL_PREFIX + "/")) {
            return value;
        }

        /*
         * Xóa các prefix cũ.
         */
        value = value.replaceFirst("^/assets/images/products/gallery/", "");
        value = value.replaceFirst("^/assets/images/product/gallery/", "");
        value = value.replaceFirst("^assets/images/products/gallery/", "");
        value = value.replaceFirst("^assets/images/product/gallery/", "");

        value = value.replaceFirst("^/assets/images/products/", "");
        value = value.replaceFirst("^/assets/images/product/", "");
        value = value.replaceFirst("^assets/images/products/", "");
        value = value.replaceFirst("^assets/images/product/", "");

        value = value.replaceFirst("^/assets/images/banners/", "");
        value = value.replaceFirst("^/assets/images/banner/", "");
        value = value.replaceFirst("^assets/images/banners/", "");
        value = value.replaceFirst("^assets/images/banner/", "");

        value = value.replaceFirst("^products/gallery/", "");
        value = value.replaceFirst("^/products/gallery/", "");
        value = value.replaceFirst("^products/", "");
        value = value.replaceFirst("^/products/", "");

        value = value.replaceFirst("^banners/", "");
        value = value.replaceFirst("^/banners/", "");
        value = value.replaceFirst("^banner/", "");
        value = value.replaceFirst("^/banner/", "");

        value = cleanFileName(value);

        return targetPrefix + value;
    }

    private static String cleanFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        String cleaned = fileName.trim().replace("\\", "/");

        /*
         * Chỉ lấy tên file cuối cùng để tránh path traversal.
         */
        int lastSlash = cleaned.lastIndexOf("/");
        if (lastSlash >= 0) {
            cleaned = cleaned.substring(lastSlash + 1);
        }

        cleaned = cleaned.replace("..", "");

        return cleaned;
    }
}