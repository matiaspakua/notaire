package com.licensis.notaire.domain.history;

/** Invalid history write (empty 400). */
public class HistoryBadRequestException extends HistoryException {

    public HistoryBadRequestException() {
        super("Invalid history request");
    }
}
