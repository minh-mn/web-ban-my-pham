package com.mycosmeticshop.utils;

import org.mindrot.jbcrypt.BCrypt;

/*
 * Utility tạo hash mật khẩu bằng BCrypt
 *
 * BCrypt là thuật toán hash mật khẩu an toàn:
 * - Có salt ngẫu nhiên
 * - Chống brute force
 * - Có cost factor (độ mạnh)
 *
 * Ví dụ:
 * password = 123456
 * hash = $2a$10$...
 */
public class HashGenerator {

    public static void main(String[] args) {

        // Mật khẩu gốc
        String password = "123456";

        /*
         * Tạo hash bằng BCrypt
         *
         * gensalt(10):
         * 10 = cost factor
         * số càng lớn thì hash càng chậm nhưng càng an toàn
         *
         * khuyến nghị:
         * 10 - 12 cho web app
         */
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));

        // In ra hash để lưu vào database
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
    }
}