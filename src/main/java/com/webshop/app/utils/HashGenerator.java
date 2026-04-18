package com.webshop.app.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("123456", BCrypt.gensalt(10));
        System.out.println(hash);
    }
}
