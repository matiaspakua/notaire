package com.licensis.notaire.application.port.in.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowTransition;

public interface QueryWorkflowTransitionsUseCase {

    List<WorkflowTransition> findByWorkflowDefinitionId(int workflowDefinitionId);

    Optional<WorkflowTransition> findById(int id);
}
