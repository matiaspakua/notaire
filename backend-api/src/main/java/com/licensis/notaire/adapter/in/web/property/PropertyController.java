package com.licensis.notaire.adapter.in.web.property;

import com.licensis.notaire.application.port.in.property.ManagePropertiesUseCase;
import com.licensis.notaire.application.port.in.property.QueryPropertiesUseCase;
import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.property.PropertyDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Property", description = "API for managing property")
public class PropertyController {

    /**
     * Create and full-update body. The cadastral designation (cadastral nomenclature) identifies
     * the property and is required on both, since PUT replaces every field (issue #655).
     */
    record PropertyRequest(
            @NotBlank
            @Schema(description = "Cadastral nomenclature that identifies the property; required and not empty",
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

    private final QueryPropertiesUseCase query;
    private final ManagePropertiesUseCase manage;

    public PropertyController(QueryPropertiesUseCase query, ManagePropertiesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private PropertyResponse toResponse(Property property) {
        PropertyDetails details = property.details();
        return new PropertyResponse(
                property.id(),
                details.cadastralDesignation(),
                details.fiscalAppraisal(),
                details.address(),
                details.notes(),
                details.registrationNumber(),
                details.volumeFolioLandRecord(),
                details.boundaries(),
                property.version());
    }

    private PropertyDetails toDetails(PropertyRequest request) {
        return new PropertyDetails(
                request.cadastralDesignation(),
                request.fiscalAppraisal(),
                request.address(),
                request.notes(),
                request.registrationNumber(),
                request.volumeFolioLandRecord(),
                request.boundaries());
    }

    @GetMapping
    @Operation(summary = "Get all property")
    public ResponseEntity<List<PropertyResponse>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(query.getById(id)));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request (the cadastral designation is missing)"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create property")
    public ResponseEntity<Object> create(@Valid @RequestBody PropertyRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(manage.create(toDetails(request))));
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request (the cadastral designation is missing)"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update property")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody PropertyRequest request) {
        manage.update(id, toDetails(request));
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete property")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
