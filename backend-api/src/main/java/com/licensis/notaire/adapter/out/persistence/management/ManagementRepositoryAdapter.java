package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.ManagementRepositoryPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementTransitionSnapshot;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;

/**
 * Outbound adapter for ManagementRepositoryPort. Maps JPA entities to domain transition snapshots
 * (issue #1850).
 */
public class ManagementRepositoryAdapter implements ManagementRepositoryPort {

    private final DeedManagementRepository jpaRepository;
    private final ManagementStatusRepository statusRepository;

    public ManagementRepositoryAdapter(DeedManagementRepository jpaRepository,
            ManagementStatusRepository statusRepository) {
        this.jpaRepository = jpaRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public ManagementTransitionSnapshot findById(Integer managementId) {
        return jpaRepository.findById(managementId).map(this::toSnapshot).orElse(null);
    }

    @Override
    public ManagementTransitionSnapshot saveStatus(Integer managementId, Integer statusId) {
        DeedManagement management = jpaRepository.findById(managementId)
                .orElseThrow(() -> new ResourceNotFoundException("Management not found with ID: " + managementId));
        ManagementStatus status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Management status not found with ID: " + statusId));
        management.setFkIdManagementStatus(status);
        return toSnapshot(jpaRepository.save(management));
    }

    private ManagementTransitionSnapshot toSnapshot(DeedManagement management) {
        ManagementStatus status = management.getFkIdManagementStatus();
        Integer statusId = status != null ? status.getIdManagementStatus() : null;
        String statusName = status != null ? status.getName() : null;
        Integer workflowDefinitionId = null;
        var procedures = management.getProcedureList();
        if (procedures != null && !procedures.isEmpty() && procedures.get(0).getFkIdProcedureType() != null
                && procedures.get(0).getFkIdProcedureType().getWorkflowDefinition() != null) {
            workflowDefinitionId = procedures.get(0).getFkIdProcedureType().getWorkflowDefinition().getId();
        }
        return new ManagementTransitionSnapshot(management.getIdManagement(), statusId, statusName,
                workflowDefinitionId);
    }
}
