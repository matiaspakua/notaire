package com.licensis.notaire.application.usecase.folio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.folio.ManageFolioTypesUseCase;
import com.licensis.notaire.application.port.out.folio.FolioTypeCatalogPort;
import com.licensis.notaire.application.port.out.folio.FolioTypeUsagePort;
import com.licensis.notaire.application.usecase.catalog.CatalogLockSupport;
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
        CatalogLockSupport.update(
                id,
                catalog::findById,
                usage::isUsedByFolios,
                FolioType::version,
                command.version(),
                () -> new FolioTypeNotFoundException(id),
                FolioTypeInUseException::lockedForEdit,
                FolioTypeStaleVersionException::new,
                existing -> existing.changeTo(command.name(), command.notes(), command.enabled(), command.auxiliary()),
                catalog::save);
    }

    @Override
    public void delete(int id) {
        CatalogLockSupport.delete(
                id,
                catalog::findById,
                usage::isUsedByFolios,
                () -> new FolioTypeNotFoundException(id),
                FolioTypeInUseException::cannotDelete,
                catalog::deleteById);
    }
}
