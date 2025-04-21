package com.bufalari.createpeople.exception;

/**
 * Exceção lançada quando ocorre erro na chamada da API de geocodificação.
 * Exception thrown when an error occurs during the geocoding API call.
 */
public class GeocodingApiException extends RuntimeException {
    
    public GeocodingApiException(String message) {
        super(message);
    }

    public GeocodingApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
