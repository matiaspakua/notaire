package com.licensis.notaire.domain.management;

/** The store refused to delete the status because other records still reference it. */
public class ManagementStatusReferencedException extends ManagementStatusInUseException {

    public ManagementStatusReferencedException(Throwable cause) {
        super(DELETE_MESSAGE, cause);
    }
}
