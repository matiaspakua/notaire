package com.licensis.notaire.application.port.in.workflow;

import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;

public interface ManageWorkflowNodesUseCase {

    record CreateWorkflowNodeCommand(
            int workflowDefinitionId,
            int statusManagementId,
            WorkflowNodeKind kind,
            Float positionX,
            Float positionY) {}

    record UpdateWorkflowNodeCommand(WorkflowNodeKind kind, Float positionX, Float positionY) {}

    WorkflowNode create(CreateWorkflowNodeCommand command);

    void update(int id, UpdateWorkflowNodeCommand command);

    void delete(int id);
}
