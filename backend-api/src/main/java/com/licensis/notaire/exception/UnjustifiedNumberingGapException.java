package com.licensis.notaire.exception;

/**
 * Thrown when the number proposed for a deed leaves a gap in the sequence
 * and no justification was provided in notes (CU86, exception 3.2).
 */
public class UnjustifiedNumberingGapException extends NotaireException {

    public UnjustifiedNumberingGapException(String message) {
        super(400, message);
    }
}
