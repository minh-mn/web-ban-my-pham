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
    public static final String POLICY_URL_PREFIX = UPLOAD_URL_PREFIX + "/policy/";

    // Issue 118: URL lưu vào database cho logo thương hiệu
    public static final String BRAND_URL_PREFIX = UPLOAD_URL_PREFIX + "/brand/";

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
            Files.createDirectories(POLICY_DIR);
            Files.createDirectories(BRAND_DIR);

            System.out.println("========== MyCosmetic Upload Path ==========");
            System.out.println("user.dir = " + System.getProperty("user.dir"));
            System.out.println("mycosmetic.upload.dir = " + System.getProperty("mycosmetic.upload.dir"));
            System.out.println("BASE_DIR = " + BASE_DIR);
            System.out.println("BANNER_DIR = " + BANNER_DIR);
            System.out.println("PRODUCT_DIR = " + PRODUCT_DIR);
            System.out.println("PRODUCT_GALLERY_DIR = " + PRODUCT_GALLERY_DIR);
            System.out.println("POLICY_DIR = " + POLICY_DIR);
            System.out.println("BRAND_DIR = " + BRAND_DIR);
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

    public static Path resolvePolicyFile(String fileName) {
        return POLICY_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    public static Path resolveBrandFile(String fileName) {
        return BRAND_DIR.resolve(cleanFileName(fileName)).normalize();
    }

    /*
     * =========================
     * NORMALIZE EXISTING IMAGE URL
     * =========================
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

    public static String normalizeBrandImageUrl(String image) {
        return normalizeUploadUrl(image, BRAND_URL_PREFIX);
    }

    private static String normalizeUploadUrl(String image, String targetPrefix) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        String value = image.trim().replace("\\", "/");

        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")) {
            return value;
        }

        if (value.startsWith(UPLOAD_URL_PREFIX + "/")) {
            return value;
        }

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

        value = value.replaceFirst("^/assets/images/brands/", "");
        value = value.replaceFirst("^/assets/images/brand/", "");
        value = value.replaceFirst("^assets/images/brands/", "");
        value = value.replaceFirst("^assets/images/brand/", "");

        value = value.replaceFirst("^products/gallery/", "");
        value = value.replaceFirst("^/products/gallery/", "");
        value = value.replaceFirst("^products/", "");
        value = value.replaceFirst("^/products/", "");

        value = value.replaceFirst("^banners/", "");
        value = value.replaceFirst("^/banners/", "");
        value = value.replaceFirst("^banner/", "");
        value = value.replaceFirst("^/banner/", "");

        value = value.replaceFirst("^brands/", "");
        value = value.replaceFirst("^/brands/", "");
        value = value.replaceFirst("^brand/", "");
        value = value.replaceFirst("^/brand/", "");

        value = cleanFileName(value);

        return targetPrefix + value;
    }

    private static String cleanFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        String cleaned = fileName.trim().replace("\\", "/");

        int lastSlash = cleaned.lastIndexOf("/");
        if (lastSlash >= 0) {
            cleaned = cleaned.substring(lastSlash + 1);
        }

        cleaned = cleaned.replace("..", "");

        return cleaned;
    }
}