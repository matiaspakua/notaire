package com.licensis.notaire.application.usecase.workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionInUseException;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;

/**
 * Workflow definition administration. A definition with associated nodes cannot be deleted (issue #1728).
 */
@Service
@Transactional
public class WorkflowDefinitionManagementService implements ManageWorkflowDefinitionsUseCase {

    private final WorkflowDefinitionCatalogPort catalog;

    public WorkflowDefinitionManagementService(WorkflowDefinitionCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public WorkflowDefinition create(SaveWorkflowDefinitionCommand command) {
        return catalog.save(WorkflowDefinition.create(command.name(), command.description(), command.active()));
    }

    @Override
    public void update(int id, SaveWorkflowDefinitionCommand command) {
        WorkflowDefinition existing =
                catalog.findById(id).orElseThrow(() -> new WorkflowDefinitionNotFoundException(id));
        catalog.save(existing.changeTo(command.name(), command.description(), command.active(), command.version()));
    }

    @Override
    public void delete(int id) {
        if (!catalog.existsById(id)) {
            throw new WorkflowDefinitionNotFoundException(id);
        }
        if (catalog.hasNodes(id)) {
            throw WorkflowDefinitionInUseException.cannotDelete();
        }
        if (!catalog.deleteById(id)) {
            throw new WorkflowDefinitionNotFoundException(id);
        }
    }
}
