package com.licensis.notaire.domain.concept;

/**
 * A create/update/delete failed with an underlying cause that the web layer maps via
 * {@code ErrorResponses} (constraint → 400/409, otherwise create 409 / update 500 / delete 409).
 */
public class ConceptOperationFailedException extends ConceptException {

    public enum Operation {
        CREATE,
        UPDATE,
        DELETE
    }

    private final Operation operation;

    public ConceptOperationFailedException(Operation operation, Exception cause) {
        super(cause.getMessage(), cause);
        this.operation = operation;
    }

    public Operation operation() {
        return operation;
    }

    public Exception causeAsException() {
        Throwable cause = getCause();
        return cause instanceof Exception exception ? exception : new RuntimeException(cause);
    }
}
