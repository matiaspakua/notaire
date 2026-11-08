package com.licensis.notaire.adapter.in.web.procedure;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.application.port.in.procedure.ManageProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProcedureTemplatesUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.TemplateView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Procedure templates (CU55). Calls inbound ports only (issue #1798).
 */
@RestController
@RequestMapping("/api/v1/procedure-templates")
@Tag(name = "ProcedureTemplate", description = "API for procedure (documents by procedure type)")
public class ProcedureTemplateController {

    record ProcedureTemplatePkRequest(
            @JsonProperty("fkIdProcedureType") Integer fkIdProcedureType,
            @JsonProperty("fkIdDocumentType") Integer fkIdDocumentType) {}

    record ProcedureTemplateRequest(
            String notes,
            Integer fkIdProcedureType,
            Integer fkIdDocumentType,
            @JsonProperty("procedureTemplatePK") ProcedureTemplatePkRequest procedureTemplatePK) {}

    record ProcedureTemplateResponse(
            Integer fkIdProcedureType,
            Integer fkIdDocumentType,
            String notes,
            int version) {}

    private final QueryProcedureTemplatesUseCase queryProcedureTemplates;
    private final ManageProcedureTemplatesUseCase manageProcedureTemplates;

    public ProcedureTemplateController(
            QueryProcedureTemplatesUseCase queryProcedureTemplates,
            ManageProcedureTemplatesUseCase manageProcedureTemplates) {
        this.queryProcedureTemplates = queryProcedureTemplates;
        this.manageProcedureTemplates = manageProcedureTemplates;
    }

    private Integer resolveProcedureTypeId(ProcedureTemplateRequest request) {
        if (request.fkIdProcedureType() != null) {
            return request.fkIdProcedureType();
        }
        return request.procedureTemplatePK() != null
                ? request.procedureTemplatePK().fkIdProcedureType() : null;
    }

    private Integer resolveDocumentTypeId(ProcedureTemplateRequest request) {
        if (request.fkIdDocumentType() != null) {
            return request.fkIdDocumentType();
        }
        return request.procedureTemplatePK() != null
                ? request.procedureTemplatePK().fkIdDocumentType() : null;
    }

    private ProcedureTemplateResponse toResponse(TemplateView view) {
        return new ProcedureTemplateResponse(
                view.procedureTypeId(), view.documentTypeId(), view.notes(), view.version());
    }

    @GetMapping
    @Operation(summary = "Get all procedure templates")
    public ResponseEntity<List<ProcedureTemplateResponse>> getAll() {
        return ResponseEntity.ok(queryProcedureTemplates.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/procedure-types/{idProcedureType}")
    @Operation(summary = "Get procedure templates by procedure type")
    public ResponseEntity<List<ProcedureTemplateResponse>> getByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(queryProcedureTemplates.findByProcedureType(idProcedureType).stream()
                .map(this::toResponse)
                .toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Get template by key composite (CU55)")
    public ResponseEntity<ProcedureTemplateResponse> getById(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType) {
        return queryProcedureTemplates.findById(idProcedureType, idDocumentType)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create procedure template (CU55)")
    public ResponseEntity<ProcedureTemplateResponse> create(
            @Valid @RequestBody ProcedureTemplateRequest request) {
        return switch (manageProcedureTemplates.create(
                resolveProcedureTypeId(request), resolveDocumentTypeId(request), request.notes())) {
            case SaveResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Update procedure template (CU55)")
    public ResponseEntity<ProcedureTemplateResponse> update(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType,
            @Valid @RequestBody ProcedureTemplateRequest request) {
        return switch (manageProcedureTemplates.update(idProcedureType, idDocumentType, request.notes())) {
            case SaveResult.Updated updated -> ResponseEntity.ok(toResponse(updated.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Delete procedure template (CU55)")
    public ResponseEntity<Void> delete(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType) {
        return switch (manageProcedureTemplates.delete(idProcedureType, idDocumentType)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }
}
