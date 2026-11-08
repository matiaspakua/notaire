package com.licensis.notaire.application.usecase.workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase;
import com.licensis.notaire.application.port.out.management.ManagementStatusCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;

/**
 * Workflow node administration. A node with associated transitions cannot be deleted (issue #1729).
 */
@Service
@Transactional
public class WorkflowNodeManagementService implements ManageWorkflowNodesUseCase {

    private final WorkflowNodeCatalogPort catalog;
    private final WorkflowDefinitionCatalogPort definitions;
    private final ManagementStatusCatalogPort statuses;

    public WorkflowNodeManagementService(
            WorkflowNodeCatalogPort catalog,
            WorkflowDefinitionCatalogPort definitions,
            ManagementStatusCatalogPort statuses) {
        this.catalog = catalog;
        this.definitions = definitions;
        this.statuses = statuses;
    }

    @Override
    public WorkflowNode create(CreateWorkflowNodeCommand command) {
        if (!definitions.existsById(command.workflowDefinitionId())) {
            throw WorkflowNodeInvalidException.missingWorkflow();
        }
        ManagementStatus status = statuses
                .findById(command.statusManagementId())
                .orElseThrow(WorkflowNodeInvalidException::missingStatus);
        return catalog.save(WorkflowNode.create(
                command.workflowDefinitionId(),
                command.statusManagementId(),
                status.name(),
                command.kind(),
                command.positionX(),
                command.positionY()));
    }

    @Override
    public void update(int id, UpdateWorkflowNodeCommand command) {
        WorkflowNode existing = catalog.findById(id).orElseThrow(() -> new WorkflowNodeNotFoundException(id));
        catalog.save(existing.changeTo(command.kind(), command.positionX(), command.positionY()));
    }

    @Override
    public void delete(int id) {
        if (!catalog.existsById(id)) {
            throw new WorkflowNodeNotFoundException(id);
        }
        if (catalog.hasTransitions(id)) {
            throw WorkflowNodeInUseException.cannotDelete();
        }
        if (!catalog.deleteById(id)) {
            throw new WorkflowNodeNotFoundException(id);
        }
    }
}
