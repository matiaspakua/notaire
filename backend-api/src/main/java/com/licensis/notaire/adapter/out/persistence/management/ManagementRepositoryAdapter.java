package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.ManagementRepositoryPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.repository.DeedManagementRepository;

/**
 * Outbound adapter for ManagementRepositoryPort.
 * Adapts Spring Data JPA repository to the port interface.
 */
public class ManagementRepositoryAdapter implements ManagementRepositoryPort {

    private final DeedManagementRepository jpaRepository;

    public ManagementRepositoryAdapter(DeedManagementRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DeedManagement findById(Integer managementId) {
        return jpaRepository.findById(managementId).orElse(null);
    }

    @Override
    public DeedManagement save(DeedManagement management) {
        return jpaRepository.save(management);
    }
}
