package com.licensis.notaire.domain.concept;

/** Base type for concept domain failures mapped by {@code ConceptExceptionHandler}. */
public class ConceptException extends RuntimeException {

    protected ConceptException(String message) {
        super(message);
    }

    protected ConceptException(String message, Throwable cause) {
        super(message, cause);
    }
}
