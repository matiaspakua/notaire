package com.licensis.notaire.domain.registrationdraft;

/** Write rejected; clients receive {@code {"message": …}} with HTTP 400. */
public class RegistrationDraftBadRequestException extends RegistrationDraftException {

    public RegistrationDraftBadRequestException(String message) {
        super(message);
    }
}
