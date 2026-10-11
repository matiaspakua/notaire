package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - A procedure of the management together with the documentation required by
 * its {@code ProcedureTemplate}, used to choose which document type to re-enter.
 */
public record DtoProcedureRequiredDocumentation(
        Integer idProcedure,
        String typeProcedureName,
        List<DtoRequiredDocument> requiredDocuments) {
}
