package com.bufalari.exception;

public class GeocodingApiException extends RuntimeException {
    public GeocodingApiException(String message, Throwable cause) {
        super(message, cause);
    }
}