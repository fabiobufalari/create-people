package com.bufalari.createpeople.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 * Exception thrown when a resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
