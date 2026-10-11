package com.licensis.notaire.jpa.exceptions;

public class ClassDeletedException extends Exception {
    public ClassDeletedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassDeletedException(String message) {
        super(message);
    }

    public ClassDeletedException() {
    }
}
