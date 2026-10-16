package com.licensis.notaire.adapter.out.persistence.management;

import com.licensis.notaire.application.port.out.management.WorkflowTransitionValidatorPort;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.WorkflowTransitionRepository;

import java.util.List;

/**
 * Outbound adapter for WorkflowTransitionValidatorPort. Validates transitions by status ids
 * (issue #1850).
 */
public class WorkflowTransitionValidatorAdapter implements WorkflowTransitionValidatorPort {

    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowTransitionValidatorAdapter(WorkflowTransitionRepository workflowTransitionRepository) {
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    @Override
    public boolean isTransitionValid(Integer workflowDefinitionId, Integer originStatusId,
            Integer destinationStatusId) {
        if (workflowDefinitionId == null || originStatusId == null || destinationStatusId == null) {
            return false;
        }
        List<WorkflowTransition> transitions =
                workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinitionId);
        return transitions.stream()
                .anyMatch(transition -> statusMatches(transition.getOriginNode(), originStatusId)
                        && statusMatches(transition.getDestinationNode(), destinationStatusId));
    }

    private static boolean statusMatches(WorkflowNode node, Integer statusId) {
        return node != null && node.getManagementStatus() != null
                && statusId.equals(node.getManagementStatus().getIdManagementStatus());
    }
}
