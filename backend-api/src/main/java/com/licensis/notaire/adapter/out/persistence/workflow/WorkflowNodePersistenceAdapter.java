package com.licensis.notaire.adapter.out.persistence.workflow;

import com.licensis.notaire.application.port.out.workflow.WorkflowNodeRepositoryPort;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the workflow node slice.
 */
@Component
public class WorkflowNodePersistenceAdapter implements WorkflowNodeRepositoryPort {

    private final WorkflowNodeRepository workflowNodeRepository;

    public WorkflowNodePersistenceAdapter(WorkflowNodeRepository workflowNodeRepository) {
        this.workflowNodeRepository = workflowNodeRepository;
    }

    @Override
    public List<WorkflowNode> findAll() {
        return workflowNodeRepository.findAll();
    }

    @Override
    public Optional<WorkflowNode> findById(Integer id) {
        return workflowNodeRepository.findById(id);
    }

    @Override
    public WorkflowNode save(WorkflowNode entity) {
        return workflowNodeRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        workflowNodeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return workflowNodeRepository.existsById(id);
    }

    @Override
    public List<WorkflowNode> findByWorkflowDefinitionId(Integer workflowDefinitionId) {
        return workflowNodeRepository.findByWorkflowDefinitionId(workflowDefinitionId);
    }
}
