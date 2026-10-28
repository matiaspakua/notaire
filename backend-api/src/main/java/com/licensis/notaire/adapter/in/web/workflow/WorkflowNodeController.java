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
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.CreateWorkflowNodeCommand;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowNodesUseCase.UpdateWorkflowNodeCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowNodesUseCase;
import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeInUseException;
import com.licensis.notaire.domain.workflow.WorkflowNodeInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowNodeNotFoundException;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.exception.BusinessValidationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Workflow nodes. Calls inbound ports only; delete-guard for associated transitions lives in the use case
 * (issue #1729).
 */
@RestController
@RequestMapping("/api/v1/workflow-node")
@Tag(name = "Workflow Node", description = "CU70/CU71 - Workflow status nodes")
public class WorkflowNodeController {

    public record WorkflowNodeCreateRequest(
            @NotNull Integer workflowDefinitionId,
            @NotNull Integer statusManagementId,
            @NotBlank String type,
            Float positionX,
            Float positionY) {}

    private final QueryWorkflowNodesUseCase query;
    private final ManageWorkflowNodesUseCase manage;

    public WorkflowNodeController(QueryWorkflowNodesUseCase query, ManageWorkflowNodesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoWorkflowNode toDto(WorkflowNode node) {
        DtoWorkflowNode dto = new DtoWorkflowNode();
        dto.setId(node.id());
        dto.setWorkflowDefinitionId(node.workflowDefinitionId());
        dto.setStatusManagementId(node.statusManagementId());
        dto.setStatusManagementName(node.statusManagementName());
        dto.setType(node.kind() == null ? null : node.kind().name());
        dto.setPositionX(node.positionX());
        dto.setPositionY(node.positionY());
        dto.setVersion(node.version());
        return dto;
    }

    @GetMapping("/by-workflow/{workflowId}")
    @Operation(summary = "Get nodes by workflow")
    public ResponseEntity<List<DtoWorkflowNode>> getByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(
                query.findByWorkflowDefinitionId(workflowId).stream().map(this::toDto).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get node by ID")
    public ResponseEntity<DtoWorkflowNode> getById(@PathVariable Integer id) {
        return query.findById(id)
                .map(n -> ResponseEntity.ok(toDto(n)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request: workflowDefinitionId, statusManagementId "
                        + "and type (INITIAL, INTERMEDIATE or FINAL) are required"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create node in a workflow")
    public ResponseEntity<Object> create(@Valid @RequestBody WorkflowNodeCreateRequest request) {
        WorkflowNodeKind kind = RequiredFields.requireEnum(request.type(), WorkflowNodeKind.class, "type");
        try {
            WorkflowNode saved = manage.create(new CreateWorkflowNodeCommand(
                    request.workflowDefinitionId(),
                    request.statusManagementId(),
                    kind,
                    request.positionX(),
                    request.positionY()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (WorkflowNodeInvalidException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request: provide at least type, positionX, or "
                        + "positionY; type must be INITIAL, INTERMEDIATE, or FINAL"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update node")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody DtoWorkflowNode dto) {
        if (dto.getType() == null && dto.getPositionX() == null && dto.getPositionY() == null) {
            throw new BusinessValidationException(
                    "type, positionX, positionY: provide at least one field to update");
        }
        WorkflowNodeKind kind = dto.getType() == null
                ? null
                : RequiredFields.parseEnum(dto.getType(), WorkflowNodeKind.class, "type");
        try {
            manage.update(id, new UpdateWorkflowNodeCommand(kind, dto.getPositionX(), dto.getPositionY()));
            return ResponseEntity.ok().build();
        } catch (WorkflowNodeNotFoundException e) {
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
    @Operation(summary = "Delete node")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (WorkflowNodeNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (WorkflowNodeInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}
