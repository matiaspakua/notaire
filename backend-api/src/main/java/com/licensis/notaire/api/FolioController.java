package com.licensis.notaire.api;

import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Folio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/folio")
@Tag(name = "Folio", description = "API para gestionar folio")
public class FolioController {

    private FolioJpaController getJpaController() {
        return new FolioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los folio")
    public ResponseEntity<List<Folio>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findFolioEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener folio por ID")
    public ResponseEntity<Folio> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findFolio(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo folio")
    public ResponseEntity<Void> create(@RequestBody Folio entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar folio")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Folio entity) {
        try {
            entity.setIdFolio(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar folio")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
