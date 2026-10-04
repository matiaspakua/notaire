package com.licensis.notaire.dto;

import java.util.List;

/** Case summary of a gestión: its escrituras, testimonios and copias, and the documents of its trámites. */
public record DtoManagementCaseSummary(
        Integer managementId,
        int managementNumber,
        String heading,
        List<DtoCaseDeed> deeds,
        List<DtoCaseDocument> documents) {
}
