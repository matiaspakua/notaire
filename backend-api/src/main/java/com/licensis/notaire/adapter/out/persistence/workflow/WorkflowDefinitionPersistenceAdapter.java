package com.licensis.notaire.adapter.out.persistence.workflow;

import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionRepositoryPort;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the workflow definition slice.
 */
@Component
public class WorkflowDefinitionPersistenceAdapter implements WorkflowDefinitionRepositoryPort {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public WorkflowDefinitionPersistenceAdapter(WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return workflowDefinitionRepository.findAll();
    }

    @Override
    public Optional<WorkflowDefinition> findById(Integer id) {
        return workflowDefinitionRepository.findById(id);
    }

    @Override
    public WorkflowDefinition save(WorkflowDefinition entity) {
        return workflowDefinitionRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        workflowDefinitionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return workflowDefinitionRepository.existsById(id);
    }
}
