package com.licensis.notaire.application.port.out.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowNode;

/** Workflow node storage in domain terms (issue #1729). */
public interface WorkflowNodeCatalogPort {

    List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId);

    Optional<WorkflowNode> findById(int id);

    boolean existsById(int id);

    /** True when the node is origin or destination of at least one transition. */
    boolean hasTransitions(int id);

    WorkflowNode save(WorkflowNode node);

    boolean deleteById(int id);
}
