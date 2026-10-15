package com.licensis.notaire.adapter.out.persistence.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowTransition;
import com.licensis.notaire.domain.workflow.WorkflowTransitionNotFoundException;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;

/**
 * Maps the legacy {@code business.WorkflowTransition} entity to the pure domain type (issue #1730).
 */
@Component
public class WorkflowTransitionCatalogPersistenceAdapter implements WorkflowTransitionCatalogPort {

    private final WorkflowTransitionRepository transitions;
    private final WorkflowDefinitionRepository definitions;
    private final WorkflowNodeRepository nodes;

    public WorkflowTransitionCatalogPersistenceAdapter(
            WorkflowTransitionRepository transitions,
            WorkflowDefinitionRepository definitions,
            WorkflowNodeRepository nodes) {
        this.transitions = transitions;
        this.definitions = definitions;
        this.nodes = nodes;
    }

    @Override
    public List<WorkflowTransition> findByWorkflowDefinitionId(int workflowDefinitionId) {
        return transitions.findByWorkflowDefinitionId(workflowDefinitionId).stream()
                .map(WorkflowTransitionCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkflowTransition> findById(int id) {
        return transitions.findById(id).map(WorkflowTransitionCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return transitions.existsById(id);
    }

    @Override
    public WorkflowTransition save(WorkflowTransition transition) {
        com.licensis.notaire.business.WorkflowTransition entity = transition.id() == null
                ? new com.licensis.notaire.business.WorkflowTransition()
                : transitions
                        .findById(transition.id())
                        .orElseThrow(() -> new WorkflowTransitionNotFoundException(transition.id()));
        if (transition.workflowDefinitionId() != null) {
            WorkflowDefinition definition = definitions
                    .findById(transition.workflowDefinitionId())
                    .orElseThrow(() -> new WorkflowTransitionNotFoundException(transition.workflowDefinitionId()));
            entity.setWorkflowDefinition(definition);
        }
        if (transition.originNodeId() != null) {
            WorkflowNode origin = nodes.findById(transition.originNodeId())
                    .orElseThrow(() -> new WorkflowTransitionNotFoundException(transition.originNodeId()));
            entity.setOriginNode(origin);
        }
        if (transition.destinationNodeId() != null) {
            WorkflowNode destination = nodes.findById(transition.destinationNodeId())
                    .orElseThrow(() -> new WorkflowTransitionNotFoundException(transition.destinationNodeId()));
            entity.setDestinationNode(destination);
        }
        entity.setCondition(transition.condition());
        entity.setDescription(transition.description());
        return toDomain(transitions.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!transitions.existsById(id)) {
            return false;
        }
        transitions.deleteById(id);
        return true;
    }

    private static WorkflowTransition toDomain(com.licensis.notaire.business.WorkflowTransition entity) {
        WorkflowDefinition definition = entity.getWorkflowDefinition();
        WorkflowNode origin = entity.getOriginNode();
        WorkflowNode destination = entity.getDestinationNode();
        return WorkflowTransition.restore(
                entity.getId(),
                definition == null ? null : definition.getId(),
                origin == null ? null : origin.getId(),
                destination == null ? null : destination.getId(),
                entity.getCondition(),
                entity.getDescription(),
                entity.getVersion());
    }
}
