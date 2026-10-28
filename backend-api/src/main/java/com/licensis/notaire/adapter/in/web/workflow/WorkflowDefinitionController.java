package com.licensis.notaire.adapter.in.web.workflow;

import java.util.List;
import java.util.Map;

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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowDefinitionsUseCase.SaveWorkflowDefinitionCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowDefinitionsUseCase;
import com.licensis.notaire.domain.workflow.WorkflowDefinition;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionInUseException;
import com.licensis.notaire.domain.workflow.WorkflowDefinitionNotFoundException;
import com.licensis.notaire.dto.DtoWorkflowDefinition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Workflow definitions. Calls inbound ports only; delete-guard for associated nodes lives in the use case
 * (issue #1728).
 */
@RestController
@RequestMapping("/api/v1/workflow-definition")
@Tag(name = "Workflow Definition", description = "CU70 - Management of status workflows")
public class WorkflowDefinitionController {

    public record WorkflowDefinitionWriteRequest(
            @NotBlank String name, String description, Boolean active, Integer version) { }

    private final QueryWorkflowDefinitionsUseCase query;
    private final ManageWorkflowDefinitionsUseCase manage;

    public WorkflowDefinitionController(
            QueryWorkflowDefinitionsUseCase query, ManageWorkflowDefinitionsUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoWorkflowDefinition toDto(WorkflowDefinition definition) {
        DtoWorkflowDefinition dto = new DtoWorkflowDefinition();
        dto.setId(definition.id());
        dto.setName(definition.name());
        dto.setDescription(definition.description());
        dto.setActive(definition.active());
        dto.setVersion(definition.version());
        return dto;
    }

    private SaveWorkflowDefinitionCommand toCommand(WorkflowDefinitionWriteRequest request) {
        boolean active = request.active() == null || request.active();
        int version = request.version() == null ? 0 : request.version();
        return new SaveWorkflowDefinitionCommand(request.name(), request.description(), active, version);
    }

    @GetMapping
    @Operation(summary = "Get all workflows")
    public ResponseEntity<List<DtoWorkflowDefinition>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toDto).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get workflow by ID")
    public ResponseEntity<DtoWorkflowDefinition> getById(@PathVariable Integer id) {
        return query.findById(id)
                .map(wf -> ResponseEntity.ok(toDto(wf)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request: name is required"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create workflow")
    public ResponseEntity<Object> create(@Valid @RequestBody WorkflowDefinitionWriteRequest request) {
        try {
            WorkflowDefinition saved = manage.create(toCommand(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request: name is required"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(
                responseCode = "409",
                description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update workflow")
    public ResponseEntity<Object> update(
            @PathVariable Integer id, @Valid @RequestBody WorkflowDefinitionWriteRequest request) {
        try {
            manage.update(id, toCommand(request));
            return ResponseEntity.ok().build();
        } catch (WorkflowDefinitionNotFoundException e) {
            return ResponseEntity.notFound().build();
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
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (WorkflowDefinitionNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (WorkflowDefinitionInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}
