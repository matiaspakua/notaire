package com.licensis.notaire.domain.folio;

/** Raised when folio associations are missing or invalid (maps to HTTP 400). */
public class FolioInvalidException extends RuntimeException {

    public FolioInvalidException(String message) {
        super(message);
    }
}
