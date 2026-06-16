package com.webshop.app.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadConfig {

    private static final String PROJECT_DIR_TOKEN = "$PROJECT_DIR$";
    private static final String DEFAULT_UPLOAD_FOLDER = "MyCosmeticShopUploads";

    /*
     * =========================
     * PHYSICAL UPLOAD DIRECTORY
     * =========================
     *
     * Hỗ trợ 3 cách:
     *
     * 1. Dùng VM option đường dẫn tuyệt đối:
     *    -Dmycosmetic.upload.dir=C:\...\MyCosmeticShop\MyCosmeticShopUploads
     *
     * 2. Dùng VM option có $PROJECT_DIR$:
     *    -Dmycosmetic.upload.dir=$PROJECT_DIR$/MyCosmeticShopUploads
     *
     * 3. Không cấu hình gì:
     *    Tự dò project root rồi dùng:
     *    {projectRoot}/MyCosmeticShopUploads
     */
    public static final Path BASE_DIR = resolveBaseDir();

    public static final Path BANNER_DIR = BASE_DIR.resolve("banner");
    public static final Path PRODUCT_DIR = BASE_DIR.resolve("product");
    public static final Path PRODUCT_GALLERY_DIR = PRODUCT_DIR.resolve("gallery");

    // Issue 123: thư mục lưu media chi tiết sản phẩm: ảnh/video
    public static final Path PRODUCT_MEDIA_DIR = PRODUCT_DIR.resolve("media");

    public static final Path POLICY_DIR = BASE_DIR.resolve("policy");

    // Issue 118: thư mục lưu logo thương hiệu
    public static final Path BRAND_DIR = BASE_DIR.resolve("brand");


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

    // Issue 123: URL lưu vào database cho media chi tiết sản phẩm
    public static final String PRODUCT_MEDIA_URL_PREFIX = UPLOAD_URL_PREFIX + "/product/media/";

    public static final String POLICY_URL_PREFIX = UPLOAD_URL_PREFIX + "/policy/";

    // Issue 118: URL lưu vào database cho logo thương hiệu
    public static final String BRAND_URL_PREFIX = UPLOAD_URL_PREFIX + "/brand/";

    public static final Path EVENT_DIR = BASE_DIR.resolve("event");
    public static final String EVENT_URL_PREFIX = UPLOAD_URL_PREFIX + "/event/";

    public static String toEventUrl(String fileName) {
        return EVENT_URL_PREFIX + cleanFileName(fileName);
    }

    public static Path resolveEventFile(String fileName) {
        return EVENT_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static boolean deleteEventFile(String fileName) {
        return deleteUploadFile(EVENT_DIR, fileName);
    }

    public static boolean deleteEventFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, EVENT_URL_PREFIX, EVENT_DIR);
    }
    

    private UploadConfig() {
    }

    private static Path resolveBaseDir() {
        String configuredDir = System.getProperty("mycosmetic.upload.dir");

        if (configuredDir != null && !configuredDir.trim().isEmpty()) {
            String value = configuredDir.trim().replace("\"", "");

            if (value.contains(PROJECT_DIR_TOKEN)) {
                Path projectRoot = detectProjectRoot();

                if (projectRoot == null) {
                    throw new IllegalStateException(
                            "Cannot resolve " + PROJECT_DIR_TOKEN
                                    + " because project root was not found. "
                                    + "Please use absolute path for -Dmycosmetic.upload.dir."
                    );
                }

                value = value.replace(PROJECT_DIR_TOKEN, projectRoot.toString());
            }

            return Paths.get(value).toAbsolutePath().normalize();
        }

        Path projectRoot = detectProjectRoot();

        if (projectRoot != null) {
            return projectRoot.resolve(DEFAULT_UPLOAD_FOLDER).toAbsolutePath().normalize();
        }

        return Paths.get(System.getProperty("user.dir"), DEFAULT_UPLOAD_FOLDER)
                .toAbsolutePath()
                .normalize();
    }

    private static Path detectProjectRoot() {
        /*
         * Ưu tiên dò từ vị trí class đang chạy.
         * Khi chạy bằng IntelliJ Gradle exploded artifact, class thường nằm trong:
         * {project}/build/classes/...
         * hoặc
         * {project}/build/...
         */
        Path classLocation = getClassLocation();

        Path rootFromClass = findProjectRootUpwards(classLocation);
        if (rootFromClass != null) {
            return rootFromClass;
        }

        /*
         * Fallback: dò từ user.dir.
         * Trường hợp của Tomcat thường là apache-tomcat/bin nên có thể không tìm được project.
         */
        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        return findProjectRootUpwards(userDir);
    }

    private static Path getClassLocation() {
        try {
            URL location = UploadConfig.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            if (location == null) {
                return null;
            }

            return Paths.get(location.toURI()).toAbsolutePath().normalize();
        } catch (Exception e) {
            return null;
        }
    }

    private static Path findProjectRootUpwards(Path start) {
        if (start == null) {
            return null;
        }

        Path current = Files.isRegularFile(start) ? start.getParent() : start;

        while (current != null) {
            boolean hasGradleFile =
                    Files.exists(current.resolve("build.gradle"))
                            || Files.exists(current.resolve("build.gradle.kts"))
                            || Files.exists(current.resolve("settings.gradle"))
                            || Files.exists(current.resolve("settings.gradle.kts"));

            boolean hasSourceFolder = Files.exists(current.resolve("src"));

            if (hasGradleFile && hasSourceFolder) {
                return current.toAbsolutePath().normalize();
            }

            current = current.getParent();
        }

        return null;
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
            Files.createDirectories(PRODUCT_MEDIA_DIR);
            Files.createDirectories(POLICY_DIR);
            Files.createDirectories(BRAND_DIR);
            Files.createDirectories(EVENT_DIR);

            System.out.println("========== MyCosmetic Upload Path ==========");
            System.out.println("user.dir = " + System.getProperty("user.dir"));
            System.out.println("mycosmetic.upload.dir = " + System.getProperty("mycosmetic.upload.dir"));
            System.out.println("BASE_DIR = " + BASE_DIR);
            System.out.println("BANNER_DIR = " + BANNER_DIR);
            System.out.println("PRODUCT_DIR = " + PRODUCT_DIR);
            System.out.println("PRODUCT_GALLERY_DIR = " + PRODUCT_GALLERY_DIR);
            System.out.println("PRODUCT_MEDIA_DIR = " + PRODUCT_MEDIA_DIR);
            System.out.println("POLICY_DIR = " + POLICY_DIR);
            System.out.println("BRAND_DIR = " + BRAND_DIR);
            System.out.println("EVENT_DIR = " + EVENT_DIR);
            System.out.println("============================================");
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

    public static String toProductMediaUrl(String fileName) {
        return PRODUCT_MEDIA_URL_PREFIX + cleanFileName(fileName);
    }

    public static String toPolicyUrl(String fileName) {
        return POLICY_URL_PREFIX + cleanFileName(fileName);
    }

    public static String toBrandUrl(String fileName) {
        return BRAND_URL_PREFIX + cleanFileName(fileName);
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

    public static Path resolveProductMediaFile(String fileName) {
        return PRODUCT_MEDIA_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolvePolicyFile(String fileName) {
        return POLICY_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolveBrandFile(String fileName) {
        return BRAND_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    /*
     * =========================
     * DELETE PHYSICAL UPLOAD FILE
     * =========================
     *
     * Các hàm này dùng sau khi xóa/cập nhật dữ liệu SQL thành công.
     *
     * Có bảo vệ:
     * - Không xóa URL ngoài như http/https/data
     * - Không cho path traversal
     * - Chỉ xóa file nằm trong đúng thư mục upload tương ứng
     * - Không throw lỗi nếu file không tồn tại
     */

    public static boolean deleteBannerFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, BANNER_URL_PREFIX, BANNER_DIR);
    }

    public static boolean deleteBrandFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, BRAND_URL_PREFIX, BRAND_DIR);
    }

    public static boolean deletePolicyFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, POLICY_URL_PREFIX, POLICY_DIR);
    }

    public static boolean deleteProductFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, PRODUCT_URL_PREFIX, PRODUCT_DIR);
    }

    public static boolean deleteProductGalleryFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, PRODUCT_GALLERY_URL_PREFIX, PRODUCT_GALLERY_DIR);
    }

    public static boolean deleteProductMediaFileByUrl(String fileUrl) {
        return deleteUploadFileByUrl(fileUrl, PRODUCT_MEDIA_URL_PREFIX, PRODUCT_MEDIA_DIR);
    }

    /*
     * Hàm tổng quát nếu controller/DAO muốn xóa theo URL bất kỳ trong /uploads.
     * Ưu tiên dùng các hàm delete... cụ thể bên trên để tránh nhầm thư mục.
     */
    public static boolean deleteUploadFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return false;
        }

        String value = normalizeSlash(fileUrl);

        if (isExternalOrDataUrl(value)) {
            return false;
        }

        if (value.startsWith(BANNER_URL_PREFIX)) {
            return deleteBannerFileByUrl(value);
        }

        if (value.startsWith(BRAND_URL_PREFIX)) {
            return deleteBrandFileByUrl(value);
        }

        if (value.startsWith(POLICY_URL_PREFIX)) {
            return deletePolicyFileByUrl(value);
        }

        if (value.startsWith(PRODUCT_GALLERY_URL_PREFIX)) {
            return deleteProductGalleryFileByUrl(value);
        }

        if (value.startsWith(PRODUCT_MEDIA_URL_PREFIX)) {
            return deleteProductMediaFileByUrl(value);
        }

        if (value.startsWith(PRODUCT_URL_PREFIX)) {
            return deleteProductFileByUrl(value);
        }

        return false;
    }

    private static boolean deleteUploadFileByUrl(String fileUrl, String expectedPrefix, Path allowedDir) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return false;
        }

        String value = normalizeSlash(fileUrl);

        if (isExternalOrDataUrl(value)) {
            return false;
        }

        if (!value.startsWith(expectedPrefix)) {
            return false;
        }

        String fileName = cleanFileName(value.substring(expectedPrefix.length()));

        return deleteUploadFile(allowedDir, fileName);
    }

    public static boolean deleteBannerFile(String fileName) {
        return deleteUploadFile(BANNER_DIR, fileName);
    }

    public static boolean deleteBrandFile(String fileName) {
        return deleteUploadFile(BRAND_DIR, fileName);
    }

    public static boolean deletePolicyFile(String fileName) {
        return deleteUploadFile(POLICY_DIR, fileName);
    }

    public static boolean deleteProductFile(String fileName) {
        return deleteUploadFile(PRODUCT_DIR, fileName);
    }

    public static boolean deleteProductGalleryFile(String fileName) {
        return deleteUploadFile(PRODUCT_GALLERY_DIR, fileName);
    }

    public static boolean deleteProductMediaFile(String fileName) {
        return deleteUploadFile(PRODUCT_MEDIA_DIR, fileName);
    }

    private static boolean deleteUploadFile(Path allowedDir, String fileName) {
        if (allowedDir == null || fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        try {
            Path root = allowedDir.toAbsolutePath().normalize();
            String cleanedName = cleanFileName(fileName);

            if (cleanedName.isBlank()) {
                return false;
            }

            Path target = root.resolve(cleanedName).toAbsolutePath().normalize();

            if (!target.startsWith(root)) {
                return false;
            }

            if (Files.isDirectory(target)) {
                return false;
            }

            return Files.deleteIfExists(target);
        } catch (Exception e) {
            System.err.println("[UploadConfig] Cannot delete upload file: " + fileName + " - " + e.getMessage());
            return false;
        }
    }

    /*
     * =========================
     * NORMALIZE EXISTING IMAGE / MEDIA URL
     * =========================
     */

    public static String normalizeProductImageUrl(String image) {
        return normalizeUploadUrl(image, PRODUCT_URL_PREFIX);
    }

    public static String normalizeProductGalleryImageUrl(String image) {
        return normalizeUploadUrl(image, PRODUCT_GALLERY_URL_PREFIX);
    }

    public static String normalizeProductMediaUrl(String media) {
        return normalizeUploadUrl(media, PRODUCT_MEDIA_URL_PREFIX);
    }

    public static String normalizeBannerImageUrl(String image) {
        return normalizeUploadUrl(image, BANNER_URL_PREFIX);
    }

    public static String normalizeBrandImageUrl(String image) {
        return normalizeUploadUrl(image, BRAND_URL_PREFIX);
    }

    public static String normalizePolicyFileUrl(String file) {
        return normalizeUploadUrl(file, POLICY_URL_PREFIX);
    }

    private static String normalizeUploadUrl(String image, String targetPrefix) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        String value = normalizeSlash(image);

        if (isExternalOrDataUrl(value)) {
            return value;
        }

        if (value.startsWith(UPLOAD_URL_PREFIX + "/")) {
            return value;
        }

        value = value.replaceFirst("^/assets/images/products/gallery/", "");
        value = value.replaceFirst("^/assets/images/product/gallery/", "");
        value = value.replaceFirst("^assets/images/products/gallery/", "");
        value = value.replaceFirst("^assets/images/product/gallery/", "");

        value = value.replaceFirst("^/assets/images/products/media/", "");
        value = value.replaceFirst("^/assets/images/product/media/", "");
        value = value.replaceFirst("^assets/images/products/media/", "");
        value = value.replaceFirst("^assets/images/product/media/", "");

        value = value.replaceFirst("^/assets/images/products/", "");
        value = value.replaceFirst("^/assets/images/product/", "");
        value = value.replaceFirst("^assets/images/products/", "");
        value = value.replaceFirst("^assets/images/product/", "");

        value = value.replaceFirst("^/assets/images/banners/", "");
        value = value.replaceFirst("^/assets/images/banner/", "");
        value = value.replaceFirst("^assets/images/banners/", "");
        value = value.replaceFirst("^assets/images/banner/", "");

        value = value.replaceFirst("^/assets/images/brands/", "");
        value = value.replaceFirst("^/assets/images/brand/", "");
        value = value.replaceFirst("^assets/images/brands/", "");
        value = value.replaceFirst("^assets/images/brand/", "");

        value = value.replaceFirst("^/assets/files/policy/", "");
        value = value.replaceFirst("^/assets/policy/", "");
        value = value.replaceFirst("^assets/files/policy/", "");
        value = value.replaceFirst("^assets/policy/", "");

        value = value.replaceFirst("^products/gallery/", "");
        value = value.replaceFirst("^/products/gallery/", "");

        value = value.replaceFirst("^products/media/", "");
        value = value.replaceFirst("^/products/media/", "");

        value = value.replaceFirst("^product/gallery/", "");
        value = value.replaceFirst("^/product/gallery/", "");

        value = value.replaceFirst("^product/media/", "");
        value = value.replaceFirst("^/product/media/", "");

        value = value.replaceFirst("^products/", "");
        value = value.replaceFirst("^/products/", "");
        value = value.replaceFirst("^product/", "");
        value = value.replaceFirst("^/product/", "");

        value = value.replaceFirst("^banners/", "");
        value = value.replaceFirst("^/banners/", "");
        value = value.replaceFirst("^banner/", "");
        value = value.replaceFirst("^/banner/", "");

        value = value.replaceFirst("^brands/", "");
        value = value.replaceFirst("^/brands/", "");
        value = value.replaceFirst("^brand/", "");
        value = value.replaceFirst("^/brand/", "");

        value = value.replaceFirst("^policies/", "");
        value = value.replaceFirst("^/policies/", "");
        value = value.replaceFirst("^policy/", "");
        value = value.replaceFirst("^/policy/", "");

        value = cleanFileName(value);

        return targetPrefix + value;
    }

    private static String normalizeSlash(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replace("\\", "/");
    }

    private static boolean isExternalOrDataUrl(String value) {
        if (value == null) {
            return false;
        }

        String lower = value.trim().toLowerCase();

        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("data:");
    }

    private static String cleanFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        String cleaned = normalizeSlash(fileName);

        int queryIndex = cleaned.indexOf("?");
        if (queryIndex >= 0) {
            cleaned = cleaned.substring(0, queryIndex);
        }

        int hashIndex = cleaned.indexOf("#");
        if (hashIndex >= 0) {
            cleaned = cleaned.substring(0, hashIndex);
        }

        int lastSlash = cleaned.lastIndexOf("/");
        if (lastSlash >= 0) {
            cleaned = cleaned.substring(lastSlash + 1);
        }

        cleaned = cleaned.replace("..", "");

        return cleaned;
    }
}
