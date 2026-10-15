package com.licensis.notaire.application.port.in.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowNode;

public interface QueryWorkflowNodesUseCase {

    List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId);

    Optional<WorkflowNode> findById(int id);
}
