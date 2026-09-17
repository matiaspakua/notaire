package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.WorkflowLookupPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.exception.BusinessValidationException;

/**
 * Outbound adapter for WorkflowLookupPort.
 * Resolves the workflow definition for a management based on its procedures.
 */
public class WorkflowLookupAdapter implements WorkflowLookupPort {

    @Override
    public WorkflowDefinition resolveWorkflowDefinition(DeedManagement management) {
        var procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new BusinessValidationException(
                    "La gestión " + management.getIdManagement() + " no tiene trámites asociados");
        }

        ProcedureType typeProcedure = procedures.get(0).getFkIdProcedureType();
        WorkflowDefinition workflowDefinition = typeProcedure != null
                ? typeProcedure.getWorkflowDefinition()
                : null;

        if (workflowDefinition == null) {
            throw new BusinessValidationException(
                    "La gestión " + management.getIdManagement() + " no tiene un workflow definido");
        }

        return workflowDefinition;
    }
}
