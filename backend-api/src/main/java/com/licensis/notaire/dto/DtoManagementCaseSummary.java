package com.licensis.notaire.dto;

import java.util.List;

/** Case summary of a management: its deeds, testimonies and copies, and the documents of its procedures. */
public record DtoManagementCaseSummary(
        Integer managementId,
        int managementNumber,
        String heading,
        List<DtoCaseDeed> deeds,
        List<DtoCaseDocument> documents) {
}
