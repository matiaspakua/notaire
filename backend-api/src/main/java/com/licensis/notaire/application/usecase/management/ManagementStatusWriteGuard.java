package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Shared guard for gestión status writes (CU83 / #804).
 * <p>
 * Post-create status mutations must go through
 * {@code POST /api/v1/gestiones/{id}/transition}. Create-time assignment is
 * allowed without a prior edge, but when a workflow exists the chosen status
 * must be a workflow node (prefer INITIAL/start nodes).
 */
@Service
public class ManagementStatusWriteGuard {

    private final WorkflowNodeRepository workflowNodeRepository;

    public ManagementStatusWriteGuard(WorkflowNodeRepository workflowNodeRepository) {
        this.workflowNodeRepository = workflowNodeRepository;
    }

    /**
     * Rejects changing the status id on an existing gestión via generic update
     * paths. Same-status updates and omitted status fields are allowed.
     *
     * @param previousStatusId current status id (may be null)
     * @param requestedStatusId status id from the request (null means no change)
     */
    public void rejectStatusMutationOnUpdate(Integer previousStatusId, Integer requestedStatusId) {
        if (requestedStatusId == null) {
            return;
        }
        if (previousStatusId != null && previousStatusId.equals(requestedStatusId)) {
            return;
        }
        throw new BusinessValidationException(
                "Status changes must use POST /api/v1/gestiones/{id}/transition; "
                        + "generic update cannot change management status");
    }

    /**
     * Validates create-time status against the workflow graph when present.
     * Prefer INITIAL nodes when the definition has any; otherwise any node.
     *
     * @param workflowDefinition workflow of the procedure type (may be null)
     * @param status chosen initial status (may be null)
     */
    public void validateInitialStatus(WorkflowDefinition workflowDefinition, ManagementStatus status) {
        if (workflowDefinition == null || status == null || status.getIdManagementStatus() == null) {
            return;
        }
        List<WorkflowNode> nodes =
                workflowNodeRepository.findByWorkflowDefinitionId(workflowDefinition.getId());
        if (nodes.isEmpty()) {
            return;
        }
        List<WorkflowNode> initialNodes = nodes.stream()
                .filter(node -> node.getType() == WorkflowNodeType.INITIAL)
                .toList();
        List<WorkflowNode> allowedNodes = initialNodes.isEmpty() ? nodes : initialNodes;
        boolean allowed = allowedNodes.stream()
                .anyMatch(node -> node.getManagementStatus() != null
                        && status.getIdManagementStatus()
                                .equals(node.getManagementStatus().getIdManagementStatus()));
        if (!allowed) {
            throw new BusinessValidationException(
                    "Initial management status must be a start node of the workflow definition");
        }
    }
}
