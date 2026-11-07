package com.licensis.notaire.adapter.in.web.concept;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase;
import com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.InUseResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult;
import com.licensis.notaire.domain.concept.ConceptInUseException;
import com.licensis.notaire.domain.concept.ConceptNotFoundException;
import com.licensis.notaire.domain.concept.ConceptOperationFailedException;
import com.licensis.notaire.dto.DtoConcept;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Concepts. Calls inbound ports only; entity and BudgetTemplateRepository access live behind
 * {@code ConceptOpsPort} (issue #1815). Write failures throw domain exceptions mapped by
 * {@link ConceptExceptionHandler} (#577).
 */
@RestController
@RequestMapping("/api/v1/concepts")
@Tag(name = "Concepts", description = "API for managing concepts")
public class ConceptController {

    /**
     * Write body for create/update (#655). Same JSON fields as {@link DtoConcept}; validation runs
     * before the use case.
     */
    public record ConceptWriteRequest(
            @NotBlank String name,
            @NotNull BigDecimal value,
            Integer percentage,
            Boolean enabled,
            @com.fasterxml.jackson.annotation.JsonAlias("fixedConcept") Boolean fixed,
            Integer version) {
    }

    private final QueryConceptsUseCase queryConcepts;
    private final ManageConceptsUseCase manageConcepts;

    public ConceptController(QueryConceptsUseCase queryConcepts, ManageConceptsUseCase manageConcepts) {
        this.queryConcepts = queryConcepts;
        this.manageConcepts = manageConcepts;
    }

    @GetMapping
    @Operation(summary = "Get all concepts")
    public ResponseEntity<List<DtoConcept>> getAllConcepts() {
        return ResponseEntity.ok(queryConcepts.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Search concepts by name")
    public ResponseEntity<List<DtoConcept>> searchConcepts(@RequestParam String name) {
        return ResponseEntity.ok(queryConcepts.searchByName(name));
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether a concept is in use")
    public ResponseEntity<Map<String, Boolean>> isConceptInUse(@PathVariable Integer id) {
        return switch (queryConcepts.isInUse(id)) {
            case InUseResult.Ok ok -> ResponseEntity.ok(Map.of("inUse", ok.inUse()));
            case InUseResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get concept by ID")
    public ResponseEntity<DtoConcept> getConceptById(@PathVariable Integer id) {
        return queryConcepts.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create concept")
    public ResponseEntity<DtoConcept> createConcept(@Valid @RequestBody ConceptWriteRequest request) {
        return switch (manageConcepts.create(toDto(request))) {
            case SaveResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(created.dto());
            case SaveResult.Failed failed ->
                throw new ConceptOperationFailedException(ConceptOperationFailedException.Operation.CREATE, failed.cause());
            case SaveResult.InUse inUse -> throw new ConceptInUseException(inUse.error());
            case SaveResult.NotFound ignored -> throw new ConceptNotFoundException();
            case SaveResult.Updated ignored ->
                throw new ConceptOperationFailedException(
                        ConceptOperationFailedException.Operation.CREATE, new IllegalStateException("unexpected Updated"));
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update concept")
    public ResponseEntity<Void> updateConcept(@PathVariable Integer id,
            @Valid @RequestBody ConceptWriteRequest request) {
        switch (manageConcepts.update(id, toDto(request))) {
            case SaveResult.Updated ignored -> {
                return ResponseEntity.ok().build();
            }
            case SaveResult.NotFound ignored -> throw new ConceptNotFoundException();
            case SaveResult.InUse inUse -> throw new ConceptInUseException(inUse.error());
            case SaveResult.Failed failed ->
                throw new ConceptOperationFailedException(ConceptOperationFailedException.Operation.UPDATE, failed.cause());
            case SaveResult.Created ignored ->
                throw new ConceptOperationFailedException(
                        ConceptOperationFailedException.Operation.UPDATE, new IllegalStateException("unexpected Created"));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete concept")
    public ResponseEntity<Void> deleteConcept(@PathVariable Integer id) {
        switch (manageConcepts.delete(id)) {
            case DeleteResult.Deleted ignored -> {
                return ResponseEntity.noContent().build();
            }
            case DeleteResult.NotFound ignored -> throw new ConceptNotFoundException();
            case DeleteResult.InUse inUse -> throw new ConceptInUseException(inUse.error());
            case DeleteResult.Conflict conflict ->
                throw new ConceptOperationFailedException(
                        ConceptOperationFailedException.Operation.DELETE, conflict.cause());
        }
    }

    private static DtoConcept toDto(ConceptWriteRequest request) {
        DtoConcept dto = new DtoConcept();
        dto.setName(request.name());
        dto.setValue(request.value());
        dto.setPercentage(request.percentage());
        dto.setEnabled(request.enabled() != null ? request.enabled() : Boolean.TRUE);
        dto.setFixed(request.fixed() != null ? request.fixed() : Boolean.FALSE);
        dto.setVersion(Objects.requireNonNullElse(request.version(), 0));
        return dto;
    }
}
