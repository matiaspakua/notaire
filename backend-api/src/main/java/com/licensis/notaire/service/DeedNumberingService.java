package com.licensis.notaire.service;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controla la numeración correlativa de escrituras por protocolo, año y escribano (CU86).
 */
@Service
@Transactional(readOnly = true)
public class DeedNumberingService {

    private final FolioRepository folioRepository;

    public DeedNumberingService(FolioRepository folioRepository) {
        this.folioRepository = folioRepository;
    }

    public int calculateNextSequenceNumber(Person notary, int year, boolean isAuxiliary) {
        return folioRepository.findMaxNumberDeedByNotaryYearAndType(
                notary.getPersonId(), year, isAuxiliary, null).orElse(0) + 1;
    }

    public NumberingValidationResult validate(int number, Person notary, int year, boolean isAuxiliary,
            String skipJustification, Integer idDeedExclude) {
        boolean duplicate = folioRepository.existsNumberDeedByNotaryYearAndType(
                number, notary.getPersonId(), year, isAuxiliary, idDeedExclude);
        if (duplicate) {
            return NumberingValidationResult.DUPLICATE;
        }

        int nextExpected = folioRepository.findMaxNumberDeedByNotaryYearAndType(
                notary.getPersonId(), year, isAuxiliary, idDeedExclude).orElse(0) + 1;
        if (number == nextExpected) {
            return NumberingValidationResult.OK;
        }

        boolean hasJustification = skipJustification != null && !skipJustification.isBlank();
        return hasJustification
                ? NumberingValidationResult.SKIP_JUSTIFIED
                : NumberingValidationResult.SKIP_UNJUSTIFIED;
    }
}
