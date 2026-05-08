package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.HistorialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial", description = "API para gestionar historial de gestiones")
public class HistorialController {

    private static final Logger log = LoggerFactory.getLogger(HistorialController.class);

    private final HistorialRepository repository;

    public HistorialController(HistorialRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todo el historial")
    public ResponseEntity<List<Historial>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener historial por ID")
    public ResponseEntity<Historial> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/gestion/{idGestion}")
    @Operation(summary = "Obtener historial de una gestion (CU13)")
    public ResponseEntity<List<Historial>> getByGestion(@PathVariable Integer idGestion) {
        return ResponseEntity.ok(repository.findByFkIdGestionIdGestion(idGestion));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo registro de historial")
    public ResponseEntity<Void> create(@RequestBody Historial entity) {
        try {
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Failed to create historial", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar historial")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Historial entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdHistorial(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update historial id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar historial")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete historial id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
