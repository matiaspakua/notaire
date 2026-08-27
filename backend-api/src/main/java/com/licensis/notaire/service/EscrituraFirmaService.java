package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.repository.EscrituraRepository;
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
public class EscrituraFirmaService {

    private static final Logger log = LoggerFactory.getLogger(EscrituraFirmaService.class);

    private final EscrituraRepository escrituraRepository;
    private final FolioRepository folioRepository;

    public EscrituraFirmaService(EscrituraRepository escrituraRepository, FolioRepository folioRepository) {
        this.escrituraRepository = escrituraRepository;
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
    public Escritura firmar(Integer idEscritura) {
        Escritura escritura = escrituraRepository.findById(idEscritura)
                .orElseThrow(() -> new ResourceNotFoundException("Escritura no encontrada con ID: " + idEscritura));

        if (!ConstantesNegocio.ESCRITURA_SIN_FIRMAR.equals(escritura.getEstado())) {
            throw new BusinessValidationException(
                    "La escritura debe estar en estado 'Sin Firmar' para poder firmarse");
        }

        if (!folioRepository.existsByFkIdEscrituraIdEscritura(idEscritura)) {
            throw new BusinessValidationException(
                    "La escritura debe tener al menos un folio asignado para poder firmarse");
        }

        escritura.setEstado(ConstantesNegocio.ESCRITURA_FIRMADA);
        log.info("Firmando escritura id: {}", idEscritura);
        return escrituraRepository.save(escritura);
    }
}
