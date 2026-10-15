package com.licensis.notaire.domain.document;

/** Base of the document type domain exceptions; none carries an HTTP status (ADR-030). */
public abstract class DocumentTypeException extends RuntimeException {

    protected DocumentTypeException(String message) {
        super(message);
    }

    protected DocumentTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
