package com.licensis.notaire.domain.document;

/** The store refused to delete the type because other records still reference it. */
public class DocumentTypeReferencedException extends DocumentTypeInUseException {

    public DocumentTypeReferencedException(Throwable cause) {
        super(DELETE_MESSAGE, cause);
    }
}
