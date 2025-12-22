package com.licensis.notaire.api;

import com.licensis.notaire.jpa.CopiaJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Copia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/copia")
@Tag(name = "Copia", description = "API para gestionar copia")
public class CopiaController {

    private CopiaJpaController getJpaController() {
        return new CopiaJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los copia")
    public ResponseEntity<List<Copia>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findCopiaEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener copia por ID")
    public ResponseEntity<Copia> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findCopia(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo copia")
    public ResponseEntity<Void> create(@RequestBody Copia entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar copia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Copia entity) {
        try {
            entity.setIdCopia(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar copia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
