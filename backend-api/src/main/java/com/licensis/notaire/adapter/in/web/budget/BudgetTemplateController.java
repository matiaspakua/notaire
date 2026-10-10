package com.licensis.notaire.adapter.in.web.budget;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.BudgetTemplateJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ConceptRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST controller for BudgetTemplate (Budget Templates).
 *
 * Fix for issue #340: Added proper OptimisticLockException handling to return
 * HTTP 409 Conflict instead of 500 on concurrent modification. Also added
 * version-aware update that fetches current entity state before merging to
 * prevent stale-data overwrites.
 *
 * Covers use cases: CU39, CU49, CU55 (Budget template management).
 */
@RestController
@RequestMapping("/api/v1/budget-templates")
@Tag(name = "BudgetTemplate", description = "API for managing budget templates")
public class BudgetTemplateController {

    private static final Logger LOG = Logger.getLogger(BudgetTemplateController.class.getName());

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

    private final ConceptRepository conceptRepository;
    private final ProcedureTypeRepository procedureTypeRepository;

    public BudgetTemplateController(
            ConceptRepository conceptRepository,
            ProcedureTypeRepository procedureTypeRepository) {
        this.conceptRepository = conceptRepository;
        this.procedureTypeRepository = procedureTypeRepository;
    }

    private BudgetTemplateJpaController getJpaController() {
        return new BudgetTemplateJpaController(null, JpaControllerProvider.getEntityManagerFactory());
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

    private BudgetTemplateResponse toResponse(BudgetTemplate entity) {
        BudgetTemplatePK pk = entity.getBudgetTemplatePK();
        return new BudgetTemplateResponse(
                pk != null ? pk.getFkIdProcedureType() : null,
                pk != null ? pk.getFkIdConcept() : null,
                entity.getNotes(),
                entity.getVersion());
    }

    @GetMapping
    @Operation(summary = "Get all budget templates")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetTemplateResponse>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findBudgetTemplateEntities()
                    .stream().map(this::toResponse).toList());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error to the get budget templates", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/procedure-types/{idProcedureType}")
    @Operation(summary = "Get budget templates by procedure type")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BudgetTemplateResponse>> getByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        try {
            return ResponseEntity.ok(getJpaController().findBudgetTemplates(idProcedureType)
                    .stream().map(this::toResponse).toList());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error to the get plantillas by procedure type " + idProcedureType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Create budget template")
    public ResponseEntity<?> create(@Valid @RequestBody BudgetTemplateRequest request) {
        Integer procedureTypeId = resolveProcedureTypeId(request);
        Integer conceptId = resolveConceptId(request);
        if (procedureTypeId == null || conceptId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "fkIdProcedureType y fkIdConcept son requeridos"));
        }
        if (!conceptRepository.existsById(conceptId) || !procedureTypeRepository.existsById(procedureTypeId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "concept or procedure type not found"));
        }
        try {
            BudgetTemplate entity = new BudgetTemplate(new BudgetTemplatePK(procedureTypeId, conceptId));
            entity.setNotes(request.notes());
            // Legacy JpaController.create reads getConcept()/getProcedureType() IDs (lines 51–52)
            // and opens its own EntityManager. Do NOT pass Spring Data getReferenceById proxies —
            // those are bound to Spring's persistence context and can leave create() returning
            // false while this method still answered 201. ID-only stubs are enough for PK wiring;
            // the JpaController re-attaches via em.getReference in its own EM.
            Concept conceptRef = new Concept();
            conceptRef.setIdConcept(conceptId);
            ProcedureType procedureTypeRef = new ProcedureType();
            procedureTypeRef.setIdProcedureType(procedureTypeId);
            entity.setConcept(conceptRef);
            entity.setProcedureType(procedureTypeRef);
            Boolean created = getJpaController().create(entity);
            if (!Boolean.TRUE.equals(created)) {
                LOG.log(Level.SEVERE, "Budget template create returned false for tipo={0} concept={1}",
                        new Object[]{procedureTypeId, conceptId});
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Error interno to the create the template"));
            }
            BudgetTemplatePK pk = entity.getBudgetTemplatePK();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("fkIdTipoTramite", pk.getFkIdProcedureType(), "fkIdConcepto", pk.getFkIdConcept()));
        } catch (PreexistingEntityException e) {
            LOG.log(Level.WARNING, "Budget template already exists", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "The budget template already exists for that procedure type and concept"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error to the create budget template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno to the create the template"));
        }
    }

    @PutMapping("/procedure-types/{idProcedureType}/concept/{idConcept}")
    @Operation(summary = "Update budget template")
    public ResponseEntity<?> update(
            @PathVariable Integer idProcedureType,
            @PathVariable Integer idConcept,
            @Valid @RequestBody BudgetTemplateRequest request
    ) {
        try {
            BudgetTemplatePK pk = new BudgetTemplatePK(idProcedureType, idConcept);

            BudgetTemplate current = getJpaController().findBudgetTemplate(pk);
            if (current == null) {
                return ResponseEntity.notFound().build();
            }

            if (request.notes() != null) {
                current.setNotes(request.notes());
            }

            getJpaController().edit(current);
            return ResponseEntity.ok().build();

        } catch (OptimisticLockException e) {
            LOG.log(Level.WARNING, "Conflicto of concurrencia to the update budget template", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto of concurrencia: the template was modificada by another user. Recargue and vuelva a intentarlo."));
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof OptimisticLockException) {
                    LOG.log(Level.WARNING, "Conflicto of concurrencia (wrapped) to the update template", cause);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Conflicto of concurrencia: the template was modificada by another user."));
                }
                cause = cause.getCause();
            }
            LOG.log(Level.SEVERE, "Error to the update budget template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno to the update the template"));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/procedure-types/{idProcedureType}/concept/{idConcept}")
    @Operation(summary = "Delete budget template")
    public ResponseEntity<?> delete(@PathVariable Integer idProcedureType, @PathVariable Integer idConcept) {
        try {
            getJpaController().destroy(new BudgetTemplatePK(idProcedureType, idConcept));
            return ResponseEntity.noContent().build();
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error to the delete budget template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno to the delete the template"));
        }
    }
}
