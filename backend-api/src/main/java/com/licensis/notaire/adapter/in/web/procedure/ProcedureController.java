package com.licensis.notaire.adapter.in.web.procedure;

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

import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/v1/tramites")
@Tag(name = "Trámites", description = "API para gestionar trámites")
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
    public record ProcedureRequest(Integer idProcedureType, Integer idProperty, Integer idDeed,
            Integer idManagement, Integer idBudget, String notes) {
    }

    @GetMapping
    @Operation(summary = "Obtener todos los trámites")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Procedure>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener trámite por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Procedure> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "404", description = "Referencia no encontrada"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo trámite")
    public ResponseEntity<Object> create(@RequestBody ProcedureRequest request) {
        if (request.idProcedureType() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "idProcedureType is required"));
        }
        Procedure entity;
        try {
            entity = hydrate(new Procedure(), request);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create tramite", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar trámite")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody ProcedureRequest request) {
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
            return ResponseEntity.ok(entity);
        } catch (Exception e) {
            log.error("Failed to update tramite id {}", id, e);
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
    @Operation(summary = "Eliminar trámite")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete tramite id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
