package com.licensis.notaire.adapter.in.web.registrationdraft;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.support.CreatedResponses;
import com.licensis.notaire.application.port.in.registrationdraft.ManageRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.in.registrationdraft.QueryRegistrationDraftsUseCase;
import com.licensis.notaire.application.port.out.registrationdraft.RegistrationDraftOpsPort.DraftResult;
import com.licensis.notaire.dto.DtoRegistrationDraft;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * REST API for generating and tracking the Registration draft (CU82).
 * Calls inbound ports only; orchestration lives behind {@code RegistrationDraftOpsPort}
 * (issue #1819).
 */
@RestController
@RequestMapping("/api/v1/registration-drafts")
@Tag(name = "Registration drafts", description = "API for generating and tracking the registration draft")
public class RegistrationDraftController {

    private final QueryRegistrationDraftsUseCase queryRegistrationDrafts;
    private final ManageRegistrationDraftsUseCase manageRegistrationDrafts;

    public RegistrationDraftController(
            QueryRegistrationDraftsUseCase queryRegistrationDrafts,
            ManageRegistrationDraftsUseCase manageRegistrationDrafts) {
        this.queryRegistrationDrafts = queryRegistrationDrafts;
        this.manageRegistrationDrafts = manageRegistrationDrafts;
    }

    /** Body of the generate request; the deed is required (issue #655). */
    public record GenerateRequest(
            @NotNull @Schema(description = "Deed for which the registration draft is generated",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            Integer idDeed) {
    }

    public record SubmitRequest(Date dateSubmission, String registryEntryNumber) {
    }

    public record ObserveRequest(String registryNotes, Date dateCorrection) {
    }

    public record RegisterRequest(Date dateReception, String finalRegistrationNumber) {
    }

    private ResponseEntity<Object> toResponse(DraftResult result) {
        return switch (result) {
            case DraftResult.Ok ok -> ResponseEntity.ok(ok.dto());
            case DraftResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DraftResult.Invalid invalid ->
                    ResponseEntity.badRequest().body(Map.of("message", invalid.message()));
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Registration draft not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU82 - Query a registration draft by ID")
    public ResponseEntity<DtoRegistrationDraft> getById(@PathVariable Integer id) {
        return queryRegistrationDrafts.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registration draft generated"),
        @ApiResponse(responseCode = "400", description = "Incomplete cadastral or registry data, or the deed is not signed"),
        @ApiResponse(responseCode = "404", description = "Deed not found"),
        @ApiResponse(responseCode = "409", description = "The deed already has a registration draft")
    })
    @PostMapping
    @Operation(summary = "CU82 - Generate the registration draft for a deed on a property")
    public ResponseEntity<Object> generate(@Valid @RequestBody GenerateRequest request) {
        return switch (manageRegistrationDrafts.generate(request.idDeed())) {
            case DraftResult.Ok ok ->
                    CreatedResponses.of(ok.dto(), "/api/v1/registration-drafts", ok.dto().getIdRegistrationDraft());
            case DraftResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DraftResult.Invalid invalid ->
                    ResponseEntity.badRequest().body(Map.of("message", invalid.message()));
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Submission recorded"),
        @ApiResponse(responseCode = "400", description = "The registration draft is not in status Generada"),
        @ApiResponse(responseCode = "404", description = "Registration draft not found")
    })
    @PutMapping("/{id}/submit")
    @Operation(summary = "CU82 - Record the registration draft submission to the registry")
    public ResponseEntity<Object> submit(@PathVariable Integer id, @Valid @RequestBody SubmitRequest request) {
        return toResponse(manageRegistrationDrafts.submit(id, request.dateSubmission(), request.registryEntryNumber()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Observation recorded"),
        @ApiResponse(responseCode = "400", description = "The registration draft is not submitted"),
        @ApiResponse(responseCode = "404", description = "Registration draft not found")
    })
    @PutMapping("/{id}/observe")
    @Operation(summary = "CU82 - Record an observation issued by the registry")
    public ResponseEntity<Object> observe(@PathVariable Integer id, @Valid @RequestBody ObserveRequest request) {
        return toResponse(manageRegistrationDrafts.observe(id, request.registryNotes(), request.dateCorrection()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Final registration recorded"),
        @ApiResponse(responseCode = "400", description = "The registration draft is not submitted"),
        @ApiResponse(responseCode = "404", description = "Registration draft not found")
    })
    @PutMapping("/{id}/register")
    @Operation(summary = "CU82 - Record the final registration of the registration draft")
    public ResponseEntity<Object> recordRegistration(
            @PathVariable Integer id, @Valid @RequestBody RegisterRequest request) {
        return toResponse(
                manageRegistrationDrafts.recordRegistration(
                        id, request.dateReception(), request.finalRegistrationNumber()));
    }
}
