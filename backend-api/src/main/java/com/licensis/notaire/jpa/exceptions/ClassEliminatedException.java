package com.licensis.notaire.jpa.exceptions;

public class ClassEliminatedException extends Exception {
    public ClassEliminatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassEliminatedException(String message) {
        super(message);
    }

    public ClassEliminatedException() {
    }
}
