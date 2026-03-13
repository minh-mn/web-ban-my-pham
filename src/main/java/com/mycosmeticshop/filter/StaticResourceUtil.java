package com.mycosmeticshop.filter;

public final class StaticResourceUtil {

    private StaticResourceUtil() {}

    public static boolean isStatic(String path) {
        if (path == null) return false;

        return path.startsWith("/assets/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/fonts/")
                || path.startsWith("/uploads/")
                || path.equals("/favicon.ico")
                || path.equals("/robots.txt")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".webp")
                || path.endsWith(".svg")
                || path.endsWith(".woff")
                || path.endsWith(".woff2")
                || path.endsWith(".ttf");
    }
}
