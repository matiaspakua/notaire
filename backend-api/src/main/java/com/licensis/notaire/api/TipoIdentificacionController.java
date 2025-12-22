package com.licensis.notaire.api;

import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.TipoIdentificacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-identificacion")
@Tag(name = "TipoIdentificacion", description = "API para gestionar tipo-identificacion")
public class TipoIdentificacionController {

    private TipoIdentificacionJpaController getJpaController() {
        return new TipoIdentificacionJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los tipo-identificacion")
    public ResponseEntity<List<TipoIdentificacion>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findTipoIdentificacionEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo-identificacion por ID")
    public ResponseEntity<TipoIdentificacion> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findTipoIdentificacion(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo-identificacion")
    public ResponseEntity<Void> create(@RequestBody TipoIdentificacion entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo-identificacion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoIdentificacion entity) {
        try {
            entity.setIdTipoIdentificacion(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo-identificacion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
