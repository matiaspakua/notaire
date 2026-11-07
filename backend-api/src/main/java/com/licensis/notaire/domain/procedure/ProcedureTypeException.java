package com.licensis.notaire.domain.procedure;

/** Base type for procedure-type domain failures mapped by {@code ProcedureTypeExceptionHandler}. */
public class ProcedureTypeException extends RuntimeException {

    protected ProcedureTypeException(String message) {
        super(message);
    }

    protected ProcedureTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
