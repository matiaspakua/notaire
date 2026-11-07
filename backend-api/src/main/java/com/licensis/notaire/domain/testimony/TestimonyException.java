package com.licensis.notaire.domain.testimony;

/** Base type for testimony domain failures mapped by {@code TestimonyExceptionHandler}. */
public class TestimonyException extends RuntimeException {

    protected TestimonyException(String message) {
        super(message);
    }

    protected TestimonyException(String message, Throwable cause) {
        super(message, cause);
    }
}
