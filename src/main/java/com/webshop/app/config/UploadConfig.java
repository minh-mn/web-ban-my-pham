package com.webshop.app.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadConfig {

    /*
     * Thư mục upload mặc định nằm trong thư mục gốc project:
     * MyCosmeticShop/MyCosmeticShopUploads
     *
     * Nếu deploy lên server thật, có thể override bằng VM option:
     * -Dmycosmetic.upload.dir=/usr/deploy/MyCosmeticShopUploads
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

    private UploadConfig() {
    }
}