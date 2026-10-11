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
 * Validates and applies the "Sign a deed" business action (CU06).
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
     * Signs a deed: it must be "Sin Firmar" and have at least one folio assigned.
     *
     * @param idDeed the deed id
     * @return the signed deed
     * @throws ResourceNotFoundException if no deed with the given id exists
     * @throws BusinessValidationException if the deed is not "Sin Firmar" or has no folio assigned
     */
    public Deed sign(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + idDeed));

        if (!BusinessConstants.DeedUnsigned.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "The deed must be in status 'Sin Firmar' to be signed");
        }

        if (!folioRepository.existsByFkIdDeedIdDeed(idDeed)) {
            throw new BusinessValidationException(
                    "The deed must have at least one folio assigned to be signed");
        }

        deed.setStatus(BusinessConstants.DeedSigned);
        log.info("Signing deed id: {}", idDeed);
        return deedRepository.save(deed);
    }
}
