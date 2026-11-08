package com.licensis.notaire.domain.history;

/** History or related aggregate not found (empty 404). */
public class HistoryNotFoundException extends HistoryException {

    public HistoryNotFoundException() {
        super("History not found");
    }
}
