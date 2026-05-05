package com.licensis.notaire.exception;

/**
 * Exception thrown when business logic validation fails
 */
public class BusinessValidationException extends NotaireException {
    public BusinessValidationException(String message) {
        super(400, message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
