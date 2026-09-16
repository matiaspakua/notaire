package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.StatusRepositoryPort;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.repository.ManagementStatusRepository;

/**
 * Outbound adapter for StatusRepositoryPort.
 * Adapts Spring Data JPA repository to the port interface.
 */
public class StatusRepositoryAdapter implements StatusRepositoryPort {

    private final ManagementStatusRepository jpaRepository;

    public StatusRepositoryAdapter(ManagementStatusRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ManagementStatus findByName(String statusName) {
        return jpaRepository.findByName(statusName).orElse(null);
    }
}
