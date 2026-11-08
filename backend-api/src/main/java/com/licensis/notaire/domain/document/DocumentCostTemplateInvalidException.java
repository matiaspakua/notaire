package com.licensis.notaire.domain.document;

/** Invalid document-cost create input (400 with {"message":…}). */
public class DocumentCostTemplateInvalidException extends DocumentCostTemplateException {

    public DocumentCostTemplateInvalidException(String message) {
        super(message);
    }
}
