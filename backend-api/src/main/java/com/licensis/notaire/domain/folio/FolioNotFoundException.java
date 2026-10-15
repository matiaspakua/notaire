package com.licensis.notaire.domain.folio;

public class FolioNotFoundException extends RuntimeException {

    public FolioNotFoundException(int id) {
        super("Folio not found: " + id);
    }
}
