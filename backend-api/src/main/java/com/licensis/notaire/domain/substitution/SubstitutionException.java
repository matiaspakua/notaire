package com.licensis.notaire.domain.substitution;

/** Base type for substitution domain failures mapped by {@code SubstitutionExceptionHandler}. */
public class SubstitutionException extends RuntimeException {

    protected SubstitutionException(String message) {
        super(message);
    }
}
