package com.licensis.notaire.api;

import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.WorkflowValidationService;
import com.licensis.notaire.service.WorkflowValidationService.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflow-definition")
@Tag(name = "Workflow Validation", description = "CU72 - Validación de consistencia del workflow")
public class WorkflowValidationController {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowValidationService validationService;

    public WorkflowValidationController(WorkflowDefinitionRepository workflowRepository,
            WorkflowNodeRepository nodeRepository,
            WorkflowTransitionRepository transitionRepository,
            WorkflowValidationService validationService) {
        this.workflowRepository = workflowRepository;
        this.nodeRepository = nodeRepository;
        this.transitionRepository = transitionRepository;
        this.validationService = validationService;
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validar consistencia de un workflow")
    public ResponseEntity<Map<String, Object>> validate(@PathVariable Integer id) {
        if (!workflowRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ValidationResult result = validationService.validate(
                nodeRepository.findByWorkflowDefinitionId(id),
                transitionRepository.findByWorkflowDefinitionId(id));
        return ResponseEntity.ok(Map.of(
                "valid", result.isValid(),
                "errors", result.getErrors()));
    }
}
