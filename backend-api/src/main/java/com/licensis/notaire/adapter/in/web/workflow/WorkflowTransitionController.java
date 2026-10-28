package com.licensis.notaire.adapter.in.web.workflow;

import java.util.List;

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
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase.CreateWorkflowTransitionCommand;
import com.licensis.notaire.application.port.in.workflow.ManageWorkflowTransitionsUseCase.UpdateWorkflowTransitionCommand;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTransitionsUseCase;
import com.licensis.notaire.domain.workflow.WorkflowTransition;
import com.licensis.notaire.domain.workflow.WorkflowTransitionInvalidException;
import com.licensis.notaire.domain.workflow.WorkflowTransitionNotFoundException;
import com.licensis.notaire.dto.DtoWorkflowTransition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Workflow transitions. Calls inbound ports only; workflow and node reference checks live in the use
 * case (issue #1730).
 */
@RestController
@RequestMapping("/api/v1/workflow-transition")
@Tag(name = "Workflow Transition", description = "CU71 - Transitions between statuses of a workflow")
public class WorkflowTransitionController {

    public record WorkflowTransitionCreateRequest(
            @NotNull Integer workflowDefinitionId,
            @NotNull Integer originNodeId,
            @NotNull Integer destinationNodeId,
            String condition,
            String description) {}

    public record WorkflowTransitionUpdateRequest(
            @NotNull Integer originNodeId,
            @NotNull Integer destinationNodeId,
            String condition,
            String description) {}

    private final QueryWorkflowTransitionsUseCase query;
    private final ManageWorkflowTransitionsUseCase manage;

    public WorkflowTransitionController(
            QueryWorkflowTransitionsUseCase query, ManageWorkflowTransitionsUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private DtoWorkflowTransition toDto(WorkflowTransition transition) {
        DtoWorkflowTransition dto = new DtoWorkflowTransition();
        dto.setId(transition.id());
        dto.setWorkflowDefinitionId(transition.workflowDefinitionId());
        dto.setOriginNodeId(transition.originNodeId());
        dto.setDestinationNodeId(transition.destinationNodeId());
        dto.setCondition(transition.condition());
        dto.setDescription(transition.description());
        dto.setVersion(transition.version());
        return dto;
    }

    @GetMapping("/by-workflow/{workflowId}")
    @Operation(summary = "Get transitions by workflow")
    public ResponseEntity<List<DtoWorkflowTransition>> getByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(
                query.findByWorkflowDefinitionId(workflowId).stream().map(this::toDto).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get transition by ID")
    public ResponseEntity<DtoWorkflowTransition> getById(@PathVariable Integer id) {
        return query.findById(id)
                .map(t -> ResponseEntity.ok(toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request: workflowDefinitionId, originNodeId and "
                        + "destinationNodeId are required"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create a transition between two nodes")
    public ResponseEntity<Object> create(@Valid @RequestBody WorkflowTransitionCreateRequest request) {
        try {
            WorkflowTransition saved = manage.create(new CreateWorkflowTransitionCommand(
                    request.workflowDefinitionId(),
                    request.originNodeId(),
                    request.destinationNodeId(),
                    request.condition(),
                    request.description()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (WorkflowTransitionInvalidException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request: originNodeId and destinationNodeId are required"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update transition")
    public ResponseEntity<Object> update(
            @PathVariable Integer id, @Valid @RequestBody WorkflowTransitionUpdateRequest request) {
        try {
            WorkflowTransition saved = manage.update(
                    id,
                    new UpdateWorkflowTransitionCommand(
                            request.originNodeId(),
                            request.destinationNodeId(),
                            request.condition(),
                            request.description()));
            return ResponseEntity.ok(toDto(saved));
        } catch (WorkflowTransitionNotFoundException | WorkflowTransitionInvalidException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // A data constraint answers 400; any other failure keeps 409 (issue #579, slice 3).
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transition")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (WorkflowTransitionNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
