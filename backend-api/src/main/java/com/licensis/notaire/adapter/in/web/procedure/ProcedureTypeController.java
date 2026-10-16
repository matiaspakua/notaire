package com.licensis.notaire.adapter.in.web.procedure;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.WorkflowAssignResult;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.exception.BusinessValidationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Procedure types. Calls inbound ports only; orchestration and persistence live in the use cases
 * (issue #1791).
 */
@RestController
@RequestMapping("/api/v1/procedure-types")
@Tag(name = "Procedure type", description = "API for procedure types")
public class ProcedureTypeController {

    private final QueryProcedureTypesUseCase queryProcedureTypes;
    private final ManageProcedureTypesUseCase manageProcedureTypes;

    public ProcedureTypeController(
            QueryProcedureTypesUseCase queryProcedureTypes, ManageProcedureTypesUseCase manageProcedureTypes) {
        this.queryProcedureTypes = queryProcedureTypes;
        this.manageProcedureTypes = manageProcedureTypes;
    }

    @GetMapping
    @Operation(summary = "Get all procedure types")
    public ResponseEntity<List<DtoProcedureType>> getAll() {
        return ResponseEntity.ok(queryProcedureTypes.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Search procedure types by name")
    public ResponseEntity<List<DtoProcedureType>> search(@RequestParam String name) {
        return ResponseEntity.ok(queryProcedureTypes.searchByName(name));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get procedure type by ID")
    public ResponseEntity<DtoProcedureType> getById(@PathVariable Integer id) {
        return queryProcedureTypes.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether the procedure type is in use")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return queryProcedureTypes.isInUse(id)
                .map(inUse -> ResponseEntity.ok(Map.of("inUse", inUse)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create procedure type")
    public ResponseEntity<Object> create(@RequestBody DtoProcedureType dto) {
        return switch (manageProcedureTypes.create(dto)) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(created.dto());
            case SaveResult.Failed failed -> ErrorResponses.createFailed(failed.cause());
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.InUse inUse -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.message()));
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update procedure type")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoProcedureType dto) {
        return switch (manageProcedureTypes.update(id, dto)) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.InUse inUse -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.message()));
            case SaveResult.Failed failed -> ErrorResponses.updateFailed(failed.cause());
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete procedure type")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        return switch (manageProcedureTypes.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.InUse inUse -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.message()));
        };
    }

    /**
     * Documented shape of the workflow assignment body. The handler reads a map so that a missing
     * {@code workflowDefinitionId} (400) can be told apart from an explicit {@code null}
     * (unassign); issue #655, Owner decision Oct 9.
     */
    @Schema(name = "WorkflowAssignmentRequest")
    public record WorkflowAssignmentRequest(
            @Schema(description = "Active workflow to assign, or null to unassign the current one",
                    requiredMode = Schema.RequiredMode.REQUIRED, nullable = true)
            Integer workflowDefinitionId) {
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400",
                    description = "Empty or invalid body: workflowDefinitionId is required (an integer, or null to unassign)"),
            @ApiResponse(responseCode = "404", description = "Procedure type or workflow not found"),
            @ApiResponse(responseCode = "409", description = "The selected workflow is not active")
    })
    @PutMapping("/{id}/workflow")
    @Operation(summary = "Assign or unassign a workflow on a procedure type")
    public ResponseEntity<Object> assignWorkflow(@PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WorkflowAssignmentRequest.class)))
            @RequestBody Map<String, Object> body) {
        RequiredFields.check().present(body.containsKey("workflowDefinitionId"), "workflowDefinitionId").orThrow();
        Object wfIdObj = body.get("workflowDefinitionId");
        if (wfIdObj != null && !(wfIdObj instanceof Integer)) {
            throw new BusinessValidationException("workflowDefinitionId: must be an integer or null");
        }
        Integer wfId = (Integer) wfIdObj;
        return switch (manageProcedureTypes.assignWorkflow(id, wfId)) {
            case WorkflowAssignResult.Ok ok -> ResponseEntity.ok(ok.dto());
            case WorkflowAssignResult.NotFound ignored -> ResponseEntity.notFound().build();
            case WorkflowAssignResult.Inactive ignored -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ProcedureTypeOpsPort.WORKFLOW_INACTIVE_MESSAGE));
        };
    }
}
