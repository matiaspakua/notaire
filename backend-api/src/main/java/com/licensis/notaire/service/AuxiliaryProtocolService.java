package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Gestiona el alta de trámites en Protocolo Auxiliar (CU81).
 */
@Service
@Transactional
public class AuxiliaryProtocolService {

    private final FolioRepository folioRepository;
    private final DeedRepository deedRepository;

    public AuxiliaryProtocolService(FolioRepository folioRepository, DeedRepository deedRepository) {
        this.folioRepository = folioRepository;
        this.deedRepository = deedRepository;
    }

    @Transactional(readOnly = true)
    public List<Folio> listAvailableFolios() {
        return folioRepository.findAvailableAuxiliaryFolios();
    }

    public int calculateNextNumberAuxiliary() {
        return folioRepository.findMaxNumberDeedAuxiliary().orElse(0) + 1;
    }

    public Deed iniciarDeed(Integer idFolio, String body, Date dateDeedrecording) {
        if (idFolio == null) {
            throw new BusinessValidationException("Debe indicar el folio auxiliar donde iniciar la escritura");
        }

        Folio folio = folioRepository.findById(idFolio)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el folio con ID: " + idFolio));

        if (!folio.getFkIdFolioType().isIsAuxiliary()) {
            throw new BusinessValidationException("El folio indicado no pertenece a Protocolo Auxiliar");
        }
        if (folio.getFkIdDeed() != null) {
            throw new BusinessValidationException("El folio indicado ya tiene una escritura asociada");
        }

        Deed deed = new Deed();
        deed.setNumber(calculateNextNumberAuxiliary());
        deed.setBody(body);
        deed.setDateDeedrecording(dateDeedrecording);
        Deed guardada = deedRepository.save(deed);

        folio.setFkIdDeed(guardada);
        folioRepository.save(folio);

        return guardada;
    }
}
