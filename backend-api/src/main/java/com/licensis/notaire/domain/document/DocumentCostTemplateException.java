package com.licensis.notaire.domain.document;

/** Base type for document-cost-template failures mapped by {@code DocumentCostTemplateExceptionHandler}. */
public class DocumentCostTemplateException extends RuntimeException {

    protected DocumentCostTemplateException(String message) {
        super(message);
    }
}
