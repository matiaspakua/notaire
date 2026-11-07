package com.licensis.notaire.domain.procedure;

/** Create/update failed; mapped via {@code ErrorResponses}. */
public class ProcedureTypeOperationFailedException extends ProcedureTypeException {

    public enum Kind {
        CREATE,
        UPDATE
    }

    private final Kind kind;

    public ProcedureTypeOperationFailedException(Kind kind, Exception cause) {
        super(cause.getMessage(), cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public Exception causeAsException() {
        Throwable cause = getCause();
        return cause instanceof Exception exception ? exception : new RuntimeException(cause);
    }
}
