package com.licensis.notaire.domain.management;

/** Base of management-status domain exceptions; none carries an HTTP status (ADR-030). */
public abstract class ManagementStatusException extends RuntimeException {

    protected ManagementStatusException(String message) {
        super(message);
    }

    protected ManagementStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
