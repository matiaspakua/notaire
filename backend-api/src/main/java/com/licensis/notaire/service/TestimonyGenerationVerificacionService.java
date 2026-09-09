package com.licensis.notaire.service;

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
 * Generates and verifies testimonios of a signed escritura (CU07, CU08).
 */
@Service
@Transactional
public class TestimonyGenerationVerificacionService {

    private static final Logger log = LoggerFactory.getLogger(TestimonyGenerationVerificacionService.class);

    private final DeedRepository deedRepository;
    private final TestimonyRepository testimonyRepository;

    public TestimonyGenerationVerificacionService(DeedRepository deedRepository,
            TestimonyRepository testimonyRepository) {
        this.deedRepository = deedRepository;
        this.testimonyRepository = testimonyRepository;
    }

    /**
     * Generates a testimonio from a signed escritura. The testimonio number is system-assigned.
     *
     * @param idEscritura the escritura ID
     * @return the generated testimonio
     * @throws ResourceNotFoundException if no escritura with the given ID exists
     * @throws BusinessValidationException if the escritura is not "Firmada"
     */
    public Testimony generar(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Escritura no encontrada con ID: " + idDeed));

        if (!BusinessConstants.DeedFIRMADA.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "La escritura debe estar en estado 'Firmada' para generar su testimonio");
        }

        Testimony testimony = new Testimony();
        testimony.setNumber((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        testimony.setFkIdDeed(deed);
        testimony.setVerified(false);
        testimony.setFlagged(false);

        log.info("Generando testimonio para escritura id: {}", idDeed);
        return testimonyRepository.save(testimony);
    }

    /**
     * Verifies a testimonio, recording whether it was observed and why.
     *
     * @param idTestimonio the testimonio ID
     * @param observado whether the testimonio was found to have observations
     * @param observaciones the reason, when observado is true
     * @return the verified testimonio
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     */
    public Testimony verificar(Integer idTestimony, boolean flagged, String notes) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimony));

        testimony.setVerified(true);
        testimony.setFlagged(flagged);
        testimony.setNotes(notes);

        log.info("Verificando testimonio id: {}, observado: {}", idTestimony, flagged);
        return testimonyRepository.save(testimony);
    }
}
