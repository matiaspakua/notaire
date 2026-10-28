package com.licensis.notaire.adapter.in.web.procedure;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureFoldersUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureFoldersUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureFolderOpsPort.HoldResult;
import com.licensis.notaire.dto.DtoProcedureFolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API for managing procedure folders (CU85). Calls inbound ports only (issue #1795).
 */
@RestController
@RequestMapping("/api/v1/procedure-folders")
@Tag(name = "Procedure folders", description = "API for managing procedure folders")
public class ProcedureFolderController {

    private final QueryProcedureFoldersUseCase queryProcedureFolders;
    private final ManageProcedureFoldersUseCase manageProcedureFolders;

    public ProcedureFolderController(
            QueryProcedureFoldersUseCase queryProcedureFolders,
            ManageProcedureFoldersUseCase manageProcedureFolders) {
        this.queryProcedureFolders = queryProcedureFolders;
        this.manageProcedureFolders = manageProcedureFolders;
    }

    public record WaitRequest(@NotBlank String reason) { }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU85 - Query a procedure folder by ID")
    public ResponseEntity<DtoProcedureFolder> getById(@PathVariable Integer id) {
        return queryProcedureFolders.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    @Operation(summary = "CU85 - Query procedure folders by management or by procedure")
    public ResponseEntity<List<DtoProcedureFolder>> search(
            @RequestParam(required = false) Integer managementId,
            @RequestParam(required = false) Integer procedureId) {
        return ResponseEntity.ok(queryProcedureFolders.search(managementId, procedureId));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folder put on hold"),
        @ApiResponse(responseCode = "400", description = "Reason not provided"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    @PutMapping("/{id}/wait")
    @Operation(summary = "CU85 - Put a procedure folder on hold; a reason is required")
    public ResponseEntity<Object> putOnHold(@PathVariable Integer id, @Valid @RequestBody WaitRequest request) {
        return switch (manageProcedureFolders.putOnHold(id, request.reason())) {
            case HoldResult.Ok ok -> ResponseEntity.ok(ok.dto());
            case HoldResult.NotFound ignored -> ResponseEntity.notFound().build();
            case HoldResult.BadRequest bad -> ResponseEntity.badRequest().body(Map.of("message", bad.message()));
        };
    }
}
