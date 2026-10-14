package com.licensis.notaire.domain.identification;

public class IdentificationTypeNotFoundException extends RuntimeException {

    public IdentificationTypeNotFoundException(int id) {
        super("Identification type not found with ID: " + id);
    }
}
