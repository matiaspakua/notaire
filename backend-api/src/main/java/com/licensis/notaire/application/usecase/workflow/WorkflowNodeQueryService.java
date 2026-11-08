package com.licensis.notaire.application.usecase.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.QueryWorkflowNodesUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.domain.workflow.WorkflowNode;

@Service
@Transactional(readOnly = true)
public class WorkflowNodeQueryService implements QueryWorkflowNodesUseCase {

    private final WorkflowNodeCatalogPort catalog;

    public WorkflowNodeQueryService(WorkflowNodeCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<WorkflowNode> findByWorkflowDefinitionId(int workflowDefinitionId) {
        return catalog.findByWorkflowDefinitionId(workflowDefinitionId);
    }

    @Override
    public Optional<WorkflowNode> findById(int id) {
        return catalog.findById(id);
    }
}
