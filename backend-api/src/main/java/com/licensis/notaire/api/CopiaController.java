package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.repository.CopiaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/copia")
@Tag(name = "Copia", description = "API para gestionar copias")
public class CopiaController {

    private static final Logger log = LoggerFactory.getLogger(CopiaController.class);

    private final CopiaRepository repository;

    public CopiaController(CopiaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las copias")
    public ResponseEntity<List<Copia>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener copia por ID")
    public ResponseEntity<Copia> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva copia")
    public ResponseEntity<Void> create(@RequestBody Copia entity) {
        try {
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Failed to create copia", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar copia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Copia entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdCopia(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update copia id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar copia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete copia id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
