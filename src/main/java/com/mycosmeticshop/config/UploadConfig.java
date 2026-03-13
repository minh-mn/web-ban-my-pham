package com.mycosmeticshop.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public final class UploadConfig {

    // =========================================================
    // THƯ MỤC GỐC LƯU FILE UPLOAD
    // Tất cả ảnh banner và sản phẩm sẽ được lưu tại:
    // D:/TTLTW/MyCosmeticShopUploads
    // =========================================================
    public static final Path BASE_DIR = Paths.get("D:/TTLTW/MyCosmeticShopUploads");

    // =========================================================
    // THƯ MỤC LƯU ẢNH BANNER
    // Ví dụ:
    // D:/TTLTW/MyCosmeticShopUploads/banner/banner1.jpg
    // =========================================================
    public static final Path BANNER_DIR  = BASE_DIR.resolve("banner");

    // =========================================================
    // THƯ MỤC LƯU ẢNH SẢN PHẨM
    // Ví dụ:
    // D:/TTLTW/MyCosmeticShopUploads/product/product1.jpg
    // =========================================================
    public static final Path PRODUCT_DIR = BASE_DIR.resolve("product");

    // =========================================================
    // STATIC BLOCK
    // Khi hệ thống khởi động sẽ tự động tạo các thư mục
    // nếu chúng chưa tồn tại
    // =========================================================
    static {
        try {
            Files.createDirectories(BANNER_DIR);
            Files.createDirectories(PRODUCT_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload", e);
        }
    }

    // =========================================================
    // CONSTRUCTOR PRIVATE
    // Không cho phép tạo object từ class này
    // vì đây chỉ là class cấu hình (config class)
    // =========================================================
    private UploadConfig() {}
}