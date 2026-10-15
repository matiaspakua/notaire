package com.licensis.notaire.adapter.out.persistence.management;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.management.ManagementStatusUsagePort;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;
import com.licensis.notaire.domain.management.ManagementStatusReferencedException;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;

/**
 * Maps the legacy {@code business.ManagementStatus} JPA entity, which stays because deed managements and history
 * still use it, to the pure domain type (issue #1719).
 */
@Component
public class ManagementStatusPersistenceAdapter implements ManagementStatusCatalogPort, ManagementStatusUsagePort {

    private final ManagementStatusRepository statuses;
    private final DeedManagementRepository deedManagements;

    public ManagementStatusPersistenceAdapter(
            ManagementStatusRepository statuses, DeedManagementRepository deedManagements) {
        this.statuses = statuses;
        this.deedManagements = deedManagements;
    }

    @Override
    public List<ManagementStatus> findAll() {
        return statuses.findAll().stream().map(ManagementStatusPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ManagementStatus> findByNameContaining(String text) {
        return statuses.findByNameContaining(text).stream().map(ManagementStatusPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<ManagementStatus> findById(int id) {
        return statuses.findById(id).map(ManagementStatusPersistenceAdapter::toDomain);
    }

    @Override
    public ManagementStatus save(ManagementStatus status) {
        com.licensis.notaire.business.ManagementStatus entity = status.id() == null
                ? new com.licensis.notaire.business.ManagementStatus()
                : statuses.findById(status.id()).orElseThrow(() -> new ManagementStatusNotFoundException(status.id()));
        entity.setName(status.name());
        entity.setNotes(status.notes());
        return toDomain(statuses.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!statuses.existsById(id)) {
            return false;
        }
        try {
            statuses.deleteById(id);
            statuses.flush();
        } catch (DataIntegrityViolationException referenced) {
            throw new ManagementStatusReferencedException(referenced);
        }
        return true;
    }

    @Override
    public boolean isUsedByDeedManagements(int id) {
        return !deedManagements.findByFkIdManagementStatusIdManagementStatus(id).isEmpty();
    }

    private static ManagementStatus toDomain(com.licensis.notaire.business.ManagementStatus entity) {
        return ManagementStatus.restore(
                entity.getIdManagementStatus(), entity.getName(), entity.getNotes(), entity.getVersion());
    }
}
