package com.licensis.notaire.service;

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
 * Validates and applies the "Firmar escritura" business action (CU06).
 */
@Service
@Transactional
public class DeedFirmaService {

    private static final Logger log = LoggerFactory.getLogger(DeedFirmaService.class);

    private final DeedRepository deedRepository;
    private final FolioRepository folioRepository;

    public DeedFirmaService(DeedRepository deedRepository, FolioRepository folioRepository) {
        this.deedRepository = deedRepository;
        this.folioRepository = folioRepository;
    }

    /**
     * Signs an escritura: requires it to be "Sin Firmar" and have at least one folio assigned.
     *
     * @param idEscritura the escritura ID
     * @return the signed escritura
     * @throws ResourceNotFoundException if no escritura with the given ID exists
     * @throws BusinessValidationException if the escritura is not "Sin Firmar" or has no folio assigned
     */
    public Deed firmar(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Escritura no encontrada con ID: " + idDeed));

        if (!BusinessConstants.DeedSINFIRMAR.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "La escritura debe estar en estado 'Sin Firmar' para poder firmarse");
        }

        if (!folioRepository.existsByFkIdDeedIdDeed(idDeed)) {
            throw new BusinessValidationException(
                    "La escritura debe tener al menos un folio asignado para poder firmarse");
        }

        deed.setStatus(BusinessConstants.DeedFIRMADA);
        log.info("Firmando escritura id: {}", idDeed);
        return deedRepository.save(deed);
    }
}
