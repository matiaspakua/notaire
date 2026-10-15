package com.licensis.notaire.application.port.out.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowDefinition;

/** Workflow definition storage in domain terms (issue #1728). */
public interface WorkflowDefinitionCatalogPort {

    List<WorkflowDefinition> findAll();

    Optional<WorkflowDefinition> findById(int id);

    boolean existsById(int id);

    /** True when the definition has at least one associated workflow node. */
    boolean hasNodes(int id);

    WorkflowDefinition save(WorkflowDefinition definition);

    boolean deleteById(int id);
}
