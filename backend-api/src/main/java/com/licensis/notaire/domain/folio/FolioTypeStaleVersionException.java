package com.licensis.notaire.domain.folio;

/** The client edited a version that is no longer the stored one (optimistic locking, issue #655). */
public class FolioTypeStaleVersionException extends FolioTypeException {

    public FolioTypeStaleVersionException(int id, int stored, int received) {
        super("Folio type " + id + " is at version " + stored + ", not " + received);
    }
}
