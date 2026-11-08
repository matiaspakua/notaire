package com.licensis.notaire.adapter.out.persistence.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;

/**
 * Maps the legacy {@code business.WorkflowDefinition} entity to the pure domain type (issue #1728).
 */
@Component
public class WorkflowDefinitionCatalogPersistenceAdapter implements WorkflowDefinitionCatalogPort {

    private final WorkflowDefinitionRepository definitions;
    private final WorkflowNodeRepository nodes;

    public WorkflowDefinitionCatalogPersistenceAdapter(
            WorkflowDefinitionRepository definitions, WorkflowNodeRepository nodes) {
        this.definitions = definitions;
        this.nodes = nodes;
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return definitions.findAll().stream().map(WorkflowDefinitionCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<WorkflowDefinition> findById(int id) {
        return definitions.findById(id).map(WorkflowDefinitionCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return definitions.existsById(id);
    }

    @Override
    public boolean hasNodes(int id) {
        return !nodes.findByWorkflowDefinitionId(id).isEmpty();
    }

    @Override
    public WorkflowDefinition save(WorkflowDefinition definition) {
        com.licensis.notaire.business.WorkflowDefinition entity = definition.id() == null
                ? new com.licensis.notaire.business.WorkflowDefinition()
                : definitions
                        .findById(definition.id())
                        .orElseThrow(() -> new WorkflowDefinitionNotFoundException(definition.id()));
        entity.setName(definition.name());
        entity.setDescription(definition.description());
        entity.setActive(definition.active());
        if (definition.id() != null) {
            entity.setVersion(definition.version());
        }
        return toDomain(definitions.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!definitions.existsById(id)) {
            return false;
        }
        definitions.deleteById(id);
        return true;
    }

    private static WorkflowDefinition toDomain(com.licensis.notaire.business.WorkflowDefinition entity) {
        return WorkflowDefinition.restore(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getVersion());
    }
}
