package com.bufalari.people.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API.
 * Catches specific exceptions and formats them into a standardized ErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- 400 Bad Request ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> "'" + error.getField() + "': " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String message = "Validation failed: " + errors;
        log.warn("Validation error: {} - Path: {}", message, request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), "Correct the invalid fields.");
    }

    @ExceptionHandler(ConstraintViolationException.class) // Handles validation on path variables, request params
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        String message = "Constraint violation: " + errors;
        log.warn("Constraint violation: {} - Path: {}", message, request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), "Correct the invalid parameters.");
    }


    @ExceptionHandler({InvalidPersonDataException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadDataExceptions(Exception ex, HttpServletRequest request) {
         log.warn("Bad request data: {} - Path: {}", ex.getMessage(), request.getRequestURI());
         return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), "Check input data format or consistency.");
    }


    @ExceptionHandler({PersonAlreadyExistsException.class, ResourceAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Conflict error: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), "Resource already exists or violates unique constraint.");
    }

    @ExceptionHandler(OperationNotAllowedException.class)
     public ResponseEntity<ErrorResponse> handleOperationNotAllowed(OperationNotAllowedException ex, HttpServletRequest request) {
         log.warn("Operation not allowed: {} - Path: {}", ex.getMessage(), request.getRequestURI());
         return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), "Operation cannot be performed due to business rules.");
     }

    // --- 401 Unauthorized ---
    // Usually handled by Spring Security filters before reaching controller/handler

    // --- 403 Forbidden ---
     @ExceptionHandler(AccessDeniedException.class)
     public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
         log.warn("Access denied: User does not have permission for {} - Path: {}", request.getMethod(), request.getRequestURI());
         return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to perform this action.", request.getRequestURI(), "Contact administrator if you believe this is an error.");
     }


    // --- 404 Not Found ---

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), "Verify the resource identifier.");
    }

    // --- 500 Internal Server Error ---

    @ExceptionHandler(GeocodingApiException.class)
    public ResponseEntity<ErrorResponse> handleGeocodingApiException(GeocodingApiException ex, HttpServletRequest request) {
        log.error("Geocoding API error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex.getCause());
        // Provide a more user-friendly message, details are logged
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process address geocoding.", request.getRequestURI(), "Address lookup failed. Try again later or check address validity.");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignStatusException(FeignException ex, HttpServletRequest request) {
        String message = String.format("Error communicating with external service: Status %d - %s", ex.status(), ex.getMessage());
        log.error("Feign client error: {} - Path: {} - Response Body: {}", message, request.getRequestURI(), ex.contentUTF8(), ex);
        HttpStatus status = HttpStatus.resolve(ex.status()) != null ? HttpStatus.resolve(ex.status()) : HttpStatus.INTERNAL_SERVER_ERROR;
         // Avoid exposing too much internal detail in the response
         String userMessage = "Failed to communicate with a required external service.";
         String suggestion = "Please try again later. If the problem persists, contact support.";
         if(status == HttpStatus.NOT_FOUND) {
            userMessage = "A required external resource was not found.";
            suggestion = "Please check related identifiers or contact support.";
         } else if (status.is4xxClientError()) {
             userMessage = "There was an issue with data sent to an external service.";
             suggestion = "Please check your input data or contact support.";
         }

        return buildErrorResponse(status, userMessage, request.getRequestURI(), suggestion);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // Log the full error including stack trace
        log.error("Unexpected internal server error - Path: {}", request.getRequestURI(), ex);
        // Return a generic error message to the client
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request.getRequestURI(), "Please try again later or contact support.");
    }


    // --- Helper Method ---

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path, String suggestion) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                suggestion
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}