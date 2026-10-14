package com.licensis.notaire.adapter.in.web.identification;

import java.util.List;

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

import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase;
import com.licensis.notaire.application.port.in.identification.ManageIdentificationTypesUseCase.SaveIdentificationTypeCommand;
import com.licensis.notaire.application.port.in.identification.QueryIdentificationTypesUseCase;
import com.licensis.notaire.domain.identification.IdentificationType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Identification type catalog. Validates the request, calls the inbound ports and maps to the web shape;
 * the rules live in the use cases and the domain exceptions are mapped by {@link IdentificationTypeExceptionHandler}
 * (slice 1 of the hexagon, issue #1700).
 */
@RestController
@RequestMapping("/api/v1/identification-types")
@Tag(name = "IdentificationType", description = "API for managing identification types")
public class IdentificationTypeController {

    /** Whole number from 1 to 50, as digits without a leading zero (issue #1526). */
    static final String CHARACTERS_PATTERN = "^([1-9]|[1-4][0-9]|50)$";

    record IdentificationTypeRequest(
            @NotBlank String name,
            @Schema(description = "Number of characters of the identification number: a whole number from 1 to 50 (issue #1526)",
                    example = "8", requiredMode = Schema.RequiredMode.REQUIRED, pattern = CHARACTERS_PATTERN)
            @NotBlank(message = "characters is required")
            @Pattern(regexp = CHARACTERS_PATTERN, message = "characters must be a whole number from 1 to 50")
            String characters) {}

    record IdentificationTypeResponse(
            Integer idIdentificationType,
            String name,
            String characters,
            int version) {}

    private final QueryIdentificationTypesUseCase query;
    private final ManageIdentificationTypesUseCase manage;

    public IdentificationTypeController(QueryIdentificationTypesUseCase query, ManageIdentificationTypesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private IdentificationTypeResponse toResponse(IdentificationType type) {
        return new IdentificationTypeResponse(type.id(), type.name(), type.characters(), type.version());
    }

    private SaveIdentificationTypeCommand toCommand(IdentificationTypeRequest request) {
        return new SaveIdentificationTypeCommand(request.name(), request.characters());
    }

    @GetMapping
    @Operation(summary = "Get all identification types")
    public ResponseEntity<List<IdentificationTypeResponse>> getAll() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get identification type by ID")
    public ResponseEntity<IdentificationTypeResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(query.getById(id)));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create identification type")
    public ResponseEntity<Object> create(@Valid @RequestBody IdentificationTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(manage.create(toCommand(request))));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "Another identification type already has this name")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update identification type")
    public ResponseEntity<Void> update(@PathVariable Integer id,
            @Valid @RequestBody IdentificationTypeRequest request) {
        manage.update(id, toCommand(request));
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "The identification type is in use by people")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete identification type")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        manage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
