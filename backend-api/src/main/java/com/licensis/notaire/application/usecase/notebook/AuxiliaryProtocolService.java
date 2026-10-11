package com.licensis.notaire.application.usecase.notebook;

import com.licensis.notaire.application.port.out.folio.FolioRepositoryPort;
import com.licensis.notaire.application.port.out.notebook.DeedOperationPort;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Gestiona the alta of procedures in Auxiliary protocol (CU81).
 */
@Service
@Transactional
public class AuxiliaryProtocolService {

    private final FolioRepositoryPort folioRepository;
    private final DeedOperationPort deedRepository;

    public AuxiliaryProtocolService(FolioRepositoryPort folioRepository, DeedOperationPort deedRepository) {
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
            throw new BusinessValidationException("Must indicar the folio auxiliar where start the deed");
        }

        Folio folio = folioRepository.findById(idFolio)
                .orElseThrow(() -> new ResourceNotFoundException("Folio not found with ID: " + idFolio));

        if (!folio.getFkIdFolioType().isIsAuxiliary()) {
            throw new BusinessValidationException("The indicated folio does not belong to Protocolo Auxiliar");
        }
        if (folio.getFkIdDeed() != null) {
            throw new BusinessValidationException("The indicated folio already has a deed associated");
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
