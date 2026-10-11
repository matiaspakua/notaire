package com.licensis.notaire.exception;

/**
 * Thrown when the number proposed for a deed is already used by another
 * deed of the same protocol, year, and notary (CU86, exception 3.1).
 */
public class DuplicateDeedNumberException extends NotaireException {

    public DuplicateDeedNumberException(String message) {
        super(409, message);
    }
}
