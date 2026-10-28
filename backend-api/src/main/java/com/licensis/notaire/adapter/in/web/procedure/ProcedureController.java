package com.licensis.notaire.adapter.in.web.procedure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.licensis.notaire.adapter.in.web.support.DeedRef;
import com.licensis.notaire.adapter.in.web.support.ManagementRef;
import com.licensis.notaire.adapter.in.web.support.ProcedureTypeRef;
import com.licensis.notaire.adapter.in.web.support.PropertyRef;
import com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase;
import com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.BudgetView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeedView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ManagementView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureCommand;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureTypeView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.PropertyView;
import com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Procedures. Calls inbound ports only; hydration and persistence live behind
 * {@code ProcedureOpsPort} (issue #1801).
 */
@RestController
@RequestMapping("/api/v1/procedures")
@Tag(name = "Procedures", description = "API for managing procedures")
public class ProcedureController {

    /**
     * Plain-id request contract for create/update, so every referenced
     * association is re-fetched from its own persisted row instead of
     * trusting a client-supplied nested object (issue #981).
     */
    public record BudgetRef(Integer idBudget) {
    }

    public record ProcedureResponse(Integer idProcedure, String notes, ProcedureTypeRef fkIdProcedureType,
            PropertyRef fkIdProperty, BudgetRef fkIdBudget, DeedRef fkIdDeed, ManagementRef fkIdManagement,
            int version) {
    }

    /**
     * Body of create and full update. {@code idProcedureType} is required on both (issue #655,
     * Owner decision Oct 9: an empty or incomplete PUT answers 400). {@code notes} is stored as
     * sent; an absent association id keeps the stored association.
     */
    public record ProcedureRequest(
            @NotNull
            @Schema(description = "Procedure type; must existir (404 if not)",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            Integer idProcedureType,
            Integer idProperty, Integer idDeed, Integer idManagement, Integer idBudget, String notes) {
    }

    private final QueryProceduresUseCase queryProcedures;
    private final ManageProceduresUseCase manageProcedures;

    public ProcedureController(QueryProceduresUseCase queryProcedures, ManageProceduresUseCase manageProcedures) {
        this.queryProcedures = queryProcedures;
        this.manageProcedures = manageProcedures;
    }

    @GetMapping
    @Operation(summary = "Get all procedures")
    public ResponseEntity<Page<ProcedureResponse>> getAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? "idProcedure,asc"
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryProcedures.findAll(pageable.getPageNumber(), pageable.getPageSize(), sort);
        var content = result.content().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new PageImpl<>(content, pageable, result.totalElements()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get procedure by ID")
    public ResponseEntity<ProcedureResponse> getById(@PathVariable Integer id) {
        return queryProcedures.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request: idProcedureType is required"),
            @ApiResponse(responseCode = "404", description = "Reference not found"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create procedure")
    public ResponseEntity<Object> create(@Valid @RequestBody ProcedureRequest request) {
        return switch (manageProcedures.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Empty or incomplete body: idProcedureType is required"),
            @ApiResponse(responseCode = "404", description = "Procedure or reference not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update procedure",
            description = "idProcedureType is required (400 if it is missing); notes are stored as sent and "
                    + "a missing association keeps the current one.")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody ProcedureRequest request) {
        return switch (manageProcedures.update(id, toCommand(request))) {
            case SaveResult.Updated updated -> ResponseEntity.ok(toResponse(updated.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete procedure")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageProcedures.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    private ProcedureCommand toCommand(ProcedureRequest request) {
        return new ProcedureCommand(
                request.idProcedureType(),
                request.idProperty(),
                request.idDeed(),
                request.idManagement(),
                request.idBudget(),
                request.notes());
    }

    private ProcedureResponse toResponse(ProcedureView view) {
        return new ProcedureResponse(
                view.idProcedure(),
                view.notes(),
                toProcedureTypeRef(view.procedureType()),
                toPropertyRef(view.property()),
                toBudgetRef(view.budget()),
                toDeedRef(view.deed()),
                toManagementRef(view.management()),
                view.version());
    }

    private static ProcedureTypeRef toProcedureTypeRef(ProcedureTypeView type) {
        return type == null
                ? null
                : new ProcedureTypeRef(
                        type.idProcedureType(),
                        type.name(),
                        type.notes(),
                        type.isArchived(),
                        type.isRegistered(),
                        type.associatesProperties(),
                        type.enabled());
    }

    private static PropertyRef toPropertyRef(PropertyView property) {
        return property == null
                ? null
                : new PropertyRef(
                        property.idProperty(),
                        property.cadastralDesignation(),
                        property.address(),
                        property.registrationNumber());
    }

    private static BudgetRef toBudgetRef(BudgetView budget) {
        return budget == null ? null : new BudgetRef(budget.idBudget());
    }

    private static DeedRef toDeedRef(DeedView deed) {
        return deed == null ? null : new DeedRef(deed.idDeed(), deed.number(), deed.status());
    }

    private static ManagementRef toManagementRef(ManagementView management) {
        return management == null
                ? null
                : new ManagementRef(management.idManagement(), management.number(), management.heading());
    }
}
