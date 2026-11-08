package com.licensis.notaire.application.port.in.workflow;

import com.licensis.notaire.domain.workflow.WorkflowTransition;

public interface ManageWorkflowTransitionsUseCase {

    record CreateWorkflowTransitionCommand(
            int workflowDefinitionId,
            int originNodeId,
            int destinationNodeId,
            String condition,
            String description) {}

    record UpdateWorkflowTransitionCommand(
            int originNodeId, int destinationNodeId, String condition, String description) {}

    WorkflowTransition create(CreateWorkflowTransitionCommand command);

    WorkflowTransition update(int id, UpdateWorkflowTransitionCommand command);

    void delete(int id);
}
