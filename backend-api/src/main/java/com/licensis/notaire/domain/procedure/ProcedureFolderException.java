package com.licensis.notaire.domain.procedure;

/** Base type for procedure-folder domain failures mapped by {@code ProcedureFolderExceptionHandler}. */
public class ProcedureFolderException extends RuntimeException {

    protected ProcedureFolderException(String message) {
        super(message);
    }
}
