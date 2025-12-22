package com.licensis.notaire.api;

import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Presupuesto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/presupuestos")
@Tag(name = "Presupuestos", description = "API para gestionar presupuestos")
public class PresupuestoController {

    private PresupuestoJpaController getJpaController() {
        return new PresupuestoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los presupuestos")
    public ResponseEntity<List<Presupuesto>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findPresupuestoEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID")
    public ResponseEntity<Presupuesto> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findPresupuesto(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo presupuesto")
    public ResponseEntity<Void> create(@RequestBody Presupuesto entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Presupuesto entity) {
        try {
            entity.setIdPresupuesto(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
