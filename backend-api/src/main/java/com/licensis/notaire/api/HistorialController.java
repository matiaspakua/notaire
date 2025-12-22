package com.licensis.notaire.api;

import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Historial;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial", description = "API para gestionar historial")
public class HistorialController {

    private HistorialJpaController getJpaController() {
        return new HistorialJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los historial")
    public ResponseEntity<List<Historial>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findHistorialEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener historial por ID")
    public ResponseEntity<Historial> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findHistorial(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo historial")
    public ResponseEntity<Void> create(@RequestBody Historial entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar historial")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Historial entity) {
        try {
            entity.setIdHistorial(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar historial")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
