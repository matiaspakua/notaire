package com.licensis.notaire.application.usecase.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTransitionsUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowTransition;

@Service
@Transactional(readOnly = true)
public class WorkflowTransitionQueryService implements QueryWorkflowTransitionsUseCase {

    private final WorkflowTransitionCatalogPort catalog;

    public WorkflowTransitionQueryService(WorkflowTransitionCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<WorkflowTransition> findByWorkflowDefinitionId(int workflowDefinitionId) {
        return catalog.findByWorkflowDefinitionId(workflowDefinitionId);
    }

    @Override
    public Optional<WorkflowTransition> findById(int id) {
        return catalog.findById(id);
    }
}
