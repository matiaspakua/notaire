package com.licensis.notaire.application.port.in.workflow;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.workflow.WorkflowDefinition;

public interface QueryWorkflowDefinitionsUseCase {

    List<WorkflowDefinition> findAll();

    Optional<WorkflowDefinition> findById(int id);
}
