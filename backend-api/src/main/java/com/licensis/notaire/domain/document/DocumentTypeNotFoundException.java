package com.licensis.notaire.domain.document;

public class DocumentTypeNotFoundException extends DocumentTypeException {

    public DocumentTypeNotFoundException(int id) {
        super("Document type not found with ID: " + id);
    }
}
