package com.licensis.notaire.api;

import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movimiento-testimonio")
@Tag(name = "MovimientoTestimonio", description = "API para gestionar movimiento-testimonio")
public class MovimientoTestimonioController {

    private MovimientoTestimonioJpaController getJpaController() {
        return new MovimientoTestimonioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los movimiento-testimonio")
    public ResponseEntity<List<MovimientoTestimonio>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findMovimientoTestimonioEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento-testimonio por ID")
    public ResponseEntity<MovimientoTestimonio> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findMovimientoTestimonio(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo movimiento-testimonio")
    public ResponseEntity<Void> create(@RequestBody MovimientoTestimonio entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento-testimonio")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody MovimientoTestimonio entity) {
        try {
            entity.setIdMovimientoTestimonio(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento-testimonio")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
