package com.licensis.notaire.domain.document;

/** Procedure type or document type missing (404 with {"message":…}). */
public class DocumentCostTemplateNotFoundException extends DocumentCostTemplateException {

    public DocumentCostTemplateNotFoundException(String message) {
        super(message);
    }
}
