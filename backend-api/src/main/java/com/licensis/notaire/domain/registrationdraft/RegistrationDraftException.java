package com.licensis.notaire.domain.registrationdraft;

/** Base type for registration-draft failures mapped by {@code RegistrationDraftExceptionHandler}. */
public class RegistrationDraftException extends RuntimeException {

    protected RegistrationDraftException(String message) {
        super(message);
    }
}
