package com.licensis.notaire.application.usecase.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.QueryWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;

@Service
@Transactional(readOnly = true)
public class WorkflowDefinitionQueryService implements QueryWorkflowDefinitionsUseCase {

    private final WorkflowDefinitionCatalogPort catalog;

    public WorkflowDefinitionQueryService(WorkflowDefinitionCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return catalog.findAll();
    }

    @Override
    public Optional<WorkflowDefinition> findById(int id) {
        return catalog.findById(id);
    }
}
