package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoHistorialSummary;
import com.licensis.notaire.service.mappers.HistorialMapper;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.HistorialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorialSummary>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream()
            .map(HistorialMapper::toDto)
            .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener historial por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorialSummary> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(h -> ResponseEntity.ok(HistorialMapper.toDto(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/gestion/{idGestion}")
    @Operation(summary = "Obtener historial de una gestion (CU13)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorialSummary>> getByGestion(@PathVariable Integer idGestion) {
        return ResponseEntity.ok(repository.findByFkIdGestionIdGestion(idGestion).stream()
            .map(HistorialMapper::toDto)
            .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo registro de historial")
    public ResponseEntity<Object> create(@RequestBody Historial entity) {
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create historial", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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
