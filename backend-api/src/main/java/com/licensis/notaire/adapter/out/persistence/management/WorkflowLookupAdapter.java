package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.WorkflowLookupPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedManagementRepository;

/**
 * Outbound adapter for WorkflowLookupPort. Resolves workflow definition id for a management
 * (issue #1850).
 */
public class WorkflowLookupAdapter implements WorkflowLookupPort {

    private final DeedManagementRepository managementRepository;

    public WorkflowLookupAdapter(DeedManagementRepository managementRepository) {
        this.managementRepository = managementRepository;
    }

    @Override
    public Integer resolveWorkflowDefinitionId(Integer managementId) {
        DeedManagement management = managementRepository.findById(managementId)
                .orElseThrow(() -> new ResourceNotFoundException("Management not found with ID: " + managementId));

        var procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new BusinessValidationException(
                    "Management " + management.getIdManagement() + " has no associated procedures");
        }

        ProcedureType procedureType = procedures.get(0).getFkIdProcedureType();
        WorkflowDefinition workflowDefinition = procedureType != null
                ? procedureType.getWorkflowDefinition()
                : null;

        if (workflowDefinition == null) {
            throw new BusinessValidationException(
                    "Management " + management.getIdManagement() + " has no workflow defined");
        }

        return workflowDefinition.getId();
    }
}
