package com.bufalari.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler for handling exceptions and providing standardized error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ClientAlreadyExistsException.
     *
     * @param ex      The ClientAlreadyExistsException that occurred.
     * @param request The WebRequest object.
     * @return A ResponseEntity with an ErrorResponse and HTTP status 400 (Bad Request).
     */
    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleClientAlreadyExistsException(ClientAlreadyExistsException ex, WebRequest request) {
        String traceId = generateTraceId();
        logger.error("[TRACE-ID: {}] - Client already exists: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Bad Request");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));
        errorResponse.setSuggestion("Please use a different email or SIN number.");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ClientNotFoundException.
     *
     * @param ex      The ClientNotFoundException that occurred.
     * @param request The WebRequest object.
     * @return A ResponseEntity with an ErrorResponse and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFoundException(ClientNotFoundException ex, WebRequest request) {
        String traceId = generateTraceId();
        logger.error("[TRACE-ID: {}] - Client not found: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setError("Not Found");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidClientDataException.
     *
     * @param ex      The InvalidClientDataException that occurred.
     * @param request The WebRequest object.
     * @return A ResponseEntity with an ErrorResponse and HTTP status 400 (Bad Request).
     */
    @ExceptionHandler(InvalidClientDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientDataException(InvalidClientDataException ex, WebRequest request) {
        String traceId = generateTraceId();
        logger.error("[TRACE-ID: {}] - Invalid client data: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError("Bad Request");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles GeocodingApiException.
     *
     * @param ex      The GeocodingApiException that occurred.
     * @param request The WebRequest object.
     * @return A ResponseEntity with an ErrorResponse and HTTP status 500 (Internal Server Error).
     */
    @ExceptionHandler(GeocodingApiException.class)
    public ResponseEntity<ErrorResponse> handleGeocodingApiException(GeocodingApiException ex, WebRequest request) {
        String traceId = generateTraceId();
        logger.error("[TRACE-ID: {}] - Geocoding API error: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("An error occurred while retrieving geographic coordinates."); // Generic message for security
        errorResponse.setPath(request.getDescription(false));

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generates a unique trace ID for the request.
     *
     * @return A UUID string representing the trace ID.
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}