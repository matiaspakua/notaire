package com.licensis.notaire.api;

import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-identificacion")
@Tag(name = "TipoIdentificacion", description = "API para gestionar tipos de identificacion")
public class TipoIdentificacionController {

    private static final Logger log = LoggerFactory.getLogger(TipoIdentificacionController.class);

    private final TipoIdentificacionRepository repository;

    public TipoIdentificacionController(TipoIdentificacionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de identificacion")
    public ResponseEntity<List<TipoIdentificacion>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de identificacion por ID")
    public ResponseEntity<TipoIdentificacion> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo de identificacion")
    public ResponseEntity<Void> create(@RequestBody TipoIdentificacion entity) {
        try {
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Failed to create tipo de identificacion", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de identificacion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoIdentificacion entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdTipoIdentificacion(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update tipo de identificacion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de identificacion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete tipo de identificacion id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
