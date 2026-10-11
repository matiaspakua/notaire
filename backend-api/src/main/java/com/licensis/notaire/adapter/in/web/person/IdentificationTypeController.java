package com.licensis.notaire.adapter.in.web.person;

import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("/api/v1/identification-types")
@Tag(name = "IdentificationType", description = "API for managing identification types")
public class IdentificationTypeController {

    private static final Logger log = LoggerFactory.getLogger(IdentificationTypeController.class);

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

    /** Reason for a 409 on delete: people still use the type (issue #1361). */
    static final String IN_USE_MESSAGE =
            "Cannot delete: the identification type is in use by people";

    /** Reason for a 409 on create/rename: one row per name, in any letter case (issue #1361). */
    static final String DUPLICATE_NAME_MESSAGE = "An identification type with this name already exists";

    private final IdentificationTypeRepository repository;
    private final PersonRepository personRepository;

    public IdentificationTypeController(IdentificationTypeRepository repository,
            PersonRepository personRepository) {
        this.repository = repository;
        this.personRepository = personRepository;
    }

    /**
     * The DNI default for new people looks the type up by name, so two rows that differ only in
     * letter case or spaces would make it ambiguous: reject a name another row already has.
     */
    private void requireUniqueName(String name, Integer ownId) {
        String wanted = name.trim();
        boolean taken = repository.findAll().stream()
                .filter(t -> ownId == null || !ownId.equals(t.getIdIdentificationType()))
                .anyMatch(t -> t.getName() != null && t.getName().trim().equalsIgnoreCase(wanted));
        if (taken) {
            throw new NotaireException(HttpStatus.CONFLICT.value(), DUPLICATE_NAME_MESSAGE);
        }
    }

    private IdentificationTypeResponse toResponse(IdentificationType entity) {
        return new IdentificationTypeResponse(
                entity.getIdIdentificationType(),
                entity.getName(),
                entity.getCharacters(),
                entity.getVersion());
    }

    private void applyRequest(IdentificationType entity, IdentificationTypeRequest request) {
        entity.setName(request.name().trim());
        entity.setCharacters(request.characters());
    }

    @GetMapping
    @Operation(summary = "Get all identification types")
    @Transactional(readOnly = true)
    public ResponseEntity<List<IdentificationTypeResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get identification type by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<IdentificationTypeResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create identification type")
    public ResponseEntity<Object> create(@Valid @RequestBody IdentificationTypeRequest request) {
        requireUniqueName(request.name(), null);
        try {
            IdentificationType entity = new IdentificationType();
            applyRequest(entity, request);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create identification type", e);
            return ResponseEntity.internalServerError().build();
        }
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
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        requireUniqueName(request.name(), id);
        return repository.findById(id).map(existing -> {
            try {
                applyRequest(existing, request);
                repository.save(existing);
                return ResponseEntity.ok().<Void>build();
            } catch (Exception e) {
                log.error("Failed to update identification type id {}", id, e);
                return ResponseEntity.internalServerError().<Void>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "The identification type is in use by people")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete identification type")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (personRepository.existsByFkIdIdentificationTypeIdIdentificationType(id)) {
            throw new NotaireException(HttpStatus.CONFLICT.value(), IN_USE_MESSAGE);
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete identification type id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
