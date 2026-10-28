package com.licensis.notaire.adapter.in.web.testimony;

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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.application.port.in.testimony.ManageTestimonyMovementsUseCase;
import com.licensis.notaire.application.port.in.testimony.QueryTestimonyMovementsUseCase;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult;
import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
import com.licensis.notaire.dto.DtoTestimonyMovement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;

/**
 * Testimony movements. Calls inbound ports only; orchestration and persistence live in the use cases
 * (issue #1787).
 */
@RestController
@RequestMapping("/api/v1/testimony-movements")
@Tag(name = "TestimonyMovement", description = "API for managing testimony movement")
public class TestimonyMovementController {

    /** Create/update body; testimony and entry date are required on create (#655). */
    public static class TestimonyMovementWriteRequest extends DtoTestimonyMovement {
        @Override
        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public DtoTestimony getTestimony() {
            return super.getTestimony();
        }

        @Override
        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public Date getDateEntry() {
            return super.getDateEntry();
        }
    }

    private final QueryTestimonyMovementsUseCase queryMovements;
    private final ManageTestimonyMovementsUseCase manageMovements;

    public TestimonyMovementController(
            QueryTestimonyMovementsUseCase queryMovements, ManageTestimonyMovementsUseCase manageMovements) {
        this.queryMovements = queryMovements;
        this.manageMovements = manageMovements;
    }

    @GetMapping
    @Operation(summary = "Get all testimony movement")
    public ResponseEntity<List<DtoTestimonyMovement>> getAll() {
        return ResponseEntity.ok(queryMovements.findAll());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get testimony movement by ID")
    public ResponseEntity<DtoTestimonyMovement> getById(@PathVariable Integer id) {
        return queryMovements.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create testimony movement")
    public ResponseEntity<Object> create(@Valid @RequestBody TestimonyMovementWriteRequest dto) {
        return switch (manageMovements.create(dto)) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(created.dto());
            case SaveResult.Failed failed -> ErrorResponses.createFailed(failed.cause());
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.StaleVersion ignored -> ErrorResponses.staleVersion();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409",
                    description = "version is not the stored one: another user modified the record")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update testimony movement")
    public ResponseEntity<Object> update(
            @PathVariable Integer id, @Valid @RequestBody TestimonyMovementWriteRequest dto) {
        return switch (manageMovements.update(id, dto)) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.StaleVersion ignored -> ErrorResponses.staleVersion();
            case SaveResult.Failed failed -> ErrorResponses.updateFailed(failed.cause());
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete testimony movement")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        return switch (manageMovements.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict conflict -> ResponseEntity.status(HttpStatus.CONFLICT).body(conflict.message());
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "The testimony is not verified or already has an open movement"),
            @ApiResponse(responseCode = "404", description = "Testimony not found")
    })
    @PostMapping("/{idTestimony}/enter-registration")
    @Operation(summary = "Enter testimony for registration",
            description = "Records the entry date of a verified testimony at the property registry")
    public ResponseEntity<DtoTestimonyMovement> registerEntry(@PathVariable Integer idTestimony) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manageMovements.registerEntry(idTestimony));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "The prior registration entry is missing"),
            @ApiResponse(responseCode = "404", description = "Testimony not found")
    })
    @PostMapping("/{idTestimony}/register-inscription")
    @Operation(summary = "Record registration of the testimony",
            description = "Marks the open testimony movement as registered and records the date")
    public ResponseEntity<DtoTestimonyMovement> registerInscription(@PathVariable Integer idTestimony) {
        return ResponseEntity.ok(manageMovements.registerInscription(idTestimony));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "The testimony is not registered"),
            @ApiResponse(responseCode = "404", description = "Testimony not found")
    })
    @PostMapping("/{idTestimony}/withdraw")
    @Operation(summary = "Withdraw a registered testimony",
            description = "Records the exit date and carton number of a registered testimony")
    public ResponseEntity<DtoTestimonyMovement> withdraw(
            @PathVariable Integer idTestimony, @Valid @RequestBody DtoTestimonyMovement dto) {
        return ResponseEntity.ok(manageMovements.withdraw(idTestimony, dto.getCardNumber()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400",
                    description = "The testimony was not withdrawn earlier, or the observed re-entry has no notes"),
            @ApiResponse(responseCode = "404", description = "Testimony not found")
    })
    @PostMapping("/{idTestimony}/reenter")
    @Operation(summary = "Re-enter a withdrawn testimony",
            description = "Creates a new entry movement for a previously withdrawn testimony, without "
                    + "changing the previous movement. The body is optional: carton number, whether it was observed "
                    + "by the registry, and notes (required when it was observed)")
    public ResponseEntity<DtoTestimonyMovement> reenter(
            @PathVariable Integer idTestimony, @RequestBody(required = false) DtoTestimonyMovement dto) {
        TestimonyReentry reentry = dto == null
                ? TestimonyReentry.empty()
                : new TestimonyReentry(dto.getCardNumber(), dto.isObservedByRegistry(), dto.getNotes());
        return ResponseEntity.status(HttpStatus.CREATED).body(manageMovements.reenter(idTestimony, reentry));
    }
}
