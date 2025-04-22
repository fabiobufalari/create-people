package com.bufalari.people.exception;

/**
 * Exceção lançada quando os dados fornecidos para uma pessoa são inválidos.
 * Exception thrown when the data provided for a person is invalid.
 */
public class InvalidPersonDataException extends RuntimeException {
    public InvalidPersonDataException(String message) {
        super(message);
    }
}
