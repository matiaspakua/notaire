package com.licensis.notaire.exception;

/**
 * Thrown when a person is created or updated with an identification type +
 * number that already belongs to another person (CU17 / CU18 / #835 / #799).
 */
public class DuplicatePersonException extends NotaireException {

    private final Integer existingPersonId;

    public DuplicatePersonException(String message, Integer existingPersonId) {
        super(409, message);
        this.existingPersonId = existingPersonId;
    }

    public Integer getExistingPersonId() {
        return existingPersonId;
    }
}
