package com.licensis.notaire.adapter.out.persistence.workflow;

import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionRepositoryPort;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the workflow transition slice.
 */
@Component
public class WorkflowTransitionPersistenceAdapter implements WorkflowTransitionRepositoryPort {

    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowTransitionPersistenceAdapter(WorkflowTransitionRepository workflowTransitionRepository) {
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    @Override
    public List<WorkflowTransition> findAll() {
        return workflowTransitionRepository.findAll();
    }

    @Override
    public Optional<WorkflowTransition> findById(Integer id) {
        return workflowTransitionRepository.findById(id);
    }

    @Override
    public WorkflowTransition save(WorkflowTransition entity) {
        return workflowTransitionRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        workflowTransitionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return workflowTransitionRepository.existsById(id);
    }

    @Override
    public List<WorkflowTransition> findByWorkflowDefinitionId(Integer workflowDefinitionId) {
        return workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinitionId);
    }
}
