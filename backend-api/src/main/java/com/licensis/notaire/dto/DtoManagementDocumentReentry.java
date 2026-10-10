package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - Read-model for elegir qué reenter: the procedures of a management
 * together with the documentation required of each uno.
 */
public record DtoManagementDocumentReentry(
        Integer idManagement,
        int number,
        String encabezado,
        List<DtoProcedureRequiredDocumentation> procedures) {
}
