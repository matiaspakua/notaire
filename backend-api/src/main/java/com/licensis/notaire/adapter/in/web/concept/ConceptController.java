package com.licensis.notaire.adapter.in.web.concept;

import java.util.List;
import java.util.Map;

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

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.concept.ManageConceptsUseCase;
import com.licensis.notaire.application.port.in.concept.QueryConceptsUseCase;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.InUseResult;
import com.licensis.notaire.application.port.out.concept.ConceptOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoConcept;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Concepts. Calls inbound ports only; entity and BudgetTemplateRepository access live behind
 * {@code ConceptOpsPort} (issue #1815).
 */
@RestController
@RequestMapping("/api/v1/concepts")
@Tag(name = "Concepts", description = "API for managing concepts")
public class ConceptController {

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
    public ResponseEntity<Object> createConcept(@RequestBody DtoConcept dto) {
        return switch (manageConcepts.create(dto)) {
            case SaveResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(created.dto());
            case SaveResult.Failed failed -> ErrorResponses.createFailed(failed.cause());
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.InUse inUse ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.error()));
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
    public ResponseEntity<Object> updateConcept(@PathVariable Integer id, @RequestBody DtoConcept dto) {
        return switch (manageConcepts.update(id, dto)) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.InUse inUse ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.error()));
            case SaveResult.Failed failed -> ErrorResponses.updateFailed(failed.cause());
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete concept")
    public ResponseEntity<Object> deleteConcept(@PathVariable Integer id) {
        return switch (manageConcepts.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.InUse inUse ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", inUse.error()));
            case DeleteResult.Conflict conflict -> ErrorResponses.conflict(conflict.cause());
        };
    }
}
