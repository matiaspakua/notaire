package com.licensis.notaire.adapter.out.persistence.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;

/**
 * Maps the legacy {@code business.WorkflowNode} entity to the pure domain type (issue #1729).
 */
@Component
public class WorkflowNodeCatalogPersistenceAdapter implements WorkflowNodeCatalogPort {

    private final WorkflowNodeRepository nodes;
    private final WorkflowDefinitionRepository definitions;
    private final ManagementStatusRepository statuses;
    private final WorkflowTransitionRepository transitions;

    public WorkflowNodeCatalogPersistenceAdapter(
            WorkflowNodeRepository nodes,
            WorkflowDefinitionRepository definitions,
            ManagementStatusRepository statuses,
            WorkflowTransitionRepository transitions) {
        this.nodes = nodes;
        this.definitions = definitions;
        this.statuses = statuses;
        this.transitions = transitions;
    }

    @Override
    public List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId) {
        return nodes.findByWorkflowDefinitionId(workflowDefinitionId).stream()
                .map(WorkflowNodeCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkflowNode> findById(int id) {
        return nodes.findById(id).map(WorkflowNodeCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return nodes.existsById(id);
    }

    @Override
    public boolean hasTransitions(int id) {
        return transitions.existsByOriginNodeIdOrDestinationNodeId(id, id);
    }

    @Override
    public WorkflowNode save(WorkflowNode node) {
        com.licensis.notaire.business.WorkflowNode entity = node.id() == null
                ? new com.licensis.notaire.business.WorkflowNode()
                : nodes.findById(node.id()).orElseThrow(() -> new WorkflowNodeNotFoundException(node.id()));
        if (node.workflowDefinitionId() != null) {
            WorkflowDefinition definition = definitions
                    .findById(node.workflowDefinitionId())
                    .orElseThrow(() -> new WorkflowNodeNotFoundException(node.workflowDefinitionId()));
            entity.setWorkflowDefinition(definition);
        }
        if (node.statusManagementId() != null) {
            ManagementStatus status = statuses
                    .findById(node.statusManagementId())
                    .orElseThrow(() -> new WorkflowNodeNotFoundException(node.statusManagementId()));
            entity.setManagementStatus(status);
        }
        if (node.kind() != null) {
            entity.setType(WorkflowNodeType.valueOf(node.kind().name()));
        }
        entity.setPositionX(node.positionX());
        entity.setPositionY(node.positionY());
        return toDomain(nodes.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!nodes.existsById(id)) {
            return false;
        }
        nodes.deleteById(id);
        return true;
    }

    private static WorkflowNode toDomain(com.licensis.notaire.business.WorkflowNode entity) {
        WorkflowDefinition definition = entity.getWorkflowDefinition();
        ManagementStatus status = entity.getManagementStatus();
        WorkflowNodeType type = entity.getType();
        return WorkflowNode.restore(
                entity.getId(),
                definition == null ? null : definition.getId(),
                status == null ? null : status.getIdManagementStatus(),
                status == null ? null : status.getName(),
                type == null ? null : WorkflowNodeKind.valueOf(type.name()),
                entity.getPositionX(),
                entity.getPositionY(),
                entity.getVersion());
    }
}
