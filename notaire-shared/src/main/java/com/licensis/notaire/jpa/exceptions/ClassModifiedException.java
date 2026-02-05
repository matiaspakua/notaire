package com.licensis.notaire.jpa.exceptions;

public class ClassModifiedException extends Exception {
    public ClassModifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassModifiedException(String message) {
        super(message);
    }

    public ClassModifiedException() {
    }
}
