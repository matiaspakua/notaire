package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.ManagementStatusLogPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedManagementRepository;

/**
 * Outbound adapter for ManagementStatusLogPort (issue #1850).
 */
public class ManagementStatusLogAdapter implements ManagementStatusLogPort {

    private final ManagementStatusLogService statusLogService;
    private final DeedManagementRepository managementRepository;

    public ManagementStatusLogAdapter(ManagementStatusLogService statusLogService,
            DeedManagementRepository managementRepository) {
        this.statusLogService = statusLogService;
        this.managementRepository = managementRepository;
    }

    @Override
    public void registerStatus(Integer managementId, String detail) {
        DeedManagement management = managementRepository.findById(managementId)
                .orElseThrow(() -> new ResourceNotFoundException("Management not found with ID: " + managementId));
        statusLogService.registerStatus(management, detail);
    }
}
