package com.licensis.notaire.exception;

/**
 * Base exception for application errors
 */
public class NotaireException extends RuntimeException {
    private final int statusCode;

    public NotaireException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public NotaireException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public NotaireException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public NotaireException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
