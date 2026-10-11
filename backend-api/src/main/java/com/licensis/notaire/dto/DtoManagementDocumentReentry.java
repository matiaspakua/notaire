package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - Read model for choosing what to re-enter: the procedures of a management
 * together with the documentation each one requires.
 */
public record DtoManagementDocumentReentry(
        Integer idManagement,
        int number,
        String heading,
        List<DtoProcedureRequiredDocumentation> procedures) {
}
