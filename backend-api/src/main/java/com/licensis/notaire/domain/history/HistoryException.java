package com.licensis.notaire.domain.history;

/** Base type for history domain failures mapped by {@code HistoryExceptionHandler}. */
public class HistoryException extends RuntimeException {

    protected HistoryException(String message) {
        super(message);
    }
}
