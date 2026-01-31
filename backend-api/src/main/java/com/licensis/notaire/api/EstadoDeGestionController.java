package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.negocio.EstadoDeGestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estado-gestion")
@Tag(name = "Estado de Gestion", description = "API para estados de gestion")
public class EstadoDeGestionController {

    private EstadoDeGestionJpaController getJpaController() {
        return new EstadoDeGestionJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los estados de gestion")
    public ResponseEntity<List<EstadoDeGestion>> getAll() {
        try {
            List<EstadoDeGestion> list = getJpaController().findEstadoDeGestionEntities();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de gestion por ID")
    public ResponseEntity<EstadoDeGestion> getById(@PathVariable Integer id) {
        try {
            EstadoDeGestion e = getJpaController().findEstadoDeGestion(id);
            return e != null ? ResponseEntity.ok(e) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear estado de gestion")
    public ResponseEntity<Void> create(@RequestBody EstadoDeGestion estadoDeGestion) {
        try {
            getJpaController().create(estadoDeGestion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de gestion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody EstadoDeGestion estadoDeGestion) {
        try {
            estadoDeGestion.setIdEstadoGestion(id);
            getJpaController().edit(estadoDeGestion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de gestion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
