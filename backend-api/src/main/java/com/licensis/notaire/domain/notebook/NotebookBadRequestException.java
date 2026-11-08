package com.licensis.notaire.domain.notebook;

/** Invalid notebook create input (400 with {"message":…}). */
public class NotebookBadRequestException extends NotebookException {

    public NotebookBadRequestException(String message) {
        super(message);
    }
}
