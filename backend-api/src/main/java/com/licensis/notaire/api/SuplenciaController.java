package com.licensis.notaire.api;

import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Suplencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suplencia")
@Tag(name = "Suplencia", description = "API para gestionar suplencia")
public class SuplenciaController {

    private SuplenciaJpaController getJpaController() {
        return new SuplenciaJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los suplencia")
    public ResponseEntity<List<Suplencia>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findSuplenciaEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener suplencia por ID")
    public ResponseEntity<Suplencia> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findSuplencia(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo suplencia")
    public ResponseEntity<Void> create(@RequestBody Suplencia entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar suplencia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Suplencia entity) {
        try {
            entity.setIdSuplencia(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar suplencia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
