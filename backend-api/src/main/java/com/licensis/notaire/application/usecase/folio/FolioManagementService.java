package com.licensis.notaire.application.usecase.folio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase;
import com.licensis.notaire.application.port.out.deed.DeedLookupPort;
import com.licensis.notaire.application.port.out.folio.FolioCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.person.PersonLookupPort;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.domain.folio.FolioInUseException;
import com.licensis.notaire.domain.folio.FolioInvalidException;
import com.licensis.notaire.domain.folio.FolioNotFoundException;

/**
 * Folio instance administration. A Utilizado folio is locked except for an idempotent re-save of the same deed link
 * (issue #1727).
 */
@Service
@Transactional
public class FolioManagementService implements ManageFoliosUseCase {

    private final FolioCatalogPort catalog;
    private final FolioTypeCatalogPort folioTypes;
    private final PersonLookupPort people;
    private final DeedLookupPort deeds;

    public FolioManagementService(
            FolioCatalogPort catalog,
            FolioTypeCatalogPort folioTypes,
            PersonLookupPort people,
            DeedLookupPort deeds) {
        this.catalog = catalog;
        this.folioTypes = folioTypes;
        this.people = people;
        this.deeds = deeds;
    }

    @Override
    public Folio create(SaveFolioCommand command) {
        requireTypeAndNotary(command);
        requireDeedIfPresent(command.deedId());
        Folio draft = Folio.create(
                command.number(),
                command.year(),
                command.status(),
                command.notes(),
                command.typeFolioId(),
                command.notaryId(),
                command.deedId());
        return catalog.save(draft);
    }

    @Override
    public Folio update(int id, SaveFolioCommand command) {
        Folio existing = catalog.findById(id).orElseThrow(() -> new FolioNotFoundException(id));
        if (existing.isInUse() && !existing.linksSameDeed(command.deedId())) {
            throw FolioInUseException.cannotModify();
        }
        requireDeedIfPresent(command.deedId());
        if (command.typeFolioId() != null && folioTypes.findById(command.typeFolioId()).isEmpty()) {
            throw new FolioInvalidException("Folio type not found: " + command.typeFolioId());
        }
        if (command.notaryId() != null && !people.existsById(command.notaryId())) {
            throw new FolioInvalidException("Notary not found: " + command.notaryId());
        }
        Folio updated = existing.changeTo(
                command.number(),
                command.year(),
                command.status(),
                command.notes(),
                command.typeFolioId(),
                command.notaryId(),
                command.deedId());
        return catalog.save(updated);
    }

    @Override
    public void delete(int id) {
        Folio existing = catalog.findById(id).orElseThrow(() -> new FolioNotFoundException(id));
        if (existing.isInUse()) {
            throw FolioInUseException.cannotDelete();
        }
        if (!catalog.deleteById(id)) {
            throw new FolioNotFoundException(id);
        }
    }

    private void requireTypeAndNotary(SaveFolioCommand command) {
        if (command.typeFolioId() == null || command.notaryId() == null) {
            throw new FolioInvalidException("typeFolioId and notaryId are required");
        }
        if (folioTypes.findById(command.typeFolioId()).isEmpty()) {
            throw new FolioInvalidException("Folio type not found: " + command.typeFolioId());
        }
        if (!people.existsById(command.notaryId())) {
            throw new FolioInvalidException("Notary not found: " + command.notaryId());
        }
    }

    private void requireDeedIfPresent(Integer deedId) {
        if (deedId != null && !deeds.existsById(deedId)) {
            throw new FolioInvalidException("Deed not found: " + deedId);
        }
    }
}
