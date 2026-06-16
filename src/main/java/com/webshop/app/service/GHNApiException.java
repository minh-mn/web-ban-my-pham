package com.webshop.app.service;

public class GHNApiException extends RuntimeException {

    public GHNApiException(String message) {
        super(message);
    }

    public GHNApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
