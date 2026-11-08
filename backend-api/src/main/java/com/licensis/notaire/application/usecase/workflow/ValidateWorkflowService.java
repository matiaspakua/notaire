package com.licensis.notaire.application.usecase.workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase;
import com.licensis.notaire.application.port.out.workflow.WorkflowDefinitionCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowNodeCatalogPort;
import com.licensis.notaire.application.port.out.workflow.WorkflowTransitionCatalogPort;
import com.licensis.notaire.application.usecase.workflow.WorkflowValidationService.ValidationResult;

/**
 * Orchestrates workflow validation via catalog ports (issue #1770).
 */
@Service
@Transactional(readOnly = true)
public class ValidateWorkflowService implements ValidateWorkflowUseCase {

    private final WorkflowDefinitionCatalogPort definitions;
    private final WorkflowNodeCatalogPort nodes;
    private final WorkflowTransitionCatalogPort transitions;
    private final WorkflowValidationService validation;

    public ValidateWorkflowService(
            WorkflowDefinitionCatalogPort definitions,
            WorkflowNodeCatalogPort nodes,
            WorkflowTransitionCatalogPort transitions,
            WorkflowValidationService validation) {
        this.definitions = definitions;
        this.nodes = nodes;
        this.transitions = transitions;
        this.validation = validation;
    }

    @Override
    public Result validate(int workflowDefinitionId) {
        if (!definitions.existsById(workflowDefinitionId)) {
            return new Result.NotFound();
        }
        ValidationResult result = validation.validate(
                nodes.findByWorkflowDefinitionId(workflowDefinitionId),
                transitions.findByWorkflowDefinitionId(workflowDefinitionId));
        return new Result.Ok(result.isValid(), result.getErrors());
    }
}
