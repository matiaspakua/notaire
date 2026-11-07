package com.licensis.notaire.domain.testimony;

/**
 * A create/update failed with an underlying cause that the web layer maps via
 * {@code ErrorResponses} (constraint → 400/409, otherwise create 409 / update 500).
 */
public class TestimonyOperationFailedException extends TestimonyException {

    public enum Operation {
        CREATE,
        UPDATE
    }

    private final Operation operation;

    public TestimonyOperationFailedException(Operation operation, Exception cause) {
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
