package com.licensis.notaire.application.port.out.workflow;

import com.licensis.notaire.business.WorkflowNode;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for workflow node persistence operations.
 */
public interface WorkflowNodeRepositoryPort {

    List<WorkflowNode> findAll();

    Optional<WorkflowNode> findById(Integer id);

    WorkflowNode save(WorkflowNode entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<WorkflowNode> findByWorkflowDefinitionId(Integer workflowDefinitionId);
}
