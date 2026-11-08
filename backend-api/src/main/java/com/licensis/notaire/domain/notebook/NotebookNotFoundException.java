package com.licensis.notaire.domain.notebook;

/** Notary or folio missing for notebook create (404 with {"message":…}). */
public class NotebookNotFoundException extends NotebookException {

    public NotebookNotFoundException(String message) {
        super(message);
    }
}
