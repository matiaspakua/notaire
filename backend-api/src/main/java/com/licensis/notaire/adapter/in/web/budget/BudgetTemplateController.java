package com.licensis.notaire.adapter.in.web.budget;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.application.port.in.budget.ManageBudgetTemplatesUseCase;
import com.licensis.notaire.application.port.in.budget.QueryBudgetTemplatesUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetTemplateOpsPort.TemplateView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for BudgetTemplate (Budget Templates).
 *
 * Covers use cases: CU39, CU49, CU55. Calls inbound ports only; persistence and
 * business rules live behind {@code BudgetTemplateOpsPort} (issue #1803).
 */
@RestController
@RequestMapping("/api/v1/budget-templates")
@Tag(name = "BudgetTemplate", description = "API for managing budget templates")
public class BudgetTemplateController {

    record BudgetTemplatePkRequest(
            @JsonProperty("fkIdProcedureType") Integer fkIdProcedureType,
            @JsonProperty("fkIdConcept") Integer fkIdConcept) {}

    record BudgetTemplateRequest(
            String notes,
            Integer fkIdProcedureType,
            Integer fkIdConcept,
            @JsonProperty("budgetTemplatePK") BudgetTemplatePkRequest budgetTemplatePK) {}

    record BudgetTemplateResponse(
            Integer fkIdProcedureType,
            Integer fkIdConcept,
            String notes,
            int version) {}

    private final QueryBudgetTemplatesUseCase queryBudgetTemplates;
    private final ManageBudgetTemplatesUseCase manageBudgetTemplates;

    public BudgetTemplateController(
            QueryBudgetTemplatesUseCase queryBudgetTemplates,
            ManageBudgetTemplatesUseCase manageBudgetTemplates) {
        this.queryBudgetTemplates = queryBudgetTemplates;
        this.manageBudgetTemplates = manageBudgetTemplates;
    }

    private Integer resolveProcedureTypeId(BudgetTemplateRequest request) {
        if (request.fkIdProcedureType() != null) {
            return request.fkIdProcedureType();
        }
        return request.budgetTemplatePK() != null
                ? request.budgetTemplatePK().fkIdProcedureType() : null;
    }

    private Integer resolveConceptId(BudgetTemplateRequest request) {
        if (request.fkIdConcept() != null) {
            return request.fkIdConcept();
        }
        return request.budgetTemplatePK() != null
                ? request.budgetTemplatePK().fkIdConcept() : null;
    }

    private BudgetTemplateResponse toResponse(TemplateView view) {
        return new BudgetTemplateResponse(
                view.procedureTypeId(),
                view.conceptId(),
                view.notes(),
                view.version());
    }

    @GetMapping
    @Operation(summary = "Get all budget templates")
    public ResponseEntity<List<BudgetTemplateResponse>> getAll() {
        return ResponseEntity.ok(queryBudgetTemplates.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/procedure-types/{idProcedureType}")
    @Operation(summary = "Get budget templates by procedure type")
    public ResponseEntity<List<BudgetTemplateResponse>> getByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(queryBudgetTemplates.findByProcedureType(idProcedureType).stream()
                .map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create budget template")
    public ResponseEntity<?> create(@Valid @RequestBody BudgetTemplateRequest request) {
        TemplateView created = manageBudgetTemplates.create(
                resolveProcedureTypeId(request), resolveConceptId(request), request.notes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "fkIdProcedureType", created.procedureTypeId(),
                        "fkIdConcept", created.conceptId()));
    }

    @PutMapping("/procedure-types/{idProcedureType}/concept/{idConcept}")
    @Operation(summary = "Update budget template")
    public ResponseEntity<?> update(
            @PathVariable Integer idProcedureType,
            @PathVariable Integer idConcept,
            @Valid @RequestBody BudgetTemplateRequest request) {
        manageBudgetTemplates.updateNotes(idProcedureType, idConcept, request.notes());
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/procedure-types/{idProcedureType}/concept/{idConcept}")
    @Operation(summary = "Delete budget template")
    public ResponseEntity<?> delete(
            @PathVariable Integer idProcedureType, @PathVariable Integer idConcept) {
        manageBudgetTemplates.delete(idProcedureType, idConcept);
        return ResponseEntity.noContent().build();
    }
}
