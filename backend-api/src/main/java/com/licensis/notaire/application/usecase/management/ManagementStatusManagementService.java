package com.licensis.notaire.application.usecase.management;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
import com.licensis.notaire.application.usecase.catalog.CatalogLockSupport;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusInUseException;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;
import com.licensis.notaire.domain.management.ManagementStatusStaleVersionException;

/**
 * Management status administration. A status that deed managements reference is locked: it can be neither edited
 * nor deleted.
 */
@Service
@Transactional
public class ManagementStatusManagementService implements ManageManagementStatusesUseCase {

    private final ManagementStatusCatalogPort catalog;
    private final ManagementStatusUsagePort usage;

    public ManagementStatusManagementService(ManagementStatusCatalogPort catalog, ManagementStatusUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public ManagementStatus create(SaveManagementStatusCommand command) {
        return catalog.save(ManagementStatus.create(command.name(), command.notes()));
    }

    @Override
    public void update(int id, SaveManagementStatusCommand command) {
        CatalogLockSupport.update(
                id,
                catalog::findById,
                usage::isUsedByDeedManagements,
                ManagementStatus::version,
                command.version(),
                () -> new ManagementStatusNotFoundException(id),
                ManagementStatusInUseException::lockedForEdit,
                ManagementStatusStaleVersionException::new,
                existing -> existing.changeTo(command.name(), command.notes()),
                catalog::save);
    }

    @Override
    public void delete(int id) {
        CatalogLockSupport.delete(
                id,
                catalog::findById,
                usage::isUsedByDeedManagements,
                () -> new ManagementStatusNotFoundException(id),
                ManagementStatusInUseException::cannotDelete,
                catalog::deleteById);
    }
}
