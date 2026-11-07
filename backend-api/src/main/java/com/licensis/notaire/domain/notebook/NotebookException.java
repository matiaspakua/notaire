package com.licensis.notaire.domain.notebook;

/** Base type for notebook domain failures mapped by {@code NotebookExceptionHandler}. */
public class NotebookException extends RuntimeException {

    protected NotebookException(String message) {
        super(message);
    }
}
