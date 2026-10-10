package com.licensis.notaire.application.usecase.deed;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates and applies the "Firmar deed" business action (CU06).
 */
@Service
@Transactional
public class DeedSigningService {

    private static final Logger log = LoggerFactory.getLogger(DeedSigningService.class);

    private final DeedRepository deedRepository;
    private final FolioRepository folioRepository;

    public DeedSigningService(DeedRepository deedRepository, FolioRepository folioRepository) {
        this.deedRepository = deedRepository;
        this.folioRepository = folioRepository;
    }

    /**
     * Signs an deed: requires it to be "Sin Firmar" and have at least one folio assigned.
     *
     * @param idEscritura the deed ID
     * @return the signed deed
     * @throws ResourceNotFoundException if not deed with the given ID exists
     * @throws BusinessValidationException if the deed is not "Sin Firmar" or has not folio assigned
     */
    public Deed sign(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Deed no encontrada con ID: " + idDeed));

        if (!BusinessConstants.DeedSINFIRMAR.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "The deed must be in status 'Sin Firmar' for poder firmarse");
        }

        if (!folioRepository.existsByFkIdDeedIdDeed(idDeed)) {
            throw new BusinessValidationException(
                    "The deed must tener to the menos a folio assigned for poder firmarse");
        }

        deed.setStatus(BusinessConstants.DeedFIRMADA);
        log.info("Firmando deed id: {}", idDeed);
        return deedRepository.save(deed);
    }
}
