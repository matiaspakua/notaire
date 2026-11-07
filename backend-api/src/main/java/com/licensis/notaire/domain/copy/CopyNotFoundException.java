package com.licensis.notaire.domain.copy;

/** No copy exists for the requested id. */
public class CopyNotFoundException extends CopyException {

    public CopyNotFoundException() {
        super("Copy not found");
    }
}
