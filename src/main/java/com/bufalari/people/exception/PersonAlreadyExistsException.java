package com.bufalari.people.exception;

/**
 * Exceção lançada quando uma pessoa já existe com os mesmos dados únicos.
 * Exception thrown when a person already exists with the same unique data.
 */
public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(String message) {
        super(message);
    }
}
