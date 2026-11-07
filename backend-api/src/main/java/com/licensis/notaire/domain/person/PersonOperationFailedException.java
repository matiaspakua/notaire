package com.licensis.notaire.domain.person;

/** Create/update failed; mapped via {@code ErrorResponses.createFailed}. */
public class PersonOperationFailedException extends PersonException {

    public PersonOperationFailedException(Exception cause) {
        super(cause.getMessage(), cause);
    }

    public Exception causeAsException() {
        Throwable cause = getCause();
        return cause instanceof Exception exception ? exception : new RuntimeException(cause);
    }
}
