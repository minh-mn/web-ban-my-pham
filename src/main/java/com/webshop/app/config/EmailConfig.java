package com.webshop.app.config;

public class EmailConfig {
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    public static final String SMTP_USERNAME = System.getenv("MY_SMTP_USER"); 
    public static final String SMTP_APP_PASSWORD = System.getenv("MY_SMTP_PASS");
    public static final String FROM_NAME = "MyCosmetic Shop";
}
