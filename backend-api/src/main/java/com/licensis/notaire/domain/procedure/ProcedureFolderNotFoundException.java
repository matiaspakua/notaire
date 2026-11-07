package com.licensis.notaire.domain.procedure;

/** No procedure folder exists for the requested id. */
public class ProcedureFolderNotFoundException extends ProcedureFolderException {

    public ProcedureFolderNotFoundException() {
        super("Procedure folder not found");
    }
}
