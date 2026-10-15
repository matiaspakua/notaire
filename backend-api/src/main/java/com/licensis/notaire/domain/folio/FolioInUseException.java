package com.licensis.notaire.domain.folio;

/** Raised when a Utilizado folio cannot be changed or deleted (issue #1727). */
public class FolioInUseException extends RuntimeException {

    public FolioInUseException(String message) {
        super(message);
    }

    public static FolioInUseException cannotModify() {
        return new FolioInUseException("This folio is in use (Utilizado) and cannot be modified.");
    }

    public static FolioInUseException cannotDelete() {
        return new FolioInUseException("Cannot delete: the folio is in use (Utilizado).");
    }
}
