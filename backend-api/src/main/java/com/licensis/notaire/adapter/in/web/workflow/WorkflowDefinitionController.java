package com.licensis.notaire.adapter.in.web.workflow;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workflow-definition")
@Tag(name = "Workflow Definition", description = "CU70 - Management of workflows of estados")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionRepository repository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowTransitionRepository transitionRepository;

    public WorkflowDefinitionController(WorkflowDefinitionRepository repository,
            WorkflowNodeRepository nodeRepository,
            WorkflowTransitionRepository transitionRepository) {
        this.repository = repository;
        this.nodeRepository = nodeRepository;
        this.transitionRepository = transitionRepository;
    }

    @GetMapping
    @Operation(summary = "Get all workflows")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoWorkflowDefinition>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(WorkflowDefinition::toDto).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get workflow by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoWorkflowDefinition> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(wf -> ResponseEntity.ok(wf.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request: name is required"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create workflow")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowDefinition dto) {
        try {
            WorkflowDefinition entity = new WorkflowDefinition();
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setActive(dto.isActive());
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.toDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request: name is required"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update workflow")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoWorkflowDefinition dto) {
        Optional<WorkflowDefinition> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowDefinition entity = existing.get();
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setActive(dto.isActive());
            if (dto.getVersion() != null) {
                entity.setVersion(dto.getVersion());
            }
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workflow")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!nodeRepository.findByWorkflowDefinitionId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Cannot delete: the workflow has associated nodes."));
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
