package com.licensis.notaire.api;

import com.licensis.notaire.jpa.TramiteJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Tramite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tramites")
@Tag(name = "Trámites", description = "API para gestionar trámites")
public class TramiteController {

    private TramiteJpaController getJpaController() {
        return new TramiteJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los trámites")
    public ResponseEntity<List<Tramite>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findTramiteEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener trámite por ID")
    public ResponseEntity<Tramite> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findTramite(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo trámite")
    public ResponseEntity<Void> create(@RequestBody Tramite entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar trámite")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Tramite entity) {
        try {
            entity.setIdTramite(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar trámite")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
