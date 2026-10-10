package com.licensis.notaire.exception;

/**
 * Thrown when the number proposed for an deed is already used by another
 * deed of the same protocolo, year and notary (CU86, excepción 3.1).
 */
public class DuplicateDeedNumberException extends NotaireException {

    public DuplicateDeedNumberException(String message) {
        super(409, message);
    }
}
