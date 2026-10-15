package com.licensis.notaire.application.port.in.workflow;

import com.licensis.notaire.domain.workflow.WorkflowDefinition;

public interface ManageWorkflowDefinitionsUseCase {

    record SaveWorkflowDefinitionCommand(String name, String description, boolean active, Integer version) {}

    WorkflowDefinition create(SaveWorkflowDefinitionCommand command);

    void update(int id, SaveWorkflowDefinitionCommand command);

    void delete(int id);
}
