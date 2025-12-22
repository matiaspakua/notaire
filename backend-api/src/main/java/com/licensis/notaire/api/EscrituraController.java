package com.licensis.notaire.api;

import com.licensis.notaire.jpa.EscrituraJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Escritura;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/escrituras")
@Tag(name = "Escrituras", description = "API para gestionar escrituras")
public class EscrituraController {

    private EscrituraJpaController getJpaController() {
        return new EscrituraJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todas las escrituras")
    public ResponseEntity<List<Escritura>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findEscrituraEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener escritura por ID")
    public ResponseEntity<Escritura> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findEscritura(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva escritura")
    public ResponseEntity<Void> create(@RequestBody Escritura entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar escritura")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Escritura entity) {
        try {
            entity.setIdEscritura(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar escritura")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
