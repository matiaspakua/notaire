package com.licensis.notaire.exception;

/**
 * Thrown when the número proposed for an escritura leaves a gap in the correlativo
 * and no justificación was provided in observaciones (CU86, excepción 3.2).
 */
public class UnjustifiedNumberingGapException extends NotaireException {

    public UnjustifiedNumberingGapException(String message) {
        super(400, message);
    }
}
