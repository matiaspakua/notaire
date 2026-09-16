package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.WorkflowTransitionValidatorPort;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.WorkflowTransitionRepository;

import java.util.List;

/**
 * Outbound adapter for WorkflowTransitionValidatorPort.
 * Validates if a status transition is allowed by the workflow definition.
 */
public class WorkflowTransitionValidatorAdapter implements WorkflowTransitionValidatorPort {

    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowTransitionValidatorAdapter(WorkflowTransitionRepository workflowTransitionRepository) {
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    @Override
    public boolean isTransitionValid(WorkflowDefinition workflowDefinition, ManagementStatus origin,
            ManagementStatus destination) {
        List<WorkflowTransition> transitions =
                workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinition.getId());
        return transitions.stream()
                .anyMatch(transicion -> coincideStatus(transicion.getOriginNode(), origin)
                        && coincideStatus(transicion.getDestinationNode(), destination));
    }

    private static boolean coincideStatus(WorkflowNode node, ManagementStatus status) {
        return node != null && node.getManagementStatus() != null && status != null
                && node.getManagementStatus().getIdManagementStatus()
                        .equals(status.getIdManagementStatus());
    }
}
