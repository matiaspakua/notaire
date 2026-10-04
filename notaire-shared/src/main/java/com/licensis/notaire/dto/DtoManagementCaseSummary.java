package com.licensis.notaire.dto;

import java.util.List;

/** Post-signing case summary of a gestión: its escrituras, testimonios and copias. */
public record DtoManagementCaseSummary(
        Integer managementId,
        int managementNumber,
        String heading,
        List<DtoCaseDeed> deeds) {
}
