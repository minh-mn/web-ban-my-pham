package com.webshop.app.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadConfig {
    public static final Path BASE_DIR = Paths.get("D:/MyCosmeticShopUploads");

    public static final Path BANNER_DIR  = BASE_DIR.resolve("banner");
    public static final Path PRODUCT_DIR = BASE_DIR.resolve("product");

    public static final Path POLICY_DIR = BASE_DIR.resolve("policy");

    private UploadConfig() {}
}
