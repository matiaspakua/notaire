package com.licensis.notaire.domain.procedure;

/** No procedure type exists for the requested id. */
public class ProcedureTypeNotFoundException extends ProcedureTypeException {

    public ProcedureTypeNotFoundException() {
        super("Procedure type not found");
    }
}
