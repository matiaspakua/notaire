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
 * Creates procedures in the auxiliary protocol (CU81).
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

    public int calculateNextAuxiliaryNumber() {
        return folioRepository.findMaxAuxiliaryDeedNumber().orElse(0) + 1;
    }

    public Deed startDeed(Integer idFolio, String body, Date dateDeedrecording) {
        if (idFolio == null) {
            throw new BusinessValidationException("The auxiliary folio where the deed starts is required");
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
        deed.setNumber(calculateNextAuxiliaryNumber());
        deed.setBody(body);
        deed.setDateDeedrecording(dateDeedrecording);
        Deed saved = deedRepository.save(deed);

        folio.setFkIdDeed(saved);
        folioRepository.save(folio);

        return saved;
    }
}
