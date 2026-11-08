package com.licensis.notaire.domain.identification;

public class IdentificationTypeNameTakenException extends RuntimeException {

    public static final String MESSAGE = "An identification type with this name already exists";

    public IdentificationTypeNameTakenException() {
        super(MESSAGE);
    }
}
