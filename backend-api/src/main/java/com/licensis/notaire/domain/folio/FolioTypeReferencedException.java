package com.licensis.notaire.domain.folio;

/** The store refused to delete the type because other records still reference it. */
public class FolioTypeReferencedException extends FolioTypeInUseException {

    public static final String MESSAGE = "Cannot delete: the folio type is referenced by other records.";

    public FolioTypeReferencedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
