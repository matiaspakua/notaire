package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoGestionWorkflowTrace;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.service.WorkflowTraceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final WorkflowTraceService workflowTraceService;

    public GestionController(GestionDeEscrituraRepository repository,
                             HistorialRepository historialRepository,
                             WorkflowTraceService workflowTraceService) {
        this.repository = repository;
        this.historialRepository = historialRepository;
        this.workflowTraceService = workflowTraceService;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las gestiones")
    public ResponseEntity<Page<GestionDeEscritura>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
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

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
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

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada"),
        @ApiResponse(responseCode = "400", description = "Gestion sin tramites o workflow definition")
    })
    @GetMapping("/{id}/workflow-trace")
    @Operation(summary = "Obtener trace del workflow de una gestion (con nodos, transiciones, historial y estados)")
    public ResponseEntity<Object> getWorkflowTrace(@PathVariable Integer id) {
        try {
            DtoGestionWorkflowTrace trace = workflowTraceService.buildTrace(id);
            return ResponseEntity.ok(trace);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot build workflow trace for gestion {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to build workflow trace for gestion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
