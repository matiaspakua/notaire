package com.licensis.notaire.domain.identification;

public class IdentificationTypeInUseException extends RuntimeException {

    public static final String MESSAGE = "Cannot delete: the identification type is in use by people";

    public IdentificationTypeInUseException() {
        super(MESSAGE);
    }

    public IdentificationTypeInUseException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
