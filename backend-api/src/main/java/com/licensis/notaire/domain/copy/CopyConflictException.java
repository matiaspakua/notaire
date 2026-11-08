package com.licensis.notaire.domain.copy;

/** Copy create/update conflicts with existing data. */
public class CopyConflictException extends CopyException {

    public CopyConflictException(String message) {
        super(message);
    }
}
