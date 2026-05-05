package com.licensis.notaire.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends NotaireException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
