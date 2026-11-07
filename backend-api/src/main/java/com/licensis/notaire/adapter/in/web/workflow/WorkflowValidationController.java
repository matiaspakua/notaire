package com.licensis.notaire.adapter.in.web.workflow;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase;
import com.licensis.notaire.application.port.in.workflow.ValidateWorkflowUseCase.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Workflow consistency validation. Calls inbound ports only (issue #1770).
 */
@RestController
@RequestMapping("/api/v1/workflow-definition")
@Tag(name = "Workflow Validation", description = "CU72 - Workflow consistency validation")
public class WorkflowValidationController {

    /** JSON body for {@code POST …/validate} — same keys clients already consume (#577). */
    public record WorkflowValidationResponse(boolean valid, List<String> errors) {
    }

    private final ValidateWorkflowUseCase validateWorkflow;

    public WorkflowValidationController(ValidateWorkflowUseCase validateWorkflow) {
        this.validateWorkflow = validateWorkflow;
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate the consistency of a workflow")
    public ResponseEntity<WorkflowValidationResponse> validate(@PathVariable Integer id) {
        return switch (validateWorkflow.validate(id)) {
            case Result.Ok ok -> ResponseEntity.ok(new WorkflowValidationResponse(ok.valid(), ok.errors()));
            case Result.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }
}
