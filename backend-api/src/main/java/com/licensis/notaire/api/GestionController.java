package com.licensis.notaire.api;

import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Gestiones", description = "API para gestionar gestiones de escritura")
public class GestionController {

    private static final Logger log = LoggerFactory.getLogger(GestionController.class);

    private final GestionDeEscrituraRepository repository;
    private final HistorialRepository historialRepository;

    public GestionController(GestionDeEscrituraRepository repository, HistorialRepository historialRepository) {
        this.repository = repository;
        this.historialRepository = historialRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las gestiones")
    public ResponseEntity<List<GestionDeEscritura>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener gestion por ID")
    public ResponseEntity<GestionDeEscritura> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener gestion por numero")
    public ResponseEntity<GestionDeEscritura> getByNumero(@PathVariable Integer numero) {
        return repository.findByNumero(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idPersona}")
    @Operation(summary = "Obtener gestiones de un cliente (CU19)")
    public ResponseEntity<List<GestionDeEscritura>> getByCliente(@PathVariable Integer idPersona) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}/estado-actual")
    @Operation(summary = "Obtener estado actual de una gestion")
    public ResponseEntity<Historial> getEstadoActual(@PathVariable Integer id) {
        List<Historial> historiales = historialRepository.findByFkIdGestionIdGestion(id);
        if (historiales.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return historiales.stream()
                .max(Comparator.comparing(Historial::getFecha))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva gestion")
    public ResponseEntity<Object> create(@RequestBody GestionDeEscritura entity) {
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create gestion", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gestion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody GestionDeEscritura entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdGestion(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update gestion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar gestion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete gestion id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
