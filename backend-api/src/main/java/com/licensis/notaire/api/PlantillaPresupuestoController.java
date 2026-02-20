package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plantilla-presupuestos")
@Tag(name = "PlantillaPresupuesto", description = "API para gestionar plantillas de presupuesto")
public class PlantillaPresupuestoController {

    private PlantillaPresupuestoJpaController getJpaController() {
        return new PlantillaPresupuestoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de presupuesto")
    public ResponseEntity<List<PlantillaPresupuesto>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findPlantillaPresupuestoEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener plantillas de presupuesto por tipo de tramite")
    public ResponseEntity<List<PlantillaPresupuesto>> getByTipoTramite(@PathVariable Integer idTipoTramite) {
        try {
            return ResponseEntity.ok(getJpaController().findPlantillasDePresupuesto(idTipoTramite));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva plantilla de presupuesto")
    public ResponseEntity<Void> create(@RequestBody PlantillaPresupuesto entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}")
    @Operation(summary = "Actualizar plantilla de presupuesto")
    public ResponseEntity<Void> update(
            @PathVariable Integer idTipoTramite,
            @PathVariable Integer idConcepto,
            @RequestBody PlantillaPresupuesto entity
    ) {
        try {
            entity.setPlantillaPresupuestoPK(new PlantillaPresupuestoPK(idTipoTramite, idConcepto));
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}")
    @Operation(summary = "Eliminar plantilla de presupuesto")
    public ResponseEntity<Void> delete(@PathVariable Integer idTipoTramite, @PathVariable Integer idConcepto) {
        try {
            getJpaController().destroy(new PlantillaPresupuestoPK(idTipoTramite, idConcepto));
            return ResponseEntity.ok().build();
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
