package com.licensis.notaire.application.port.out.workflow;

import com.licensis.notaire.business.WorkflowTransition;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for workflow transition persistence operations.
 */
public interface WorkflowTransitionRepositoryPort {

    List<WorkflowTransition> findAll();

    Optional<WorkflowTransition> findById(Integer id);

    WorkflowTransition save(WorkflowTransition entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<WorkflowTransition> findByWorkflowDefinitionId(Integer workflowDefinitionId);
}
