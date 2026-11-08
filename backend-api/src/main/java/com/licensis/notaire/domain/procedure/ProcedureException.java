package com.licensis.notaire.domain.procedure;

/** Base type for procedure domain failures mapped by {@code ProcedureExceptionHandler}. */
public class ProcedureException extends RuntimeException {

    protected ProcedureException(String message) {
        super(message);
    }
}
