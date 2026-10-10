package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - A procedure of the management together with the documentation required according to
 * its {@code ProcedureTemplate}, usada for elegir qué document type
 * reingresar.
 */
public record DtoProcedureRequiredDocumentation(
        Integer idProcedure,
        String typeProcedureName,
        List<DtoRequiredDocument> documentsNecesarios) {
}
