package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.service.mappers.HistoryMapper;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.HistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial", description = "API para gestionar historial de gestiones")
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    private final HistoryRepository repository;

    public HistoryController(HistoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todo el historial")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorySummary>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream()
            .map(HistoryMapper::toDto)
            .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener historial por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorySummary> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(h -> ResponseEntity.ok(HistoryMapper.toDto(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/gestion/{idManagement}")
    @Operation(summary = "Obtener historial de una gestion (CU13)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorySummary>> getByManagement(@PathVariable Integer idManagement) {
        return ResponseEntity.ok(repository.findByFkIdManagementIdManagement(idManagement).stream()
            .map(HistoryMapper::toDto)
            .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo registro de historial")
    public ResponseEntity<Object> create(@RequestBody History entity) {
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
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody History entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdHistory(id);
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
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        History entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            // EstadoDeGestion.historialList is an eagerly-fetched, cascade=ALL, bidirectional
            // collection that this entity belongs to. Hibernate's cascade processing on that
            // stale collection reference silently cancels a direct entityManager.remove() at
            // flush time unless the entity is unlinked from it first.
            entity.getFkIdManagementStatus().getHistoryList().remove(entity);
            repository.delete(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete historial id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
