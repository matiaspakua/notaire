package com.licensis.notaire.domain.property;

import java.math.BigDecimal;

/** The descriptive fields of a property; a full update replaces all of them (issue #655). */
public record PropertyDetails(
        String cadastralDesignation,
        BigDecimal fiscalAppraisal,
        String address,
        String notes,
        String registrationNumber,
        String volumeFolioLandRecord,
        String boundaries) {
}
