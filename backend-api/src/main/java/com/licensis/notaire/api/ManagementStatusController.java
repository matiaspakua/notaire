package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/estado-gestion")
@Tag(name = "Estado de Gestion", description = "API para estados de gestion")
public class ManagementStatusController {

    private final ManagementStatusRepository repository;
    private final DeedManagementRepository managementRepository;

    public ManagementStatusController(ManagementStatusRepository repository,
            DeedManagementRepository managementRepository) {
        this.repository = repository;
        this.managementRepository = managementRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los estados de gestion")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoManagementStatus>> getAll() {
        List<DtoManagementStatus> result = repository.findAll().stream()
                .map(ManagementStatus::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de gestion por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementStatus> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar estados de gestion por nombre")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoManagementStatus>> search(@RequestParam String name) {
        List<DtoManagementStatus> result = repository.findByNameContaining(name).stream()
                .map(ManagementStatus::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si el estado esta referenciado")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty();
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear estado de gestion")
    public ResponseEntity<Object> create(@RequestBody DtoManagementStatus dto) {
        try {
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            ManagementStatus entity = new ManagementStatus();
            entity.setAtributo(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de gestion")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoManagementStatus dto) {
        Optional<ManagementStatus> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "No se puede modificar: el estado está referenciado por gestiones de escritura."));
        }
        try {
            ManagementStatus entity = existing.get();
            dto.setIdManagementStatus(id);
            if (dto.getVersion() == null) {
                dto.setVersion(entity.getVersion());
            }
            entity.setAtributo(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de gestion")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "No se puede eliminar: el estado está referenciado por gestiones de escritura."));
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
