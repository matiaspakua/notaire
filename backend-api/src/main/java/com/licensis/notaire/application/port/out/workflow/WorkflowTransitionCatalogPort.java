package com.licensis.notaire.application.port.out.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowTransition;

/** Workflow transition storage in domain terms (issue #1730). */
public interface WorkflowTransitionCatalogPort {

    List<WorkflowTransition> findByWorkflowDefinitionId(int workflowDefinitionId);

    Optional<WorkflowTransition> findById(int id);

    boolean existsById(int id);

    WorkflowTransition save(WorkflowTransition transition);

    boolean deleteById(int id);
}
