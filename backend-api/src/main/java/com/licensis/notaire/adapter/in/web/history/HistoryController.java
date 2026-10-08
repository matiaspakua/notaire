package com.licensis.notaire.adapter.in.web.history;

import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.application.usecase.history.HistoryMapper;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial", description = "API para gestionar historial de gestiones")
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    record HistoryRequest(
            Date date,
            String notes,
            @NotNull Integer managementStatusId,
            @NotNull Integer managementId) {}

    private final HistoryRepository repository;
    private final ManagementStatusRepository managementStatusRepository;
    private final DeedManagementRepository deedManagementRepository;

    public HistoryController(HistoryRepository repository,
            ManagementStatusRepository managementStatusRepository,
            DeedManagementRepository deedManagementRepository) {
        this.repository = repository;
        this.managementStatusRepository = managementStatusRepository;
        this.deedManagementRepository = deedManagementRepository;
    }

    private boolean applyRequest(History entity, HistoryRequest request) {
        ManagementStatus status = managementStatusRepository.findById(request.managementStatusId()).orElse(null);
        DeedManagement management = deedManagementRepository.findById(request.managementId()).orElse(null);
        if (status == null || management == null) {
            return false;
        }
        entity.setDate(request.date() != null ? request.date() : new Date());
        entity.setNotes(request.notes());
        entity.setFkIdManagementStatus(status);
        entity.setFkIdManagement(management);
        return true;
    }

    @GetMapping
    @Operation(summary = "Obtener el historial, una página por vez",
            description = "Página de Spring Data (por defecto 20 filas, ordenadas por idHistory ascendente). "
                    + "Usar sort=idHistory,desc para ver primero los cambios más recientes. "
                    + "El historial completo de una gestión está en GET /api/v1/historial/gestion/{idManagement}.")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DtoHistorySummary>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "idHistory") Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable).map(HistoryMapper::toDto));
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
    public ResponseEntity<Object> create(@Valid @RequestBody HistoryRequest request) {
        try {
            History entity = new History();
            if (!applyRequest(entity, request)) {
                return ResponseEntity.badRequest().build();
            }
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(HistoryMapper.toDto(entity));
        } catch (Exception e) {
            log.error("Failed to create historial", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "403", description = "Solo un administrador puede modificar el historial"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar historial (solo administrador)",
            description = "El historial es la traza de cambios de estado de la gestión (CU13): "
                    + "solo un administrador puede corregir un registro.")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody HistoryRequest request) {
        return repository.findById(id).map(existing -> {
            try {
                if (!applyRequest(existing, request)) {
                    return ResponseEntity.badRequest().<Void>build();
                }
                repository.save(existing);
                return ResponseEntity.ok().<Void>build();
            } catch (Exception e) {
                log.error("Failed to update historial id {}", id, e);
                return ResponseEntity.internalServerError().<Void>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "403", description = "Solo un administrador puede eliminar el historial"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar historial (solo administrador)",
            description = "El historial es la traza de cambios de estado de la gestión (CU13): "
                    + "solo un administrador puede eliminar un registro.")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        History entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.getFkIdManagementStatus().getHistoryList().remove(entity);
            repository.delete(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete historial id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
