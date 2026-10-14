package com.licensis.notaire.application.usecase.identification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeCatalogPort;
import com.licensis.notaire.application.port.out.identification.IdentificationTypeUsagePort;
import com.licensis.notaire.domain.identification.IdentificationType;
import com.licensis.notaire.domain.identification.IdentificationTypeInUseException;
import com.licensis.notaire.domain.identification.IdentificationTypeNameTakenException;
import com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException;

/**
 * Catalog administration (issue #1361). The DNI default for new people looks the type up by name, so two rows
 * that differ only in letter case or spaces would make it ambiguous: a name another type has is refused.
 */
@Service
@Transactional
public class IdentificationTypeManagementService implements ManageIdentificationTypesUseCase {

    private final IdentificationTypeCatalogPort catalog;
    private final IdentificationTypeUsagePort usage;

    public IdentificationTypeManagementService(IdentificationTypeCatalogPort catalog,
            IdentificationTypeUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public IdentificationType create(SaveIdentificationTypeCommand command) {
        IdentificationType type = IdentificationType.create(command.name(), command.characters());
        requireNameAvailable(type.name(), null);
        return catalog.save(type);
    }

    @Override
    public void update(int id, SaveIdentificationTypeCommand command) {
        IdentificationType existing = catalog.findById(id).orElseThrow(() -> new IdentificationTypeNotFoundException(id));
        IdentificationType changed = existing.changeTo(command.name(), command.characters());
        requireNameAvailable(changed.name(), id);
        catalog.save(changed);
    }

    @Override
    public void delete(int id) {
        if (!catalog.existsById(id)) {
            throw new IdentificationTypeNotFoundException(id);
        }
        if (usage.isUsedByPeople(id)) {
            throw new IdentificationTypeInUseException();
        }
        catalog.deleteById(id);
    }

    private void requireNameAvailable(String name, Integer ownId) {
        boolean taken = catalog.findAll().stream()
                .filter(other -> ownId == null || !ownId.equals(other.id()))
                .anyMatch(other -> other.hasSameNameAs(name));
        if (taken) {
            throw new IdentificationTypeNameTakenException();
        }
    }
}
