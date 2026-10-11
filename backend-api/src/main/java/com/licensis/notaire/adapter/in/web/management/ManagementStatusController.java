package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
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
@RequestMapping("/api/v1/management-statuses")
@Tag(name = "Management status", description = "API for management")
public class ManagementStatusController {

    private final ManagementStatusRepository repository;
    private final DeedManagementRepository managementRepository;

    public ManagementStatusController(ManagementStatusRepository repository,
            DeedManagementRepository managementRepository) {
        this.repository = repository;
        this.managementRepository = managementRepository;
    }

    @GetMapping
    @Operation(summary = "Get all management statuses")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoManagementStatus>> getAll() {
        List<DtoManagementStatus> result = repository.findAll().stream()
                .map(ManagementStatus::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get management status by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementStatus> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search management statuses by name")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoManagementStatus>> search(@RequestParam String name) {
        List<DtoManagementStatus> result = repository.findByNameContaining(name).stream()
                .map(ManagementStatus::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether the status is referenced")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty();
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create management status")
    public ResponseEntity<Object> create(@RequestBody DtoManagementStatus dto) {
        try {
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            ManagementStatus entity = new ManagementStatus();
            entity.setAttribute(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update management status")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoManagementStatus dto) {
        Optional<ManagementStatus> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "Cannot update: the status is referenced by deed managements."));
        }
        try {
            ManagementStatus entity = existing.get();
            dto.setIdManagementStatus(id);
            if (dto.getVersion() == null) {
                dto.setVersion(entity.getVersion());
            }
            entity.setAttribute(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete management status")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!managementRepository.findByFkIdManagementStatusIdManagementStatus(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "Cannot delete: the status is referenced by deed managements."));
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
