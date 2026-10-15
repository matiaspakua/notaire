package com.licensis.notaire.domain.management;

/** A status that deed managements reference is locked: it can be neither edited nor deleted. */
public class ManagementStatusInUseException extends ManagementStatusException {

    public static final String LOCKED_MESSAGE = "Cannot update: the status is referenced by deed managements.";
    public static final String DELETE_MESSAGE = "Cannot delete: the status is referenced by deed managements.";

    protected ManagementStatusInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    private ManagementStatusInUseException(String message) {
        super(message);
    }

    public static ManagementStatusInUseException lockedForEdit() {
        return new ManagementStatusInUseException(LOCKED_MESSAGE);
    }

    public static ManagementStatusInUseException cannotDelete() {
        return new ManagementStatusInUseException(DELETE_MESSAGE);
    }
}
