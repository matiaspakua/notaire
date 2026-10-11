package com.licensis.notaire.application.usecase.testimony;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates and verifies testimonies of a signed deed (CU07, CU08).
 */
@Service
@Transactional
public class TestimonyGenerationVerificationService {

    private static final Logger log = LoggerFactory.getLogger(TestimonyGenerationVerificationService.class);

    private final DeedRepository deedRepository;
    private final TestimonyRepository testimonyRepository;

    public TestimonyGenerationVerificationService(DeedRepository deedRepository,
            TestimonyRepository testimonyRepository) {
        this.deedRepository = deedRepository;
        this.testimonyRepository = testimonyRepository;
    }

    /**
     * Generates a testimony from a signed deed. The testimony number is system-assigned.
     *
     * @param idEscritura the deed ID
     * @return the generated testimony
     * @throws ResourceNotFoundException if not deed with the given ID exists
     * @throws BusinessValidationException if the deed is not "Firmada"
     */
    public Testimony generate(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + idDeed));

        if (!BusinessConstants.DeedFIRMADA.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "The deed must be in status 'Firmada' to generate its testimony");
        }

        Testimony testimony = new Testimony();
        testimony.setNumber((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        testimony.setFkIdDeed(deed);
        testimony.setVerified(false);
        testimony.setFlagged(false);

        log.info("Generating testimony for deed id: {}", idDeed);
        return testimonyRepository.save(testimony);
    }

    /**
     * Verifies a testimony, recording whether it was observed and why.
     *
     * @param idTestimony the testimony id
     * @param flagged whether the testimony was found to have observations
     * @param notes the reason, when flagged is true
     * @return the verified testimony
     * @throws ResourceNotFoundException if no testimony with the given id exists
     */
    public Testimony verify(Integer idTestimony, boolean flagged, String notes) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimony not found with ID: " + idTestimony));

        testimony.setVerified(true);
        testimony.setFlagged(flagged);
        testimony.setNotes(notes);

        log.info("Verificando testimony id: {}, observado: {}", idTestimony, flagged);
        return testimonyRepository.save(testimony);
    }
}
