package com.bufalari.people.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an operation cannot be performed due to business rules
 * (e.g., deleting a group that still has members).
 * Maps to HTTP 400 Bad Request by default.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Or HttpStatus.CONFLICT depending on the context
public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
}