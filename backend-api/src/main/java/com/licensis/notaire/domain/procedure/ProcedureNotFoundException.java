package com.licensis.notaire.domain.procedure;

/** Procedure or a referenced aggregate does not exist (empty 404). */
public class ProcedureNotFoundException extends ProcedureException {

    public ProcedureNotFoundException() {
        super("Procedure not found");
    }
}
