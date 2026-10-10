package com.licensis.notaire.dto;

import java.util.Date;
import java.util.List;

/**
 * CU10 - Read-model for the management detail screen (step 4 of the Curso of
 * Eventos): number of management, encabezado, date of inicio, notary a
 * cargo, cadastral nomenclature (if the management involucra a Property) and the
 * documentation that must be presented by entidades externas.
 */
public record DtoManagementExternalEntityDocuments(
        Integer idManagement,
        int number,
        String encabezado,
        Date dateStart,
        String notary,
        String cadastralDesignation,
        List<DtoExternalEntityDocument> documents) {
}
