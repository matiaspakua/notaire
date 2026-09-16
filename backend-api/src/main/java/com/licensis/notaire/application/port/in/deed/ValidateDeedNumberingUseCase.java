package com.licensis.notaire.application.port.in.deed;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.domain.deed.DeedNumberingValidationResult;

/**
 * Inbound port for validating a proposed deed number.
 * Orchestrates the business rules for deed numbering (CU86).
 */
public interface ValidateDeedNumberingUseCase {

    /**
     * Validate a proposed deed number against numbering rules.
     *
     * @param number the proposed deed number
     * @param notary the notary person (contains personId)
     * @param year the year
     * @param isAuxiliary true if for auxiliary protocol, false for principal
     * @param skipJustification optional justification for skipping sequential numbers
     * @param idDeedExclude deed ID to exclude from validation (e.g., when updating), or null
     * @return validation result indicating if the number is acceptable
     */
    DeedNumberingValidationResult validate(
            int number, Person notary, int year, boolean isAuxiliary,
            String skipJustification, Integer idDeedExclude);
}
