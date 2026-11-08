package com.licensis.notaire.application.usecase.workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowTransition;
import com.licensis.notaire.domain.workflow.WorkflowTransitionInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowTransitionNotFoundException;

/**
 * Workflow transition administration. Create/update validate workflow and node references (issue #1730).
 */
@Service
@Transactional
public class WorkflowTransitionManagementService implements ManageWorkflowTransitionsUseCase {

    private final WorkflowTransitionCatalogPort catalog;
    private final WorkflowDefinitionCatalogPort definitions;
    private final WorkflowNodeCatalogPort nodes;

    public WorkflowTransitionManagementService(
            WorkflowTransitionCatalogPort catalog,
            WorkflowDefinitionCatalogPort definitions,
            WorkflowNodeCatalogPort nodes) {
        this.catalog = catalog;
        this.definitions = definitions;
        this.nodes = nodes;
    }

    @Override
    public WorkflowTransition create(CreateWorkflowTransitionCommand command) {
        if (!definitions.existsById(command.workflowDefinitionId())) {
            throw WorkflowTransitionInvalidException.missingWorkflow();
        }
        if (!nodes.existsById(command.originNodeId())) {
            throw WorkflowTransitionInvalidException.missingOriginNode();
        }
        if (!nodes.existsById(command.destinationNodeId())) {
            throw WorkflowTransitionInvalidException.missingDestinationNode();
        }
        return catalog.save(WorkflowTransition.create(
                command.workflowDefinitionId(),
                command.originNodeId(),
                command.destinationNodeId(),
                command.condition(),
                command.description()));
    }

    @Override
    public WorkflowTransition update(int id, UpdateWorkflowTransitionCommand command) {
        WorkflowTransition existing =
                catalog.findById(id).orElseThrow(() -> new WorkflowTransitionNotFoundException(id));
        if (!nodes.existsById(command.originNodeId())) {
            throw WorkflowTransitionInvalidException.missingOriginNode();
        }
        if (!nodes.existsById(command.destinationNodeId())) {
            throw WorkflowTransitionInvalidException.missingDestinationNode();
        }
        return catalog.save(existing.changeTo(
                command.originNodeId(),
                command.destinationNodeId(),
                command.condition(),
                command.description()));
    }

    @Override
    public void delete(int id) {
        if (!catalog.existsById(id)) {
            throw new WorkflowTransitionNotFoundException(id);
        }
        if (!catalog.deleteById(id)) {
            throw new WorkflowTransitionNotFoundException(id);
        }
    }
}
