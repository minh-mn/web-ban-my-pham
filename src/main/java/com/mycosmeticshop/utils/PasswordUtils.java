package com.mycosmeticshop.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;

        // storedHash phải là BCrypt hash hợp lệ: $2a$ / $2b$ / $2y$
        if (!(storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"))) {
            return false; // hoặc throw để biết DB đang lưu sai
        }

        return BCrypt.checkpw(rawPassword, storedHash);
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }
}
