package com.mycosmeticshop.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

// Class cấu hình thư mục lưu file upload (ảnh banner và ảnh sản phẩm)
public final class UploadConfig {

    // Thư mục gốc lưu tất cả file upload
    public static final Path BASE_DIR = Paths.get("D:/TTLTW/MyCosmeticShopUploads");

    // Thư mục lưu ảnh banner
    public static final Path BANNER_DIR  = BASE_DIR.resolve("banner");

    // Thư mục lưu ảnh sản phẩm
    public static final Path PRODUCT_DIR = BASE_DIR.resolve("product");

    // Khi hệ thống khởi động sẽ tự tạo các thư mục nếu chưa tồn tại
    static {
        try {
            Files.createDirectories(BANNER_DIR);
            Files.createDirectories(PRODUCT_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload", e);
        }
    }

    // Constructor private để không cho tạo object
    // vì đây chỉ là class cấu hình
    private UploadConfig() {}
}