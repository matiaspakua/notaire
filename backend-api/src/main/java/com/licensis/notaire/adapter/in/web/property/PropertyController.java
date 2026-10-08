package com.licensis.notaire.adapter.in.web.property;

import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.PropertyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inmueble")
@Tag(name = "Inmueble", description = "API para gestionar inmueble")
public class PropertyController {

    /**
     * Create and full-update body. The cadastral designation (nomenclatura catastral) identifies
     * the property and is required on both, since PUT replaces every field (issue #655).
     */
    record PropertyRequest(
            @NotBlank
            @Schema(description = "Nomenclatura catastral que identifica el inmueble; obligatoria y no vacía",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String cadastralDesignation,
            java.math.BigDecimal fiscalAppraisal,
            String address,
            String notes,
            String registrationNumber,
            String volumeFolioLandRecord,
            String boundaries) {}

    record PropertyResponse(
            Integer idProperty,
            String cadastralDesignation,
            java.math.BigDecimal fiscalAppraisal,
            String address,
            String notes,
            String registrationNumber,
            String volumeFolioLandRecord,
            String boundaries,
            int version) {}

    private final PropertyRepository repository;

    public PropertyController(PropertyRepository repository) {
        this.repository = repository;
    }

    private PropertyResponse toResponse(Property entity) {
        return new PropertyResponse(
                entity.getIdProperty(),
                entity.getCadastralDesignation(),
                entity.getFiscalAppraisal(),
                entity.getAddress(),
                entity.getNotes(),
                entity.getRegistrationNumber(),
                entity.getVolumeFolioLandRecord(),
                entity.getBoundaries(),
                entity.getVersion());
    }

    private void applyRequest(Property entity, PropertyRequest request) {
        entity.setCadastralDesignation(request.cadastralDesignation());
        entity.setFiscalAppraisal(request.fiscalAppraisal());
        entity.setAddress(request.address());
        entity.setNotes(request.notes());
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setVolumeFolioLandRecord(request.volumeFolioLandRecord());
        entity.setBoundaries(request.boundaries());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los inmueble")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inmueble por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (falta la nomenclatura catastral)"),
        @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PostMapping
    @Operation(summary = "Crear nuevo inmueble")
    public ResponseEntity<Object> create(@Valid @RequestBody PropertyRequest request) {
        try {
            Property entity = new Property();
            applyRequest(entity, request);
            Property saved = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (falta la nomenclatura catastral)"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar inmueble")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody PropertyRequest request) {
        return repository.findById(id).map(existing -> {
            applyRequest(existing, request);
            repository.save(existing);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inmueble")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
