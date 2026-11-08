package com.licensis.notaire.domain.registrationdraft;

/** No registration draft or deed exists for the requested id. */
public class RegistrationDraftNotFoundException extends RegistrationDraftException {

    public RegistrationDraftNotFoundException() {
        super("Registration draft not found");
    }
}
