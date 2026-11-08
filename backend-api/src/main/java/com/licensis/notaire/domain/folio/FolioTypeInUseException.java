package com.licensis.notaire.domain.folio;

/** A folio type that folios use is locked: it can be neither edited nor deleted. */
public class FolioTypeInUseException extends FolioTypeException {

    public static final String LOCKED_MESSAGE = "This folio type is in use and cannot be modified. Create a new one.";
    public static final String DELETE_MESSAGE = "Cannot delete: the folio type is in use by folios.";

    protected FolioTypeInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    private FolioTypeInUseException(String message) {
        super(message);
    }

    public static FolioTypeInUseException lockedForEdit() {
        return new FolioTypeInUseException(LOCKED_MESSAGE);
    }

    public static FolioTypeInUseException cannotDelete() {
        return new FolioTypeInUseException(DELETE_MESSAGE);
    }
}
