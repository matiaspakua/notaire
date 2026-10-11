package com.licensis.notaire.dto;

import java.util.Date;
import java.util.List;

/**
 * CU10 - Read-model for the management detail screen (step 4 of the course of
 * events): management number, heading, start date, notary in
 * charge, cadastral designation (if the management involves a property) and the
 * documentation that must be presented by external entities.
 */
public record DtoManagementExternalEntityDocuments(
        Integer idManagement,
        int number,
        String heading,
        Date dateStart,
        String notary,
        String cadastralDesignation,
        List<DtoExternalEntityDocument> documents) {
}
