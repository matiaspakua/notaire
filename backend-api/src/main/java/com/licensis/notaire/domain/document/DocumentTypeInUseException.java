package com.licensis.notaire.domain.document;

/**
 * A document type that procedure templates or submitted documents use is locked: it can be neither edited nor
 * deleted.
 */
public class DocumentTypeInUseException extends DocumentTypeException {

    public static final String LOCKED_MESSAGE =
            "This document type is in use and cannot be modified. Create a new one.";
    public static final String DELETE_MESSAGE =
            "Cannot delete: the document type is used by procedure templates or submitted documents.";

    protected DocumentTypeInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    private DocumentTypeInUseException(String message) {
        super(message);
    }

    public static DocumentTypeInUseException lockedForEdit() {
        return new DocumentTypeInUseException(LOCKED_MESSAGE);
    }

    public static DocumentTypeInUseException cannotDelete() {
        return new DocumentTypeInUseException(DELETE_MESSAGE);
    }
}
