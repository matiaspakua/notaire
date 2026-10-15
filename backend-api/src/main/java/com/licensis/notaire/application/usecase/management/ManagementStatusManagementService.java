package com.licensis.notaire.application.usecase.management;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.management.ManageManagementStatusesUseCase;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
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
        ManagementStatus existing = catalog.findById(id).orElseThrow(() -> new ManagementStatusNotFoundException(id));
        if (usage.isUsedByDeedManagements(id)) {
            throw ManagementStatusInUseException.lockedForEdit();
        }
        if (existing.version() != command.version()) {
            throw new ManagementStatusStaleVersionException(id, existing.version(), command.version());
        }
        catalog.save(existing.changeTo(command.name(), command.notes()));
    }

    @Override
    public void delete(int id) {
        catalog.findById(id).orElseThrow(() -> new ManagementStatusNotFoundException(id));
        if (usage.isUsedByDeedManagements(id)) {
            throw ManagementStatusInUseException.cannotDelete();
        }
        if (!catalog.deleteById(id)) {
            throw new ManagementStatusNotFoundException(id);
        }
    }
}
