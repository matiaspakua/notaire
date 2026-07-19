package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.OptimisticLockException;
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
 * REST controller for PlantillaPresupuesto (Budget Templates).
 *
 * Fix for issue #340: Added proper OptimisticLockException handling to return
 * HTTP 409 Conflict instead of 500 on concurrent modification. Also added
 * version-aware update that fetches current entity state before merging to
 * prevent stale-data overwrites.
 *
 * Covers use cases: CU39, CU49, CU55 (Plantilla de presupuesto management).
 */
@RestController
@RequestMapping("/api/v1/plantilla-presupuestos")
@Tag(name = "PlantillaPresupuesto", description = "API para gestionar plantillas de presupuesto")
public class PlantillaPresupuestoController {

    private static final Logger LOG = Logger.getLogger(PlantillaPresupuestoController.class.getName());

    private PlantillaPresupuestoJpaController getJpaController() {
        return new PlantillaPresupuestoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de presupuesto")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlantillaPresupuesto>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findPlantillaPresupuestoEntities());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener plantillas de presupuesto", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener plantillas de presupuesto por tipo de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlantillaPresupuesto>> getByTipoTramite(@PathVariable Integer idTipoTramite) {
        try {
            return ResponseEntity.ok(getJpaController().findPlantillasDePresupuesto(idTipoTramite));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener plantillas por tipo de tramite " + idTipoTramite, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva plantilla de presupuesto")
    public ResponseEntity<?> create(@RequestBody PlantillaPresupuesto entity) {
        try {
            getJpaController().create(entity);
            PlantillaPresupuestoPK pk = entity.getPlantillaPresupuestoPK();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("fkIdTipoTramite", pk.getFkIdTipoTramite(), "fkIdConcepto", pk.getFkIdConcepto()));
        } catch (PreexistingEntityException e) {
            LOG.log(Level.WARNING, "Plantilla de presupuesto ya existe", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "La plantilla de presupuesto ya existe para ese tipo de trámite y concepto"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al crear plantilla de presupuesto", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno al crear la plantilla"));
        }
    }

    /**
     * Update a PlantillaPresupuesto.
     *
     * To prevent OptimisticLockException (issue #340), we first retrieve the current
     * persisted entity and copy the client's changes onto it. This ensures the
     * @Version field is always current, avoiding stale-data conflicts when the
     * caller does not send the version field.
     */
    @PutMapping("/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}")
    @Operation(summary = "Actualizar plantilla de presupuesto")
    public ResponseEntity<?> update(
            @PathVariable Integer idTipoTramite,
            @PathVariable Integer idConcepto,
            @RequestBody PlantillaPresupuesto entity
    ) {
        try {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(idTipoTramite, idConcepto);

            // Fetch current state to carry the correct @Version value
            PlantillaPresupuesto current = getJpaController().findPlantillaPresupuesto(pk);
            if (current == null) {
                return ResponseEntity.notFound().build();
            }

            // Apply incoming changes (only mutable fields — PK and version come from DB)
            entity.setPlantillaPresupuestoPK(pk);
            if (entity.getObservaciones() != null) {
                current.setObservaciones(entity.getObservaciones());
            }
            // Propagate related entities if provided
            if (entity.getConcepto() != null) {
                current.setConcepto(entity.getConcepto());
            }
            if (entity.getTipoDeTramite() != null) {
                current.setTipoDeTramite(entity.getTipoDeTramite());
            }

            getJpaController().edit(current);
            return ResponseEntity.ok().build();

        } catch (OptimisticLockException e) {
            // Concurrent modification detected — client should re-fetch and retry
            LOG.log(Level.WARNING, "Conflicto de concurrencia al actualizar plantilla de presupuesto", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto de concurrencia: la plantilla fue modificada por otro usuario. Recargue y vuelva a intentarlo."));
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Check if the root cause is an OptimisticLockException (can be wrapped in RollbackException)
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof OptimisticLockException) {
                    LOG.log(Level.WARNING, "Conflicto de concurrencia (wrapped) al actualizar plantilla", cause);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Conflicto de concurrencia: la plantilla fue modificada por otro usuario."));
                }
                cause = cause.getCause();
            }
            LOG.log(Level.SEVERE, "Error al actualizar plantilla de presupuesto", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno al actualizar la plantilla"));
        }
    }

    @DeleteMapping("/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}")
    @Operation(summary = "Eliminar plantilla de presupuesto")
    public ResponseEntity<?> delete(@PathVariable Integer idTipoTramite, @PathVariable Integer idConcepto) {
        try {
            getJpaController().destroy(new PlantillaPresupuestoPK(idTipoTramite, idConcepto));
            return ResponseEntity.ok().build();
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar plantilla de presupuesto", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno al eliminar la plantilla"));
        }
    }
}
