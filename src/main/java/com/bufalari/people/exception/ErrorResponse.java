package com.bufalari.people.exception;

import com.fasterxml.jackson.annotation.JsonFormat; // For consistent date format
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter; // Use Lombok getters

import java.time.LocalDateTime;

/**
 * Standardized error response format for the API.
 */
@Getter // Use Lombok Getters instead of manual ones
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields (like suggestion) in JSON
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // ISO 8601 format
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String suggestion; // Optional suggestion for the user

    // Constructor remains the same
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String suggestion) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.suggestion = suggestion;
    }

    // Convenience constructor without suggestion
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}