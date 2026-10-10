package com.licensis.notaire.exception;

/**
 * Thrown when the number proposed for an deed leaves a gap in the correlativo
 * and not justificación was provided in notes (CU86, excepción 3.2).
 */
public class UnjustifiedNumberingGapException extends NotaireException {

    public UnjustifiedNumberingGapException(String message) {
        super(400, message);
    }
}
