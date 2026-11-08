package com.licensis.notaire.adapter.out.persistence.notebook;

import com.licensis.notaire.application.port.out.folio.FolioCatalogPort;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Creates procedures in the auxiliary protocol (CU81). Outbound persistence helper
 * (issues #1880 / #1903 — Spring Data FolioRepository locally; no entity-speaking application port).
 */
@Service
@Transactional
public class AuxiliaryProtocolService {

    private final FolioRepository folioRepository;
    private final FolioCatalogPort folioCatalog;
    private final DeedRepository deedRepository;

    public AuxiliaryProtocolService(
            FolioRepository folioRepository,
            FolioCatalogPort folioCatalog,
            DeedRepository deedRepository) {
        this.folioRepository = folioRepository;
        this.folioCatalog = folioCatalog;
        this.deedRepository = deedRepository;
    }

    /**
     * Available auxiliary folios as domain records so the HTTP adapter can reuse {@code FolioResponse}
     * without the ports-only {@code FolioController} depending on JPA entities (issue #1727).
     */
    @Transactional(readOnly = true)
    public List<com.licensis.notaire.domain.folio.Folio> listAvailableFolios() {
        return folioCatalog.findAvailableAuxiliaryFolios();
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
