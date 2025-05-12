package com.bufalari.people.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource; // Importar MessageSource
import org.springframework.context.NoSuchMessageException; // Importar exceção
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Locale; // Importar Locale
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API.
 * Catches specific exceptions and formats them into a standardized ErrorResponse.
 */
@RestControllerAdvice // Centraliza o tratamento de exceções para REST Controllers
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource; // Injetado para i18n

    // Injeção via construtor
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // --- 400 Bad Request Handlers ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request, Locale locale) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> String.format("'%s': %s", error.getField(), getMessage(error, locale))) // Usa mensagem i18n
                .collect(Collectors.joining("; "));
        String message = getMessage("error.validation.failed", locale, "Validation failed") + ": " + errors;
        log.warn("Validation error: {} - Path: {}", message, request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), getMessage("suggestion.validation", locale, "Correct the invalid fields."), locale);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request, Locale locale) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage) // Usa a mensagem da anotação de validação
                .collect(Collectors.joining("; "));
        String message = getMessage("error.constraint.violation", locale, "Constraint violation") + ": " + errors;
        log.warn("Constraint violation: {} - Path: {}", message, request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), getMessage("suggestion.constraint", locale, "Correct the invalid parameters."), locale);
    }

    @ExceptionHandler({InvalidPersonDataException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class}) // Adiciona IllegalArgumentException
    public ResponseEntity<ErrorResponse> handleBadDataExceptions(Exception ex, HttpServletRequest request, Locale locale) {
        String message = getMessage("error.bad.request", locale, "Bad request data") + ": " + ex.getMessage();
        log.warn("Bad request data: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), getMessage("suggestion.bad.request", locale, "Check input data format or consistency."), locale);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleOperationNotAllowed(OperationNotAllowedException ex, HttpServletRequest request, Locale locale) {
        String message = getMessage("error.operation.notAllowed", locale, "Operation not allowed") + ": " + ex.getMessage();
        log.warn("Operation not allowed: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        // Usar CONFLICT (409) pode ser mais apropriado se a operação falha devido ao estado atual do recurso
        return buildErrorResponse(HttpStatus.CONFLICT, message, request.getRequestURI(), getMessage("suggestion.operation.notAllowed", locale, "Operation cannot be performed due to business rules or resource state."), locale);
    }

    // --- 401 Unauthorized ---
    // Geralmente tratado pelo Spring Security antes de chegar aqui. Se chegar, pode ser um erro de config.
    // @ExceptionHandler(AuthenticationException.class) ...

    // --- 403 Forbidden ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, Locale locale) {
        String message = getMessage("error.access.denied", locale, "Access Denied: You do not have permission to perform this action.");
        log.warn("Access denied: User lacks permission for {} on path {} - Original message: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, message, request.getRequestURI(), getMessage("suggestion.access.denied", locale, "Contact administrator if you believe this is an error."), locale);
    }

    // --- 404 Not Found ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request, Locale locale) {
        String message = getMessage("error.resource.notFound", locale, "Resource not found") + ": " + ex.getMessage();
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request.getRequestURI(), getMessage("suggestion.resource.notFound", locale, "Verify the resource identifier."), locale);
    }

    // --- 409 Conflict ---
    @ExceptionHandler({PersonAlreadyExistsException.class, ResourceAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex, HttpServletRequest request, Locale locale) {
        String message = getMessage("error.resource.conflict", locale, "Resource conflict") + ": " + ex.getMessage();
        log.warn("Conflict error: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.CONFLICT, message, request.getRequestURI(), getMessage("suggestion.resource.conflict", locale, "Resource already exists or violates unique constraint."), locale);
    }

    // --- 5xx Server Errors ---

    @ExceptionHandler(GeocodingApiException.class)
    public ResponseEntity<ErrorResponse> handleGeocodingApiException(GeocodingApiException ex, HttpServletRequest request, Locale locale) {
        // Logar causa raiz se existir
        log.error("Geocoding API error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex.getCause());
        String message = getMessage("error.geocoding.failed", locale, "Failed to process address geocoding.");
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, message, request.getRequestURI(), getMessage("suggestion.geocoding.failed", locale, "Address lookup service unavailable. Try again later or check address validity."), locale);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignStatusException(FeignException ex, HttpServletRequest request, Locale locale) {
        String errorDetails = String.format("Status %d attempting to access %s", ex.status(), request.getRequestURI()); // Simplificado
        log.error("Feign client error: {} - Path: {} - Response Body: {}", errorDetails, request.getRequestURI(), ex.contentUTF8(), ex);

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // Default se status não for reconhecido
        }

        String userMessageKey;
        String suggestionKey;

        if (status == HttpStatus.NOT_FOUND) {
            userMessageKey = "error.feign.notFound";
            suggestionKey = "suggestion.feign.notFound";
        } else if (status.is4xxClientError()) {
            userMessageKey = "error.feign.clientError";
            suggestionKey = "suggestion.feign.clientError";
        } else { // 5xx ou desconhecido
            userMessageKey = "error.feign.serverError";
            suggestionKey = "suggestion.feign.serverError";
        }

        String message = getMessage(userMessageKey, locale, "Failed to communicate with a required external service.");
        String suggestion = getMessage(suggestionKey, locale, "Please try again later. If the problem persists, contact support.");

        return buildErrorResponse(status, message, request.getRequestURI(), suggestion, locale);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request, Locale locale) {
        log.error("Unexpected internal server error - Path: {}", request.getRequestURI(), ex);
        String message = getMessage("error.generic", locale, "An unexpected internal error occurred.");
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI(), getMessage("suggestion.generic", locale, "Please try again later or contact support."), locale);
    }


    // --- Helper Methods ---

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path, String suggestion, Locale locale) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(), // Usa a frase padrão do status HTTP
                message, // Mensagem específica (potencialmente i18n)
                path,
                suggestion // Sugestão (potencialmente i18n)
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // Helper para obter mensagem i18n com fallback
    private String getMessage(String code, Locale locale, String defaultMessage) {
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            return defaultMessage; // Retorna default se a chave não existir
        }
    }

    // Helper para obter mensagem i18n para erros de validação
    private String getMessage(FieldError error, Locale locale) {
        // Tenta resolver a mensagem usando os códigos do FieldError
        // O Spring geralmente gera códigos como: NotBlank.personDTO.name, NotBlank.name, NotBlank.java.lang.String, NotBlank
        // O messageSource tentará encontrar a chave mais específica primeiro.
        return messageSource.getMessage(error, locale);
    }
}