package com.licensis.notaire.domain.procedure;

/** Procedure type is locked because another aggregate references it. */
public class ProcedureTypeInUseException extends ProcedureTypeException {

    public ProcedureTypeInUseException(String message) {
        super(message);
    }
}
