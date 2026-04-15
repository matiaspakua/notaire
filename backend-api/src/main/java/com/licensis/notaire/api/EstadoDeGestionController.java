package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.negocio.EstadoDeGestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseEntity<List<DtoEstadoDeGestion>> getAll() {
        List<EstadoDeGestion> list = getJpaController().findEstadoDeGestionEntities();
        List<DtoEstadoDeGestion> result = new ArrayList<>();
        for (EstadoDeGestion e : list) {
            result.add(e.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de gestion por ID")
    public ResponseEntity<DtoEstadoDeGestion> getById(@PathVariable Integer id) {
        EstadoDeGestion e = getJpaController().findEstadoDeGestion(id);
        return e != null ? ResponseEntity.ok(e.getDto()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear estado de gestion")
    public ResponseEntity<Object> create(@RequestBody EstadoDeGestion estadoDeGestion) {
        try {
            getJpaController().create(estadoDeGestion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de gestion")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody EstadoDeGestion estadoDeGestion) {
        try {
            estadoDeGestion.setIdEstadoGestion(id);
            getJpaController().edit(estadoDeGestion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de gestion")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el estado de gestión está referenciado por otros registros.");
        }
    }
}
