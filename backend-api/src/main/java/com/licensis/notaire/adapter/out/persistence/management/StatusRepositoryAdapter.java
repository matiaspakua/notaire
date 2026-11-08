package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.StatusRepositoryPort;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.repository.ManagementStatusRepository;

/**
 * Outbound adapter for StatusRepositoryPort. Maps JPA status entities to domain views (issue #1850).
 */
public class StatusRepositoryAdapter implements StatusRepositoryPort {

    private final ManagementStatusRepository jpaRepository;

    public StatusRepositoryAdapter(ManagementStatusRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ManagementStatus findByName(String statusName) {
        return jpaRepository.findByName(statusName).map(StatusRepositoryAdapter::toDomain).orElse(null);
    }

    private static ManagementStatus toDomain(com.licensis.notaire.business.ManagementStatus entity) {
        return ManagementStatus.restore(
                entity.getIdManagementStatus(), entity.getName(), entity.getNotes(), entity.getVersion());
    }
}
