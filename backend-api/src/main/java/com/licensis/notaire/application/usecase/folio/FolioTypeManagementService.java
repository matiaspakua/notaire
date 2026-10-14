package com.licensis.notaire.application.usecase.folio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeInUseException;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;
import com.licensis.notaire.domain.folio.FolioTypeStaleVersionException;

/**
 * Folio type administration. A type that folios use is locked, because changing it would rewrite what those folios
 * mean: it can be neither edited nor deleted, and a new type is created instead.
 */
@Service
@Transactional
public class FolioTypeManagementService implements ManageFolioTypesUseCase {

    private final FolioTypeCatalogPort catalog;
    private final FolioTypeUsagePort usage;

    public FolioTypeManagementService(FolioTypeCatalogPort catalog, FolioTypeUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public FolioType create(SaveFolioTypeCommand command) {
        return catalog.save(FolioType.create(command.name(), command.notes(), command.enabled(), command.auxiliary()));
    }

    @Override
    public void update(int id, SaveFolioTypeCommand command) {
        FolioType existing = catalog.findById(id).orElseThrow(() -> new FolioTypeNotFoundException(id));
        if (usage.isUsedByFolios(id)) {
            throw FolioTypeInUseException.lockedForEdit();
        }
        if (existing.version() != command.version()) {
            throw new FolioTypeStaleVersionException(id, existing.version(), command.version());
        }
        catalog.save(existing.changeTo(command.name(), command.notes(), command.enabled(), command.auxiliary()));
    }

    @Override
    public void delete(int id) {
        catalog.findById(id).orElseThrow(() -> new FolioTypeNotFoundException(id));
        if (usage.isUsedByFolios(id)) {
            throw FolioTypeInUseException.cannotDelete();
        }
        if (!catalog.deleteById(id)) {
            throw new FolioTypeNotFoundException(id);
        }
    }
}
