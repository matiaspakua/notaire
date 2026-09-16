package com.licensis.notaire.application.usecase.deed;

import com.licensis.notaire.application.port.in.deed.CalculateNextDeedNumberUseCase;
import com.licensis.notaire.application.port.in.deed.ValidateDeedNumberingUseCase;
import com.licensis.notaire.application.port.out.deed.DeedNumberingPort;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.domain.deed.DeedNumberingValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deed numbering use case implementation (CU86).
 * Orchestrates business rules for validating and calculating correlativo
 * for escrituras by notary, year, and protocol type.
 */
@Service
@Transactional(readOnly = true)
public class DeedNumberingUseCaseImpl implements CalculateNextDeedNumberUseCase,
        ValidateDeedNumberingUseCase {

    private final DeedNumberingPort deedNumberingPort;

    public DeedNumberingUseCaseImpl(DeedNumberingPort deedNumberingPort) {
        this.deedNumberingPort = deedNumberingPort;
    }

    @Override
    public int calculateNextSequenceNumber(Person notary, int year, boolean isAuxiliary) {
        return deedNumberingPort.findMaxNumberDeedByNotaryYearAndType(
                notary.getPersonId(), year, isAuxiliary, null).orElse(0) + 1;
    }

    @Override
    public DeedNumberingValidationResult validate(
            int number, Person notary, int year, boolean isAuxiliary,
            String skipJustification, Integer idDeedExclude) {
        boolean duplicate = deedNumberingPort.existsNumberDeedByNotaryYearAndType(
                number, notary.getPersonId(), year, isAuxiliary, idDeedExclude);
        if (duplicate) {
            return DeedNumberingValidationResult.DUPLICATE;
        }

        int nextExpected = deedNumberingPort.findMaxNumberDeedByNotaryYearAndType(
                notary.getPersonId(), year, isAuxiliary, idDeedExclude).orElse(0) + 1;
        if (number == nextExpected) {
            return DeedNumberingValidationResult.OK;
        }

        boolean hasJustification = skipJustification != null && !skipJustification.isBlank();
        return hasJustification
                ? DeedNumberingValidationResult.SKIP_JUSTIFIED
                : DeedNumberingValidationResult.SKIP_UNJUSTIFIED;
    }
}
