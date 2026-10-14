package com.licensis.notaire.domain.folio;

public class FolioTypeNotFoundException extends FolioTypeException {

    public FolioTypeNotFoundException(int id) {
        super("Folio type not found with ID: " + id);
    }
}
