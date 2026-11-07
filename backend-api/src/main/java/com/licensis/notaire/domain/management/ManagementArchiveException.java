package com.licensis.notaire.domain.management;

/** Base type for archive failures mapped by {@code ManagementArchiveExceptionHandler}. */
public class ManagementArchiveException extends RuntimeException {

    protected ManagementArchiveException(String message) {
        super(message);
    }
}
