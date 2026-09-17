package com.licensis.notaire.application.port.out.workflow;

import com.licensis.notaire.business.WorkflowDefinition;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for workflow definition persistence operations.
 */
public interface WorkflowDefinitionRepositoryPort {

    List<WorkflowDefinition> findAll();

    Optional<WorkflowDefinition> findById(Integer id);

    WorkflowDefinition save(WorkflowDefinition entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
