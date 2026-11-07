package com.licensis.notaire.domain.procedure;

/** Hold rejected; clients receive {@code {"message": …}} with HTTP 400. */
public class ProcedureFolderBadRequestException extends ProcedureFolderException {

    public ProcedureFolderBadRequestException(String message) {
        super(message);
    }
}
