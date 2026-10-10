package com.licensis.notaire.adapter.in.web.procedure;

import com.licensis.notaire.adapter.in.web.support.DeedRef;
import com.licensis.notaire.adapter.in.web.support.ManagementRef;
import com.licensis.notaire.adapter.in.web.support.ProcedureTypeRef;
import com.licensis.notaire.adapter.in.web.support.PropertyRef;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.application.port.out.budget.BudgetRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeRepositoryPort;
import com.licensis.notaire.application.port.out.property.PropertyRepositoryPort;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.DeedRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/v1/procedures")
@Tag(name = "Procedures", description = "API for managing procedures")
public class ProcedureController {

    private static final Logger log = LoggerFactory.getLogger(ProcedureController.class);

    private final ProcedureRepositoryPort repository;
    private final ProcedureTypeRepositoryPort procedureTypeRepository;
    private final PropertyRepositoryPort propertyRepository;
    private final BudgetRepositoryPort budgetRepository;
    private final DeedRepository deedRepository;
    private final DeedManagementRepository deedManagementRepository;

    public ProcedureController(ProcedureRepositoryPort repository,
            ProcedureTypeRepositoryPort procedureTypeRepository,
            PropertyRepositoryPort propertyRepository,
            BudgetRepositoryPort budgetRepository,
            DeedRepository deedRepository,
            DeedManagementRepository deedManagementRepository) {
        this.repository = repository;
        this.procedureTypeRepository = procedureTypeRepository;
        this.propertyRepository = propertyRepository;
        this.budgetRepository = budgetRepository;
        this.deedRepository = deedRepository;
        this.deedManagementRepository = deedManagementRepository;
    }

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
        public static ProcedureResponse from(Procedure procedure) {
            Budget budget = procedure.getFkIdBudget();
            return new ProcedureResponse(procedure.getIdProcedure(), procedure.getNotes(),
                    ProcedureTypeRef.from(procedure.getFkIdProcedureType()),
                    PropertyRef.from(procedure.getFkIdProperty()),
                    budget == null ? null : new BudgetRef(budget.getIdBudget()),
                    DeedRef.from(procedure.getFkIdDeed()), ManagementRef.from(procedure.getFkIdManagement()),
                    procedure.getVersion());
        }
    }

    /**
     * Body of create and full update. {@code idProcedureType} is required on both (issue #655,
     * Owner decision Oct 9: an empty or incomplete PUT answers 400). {@code notes} is stored as
     * sent; an absent association id keeps the stored association.
     */
    public record ProcedureRequest(
            @Schema(description = "Procedure type; must existir (404 if not)",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            Integer idProcedureType,
            Integer idProperty, Integer idDeed, Integer idManagement, Integer idBudget, String notes) {
    }

    @GetMapping
    @Operation(summary = "Get all procedures")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ProcedureResponse>> getAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable).map(ProcedureResponse::from));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get procedure by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<ProcedureResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(procedure -> ResponseEntity.ok(ProcedureResponse.from(procedure)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Invalid request: idProcedureType is required"),
    @ApiResponse(responseCode = "404", description = "Referencia no encontrada"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Create procedure")
    public ResponseEntity<Object> create(@RequestBody ProcedureRequest request) {
        RequiredFields.require(request.idProcedureType(), "idProcedureType");
        Procedure entity;
        try {
            entity = hydrate(new Procedure(), request);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProcedureResponse.from(entity));
        } catch (Exception e) {
            log.error("Failed to create procedure", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Cuerpo empty or incompleto: idProcedureType is required"),
    @ApiResponse(responseCode = "404", description = "Procedure or referencia not found")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update procedure",
               description = "idProcedureType is required (400 if falta); notes guarda tal as envía and "
                       + "a asociación ausente conserva the actual.")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody ProcedureRequest request) {
        RequiredFields.require(request.idProcedureType(), "idProcedureType");
        Procedure existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        Procedure entity;
        try {
            entity = hydrate(existing, request);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity = repository.save(entity);
            return ResponseEntity.ok(ProcedureResponse.from(entity));
        } catch (Exception e) {
            log.error("Failed to update procedure id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolves every association id present in {@code request} against its own
     * repository and applies it to {@code entity}, guaranteeing the persisted
     * and returned state always reflects the real row (issue #981).
     *
     * @throws NoSuchElementException if a provided id does not resolve
     */
    private Procedure hydrate(Procedure entity, ProcedureRequest request) {
        entity.setNotes(request.notes());
        if (request.idProcedureType() != null) {
            ProcedureType procedureType = procedureTypeRepository.findById(request.idProcedureType())
                    .orElseThrow();
            entity.setFkIdProcedureType(procedureType);
        }
        if (request.idProperty() != null) {
            Property property = propertyRepository.findById(request.idProperty()).orElseThrow();
            entity.setFkIdProperty(property);
        }
        if (request.idDeed() != null) {
            Deed deed = deedRepository.findById(request.idDeed()).orElseThrow();
            entity.setFkIdDeed(deed);
        }
        if (request.idManagement() != null) {
            DeedManagement management = deedManagementRepository.findById(request.idManagement()).orElseThrow();
            entity.setFkIdManagement(management);
        }
        if (request.idBudget() != null) {
            Budget budget = budgetRepository.findById(request.idBudget()).orElseThrow();
            entity.setFkIdBudget(budget);
        }
        return entity;
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete procedure")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete procedure id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
