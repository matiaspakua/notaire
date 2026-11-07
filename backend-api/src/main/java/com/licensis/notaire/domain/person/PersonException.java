package com.licensis.notaire.domain.person;

/** Base type for person domain failures mapped by {@code PersonExceptionHandler}. */
public class PersonException extends RuntimeException {

    protected PersonException(String message) {
        super(message);
    }

    protected PersonException(String message, Throwable cause) {
        super(message, cause);
    }
}
