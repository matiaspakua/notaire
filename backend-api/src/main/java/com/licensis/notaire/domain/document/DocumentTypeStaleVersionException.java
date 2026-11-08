package com.licensis.notaire.domain.document;

/** The client edited a version that is no longer the stored one (optimistic locking). */
public class DocumentTypeStaleVersionException extends DocumentTypeException {

    public DocumentTypeStaleVersionException(int id, int stored, int received) {
        super("Document type " + id + " is at version " + stored + ", not " + received);
    }
}
