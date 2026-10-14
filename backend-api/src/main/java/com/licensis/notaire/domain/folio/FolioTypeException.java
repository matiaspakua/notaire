package com.licensis.notaire.domain.folio;

/** Base of the folio type domain exceptions; none carries an HTTP status (ADR-030). */
public abstract class FolioTypeException extends RuntimeException {

    protected FolioTypeException(String message) {
        super(message);
    }

    protected FolioTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
