package com.licensis.notaire.adapter.in.web.registrationdraft;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.licensis.notaire.adapter.in.web.support.CreatedResponses;
import com.licensis.notaire.dto.DtoRegistrationDraft;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.usecase.registrationdraft.RegistrationDraftService;
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
 * REST API for generating and tracking the Registration draft
 * (CU82).
 */
@RestController
@RequestMapping("/api/v1/registration-drafts")
@Tag(name = "Registration drafts", description = "API for generating and tracking the registration draft")
public class RegistrationDraftController {

    private final RegistrationDraftService registrationDraftService;

    public RegistrationDraftController(RegistrationDraftService registrationDraftService) {
        this.registrationDraftService = registrationDraftService;
    }

    /** Body of the generate request; the deed is required (issue #655). */
    public record GenerateRequest(
            @NotNull @Schema(description = "Deed for the that generates the minuta",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            Integer idDeed) {
    }

    public record PresentarRequest(Date dateSubmission, String registryEntryNumber) {
    }

    public record ObservarRequest(String registryNotes, Date dateCorrection) {
    }

    public record InscribirRequest(Date dateReception, String finalRegistrationNumber) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Minuta not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU82 - Query a registration draft by ID")
    public ResponseEntity<DtoRegistrationDraft> getById(@PathVariable Integer id) {
        return registrationDraftService.findById(id)
                .map(draft -> ResponseEntity.ok(draft.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Minuta generada"),
        @ApiResponse(responseCode = "400", description = "Datos catastrales/registrales incompletos or deed not firmada"),
        @ApiResponse(responseCode = "404", description = "Deed no encontrada"),
        @ApiResponse(responseCode = "409", description = "The deed already has a registration draft")
    })
    @PostMapping
    @Operation(summary = "CU82 - Generate the registration draft for a deed on a property")
    public ResponseEntity<Object> generate(@Valid @RequestBody GenerateRequest request) {
        try {
            DtoRegistrationDraft dto = registrationDraftService.generate(request.idDeed()).getDto();
            return CreatedResponses.of(dto, "/api/v1/registration-drafts", dto.getIdRegistrationDraft());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Presentación registrada"),
        @ApiResponse(responseCode = "400", description = "The minuta is not in status Generada"),
        @ApiResponse(responseCode = "404", description = "Minuta not found")
    })
    @PutMapping("/{id}/submit")
    @Operation(summary = "CU82 - Record the presentación of the minuta ante the Registro")
    public ResponseEntity<Object> presentar(@PathVariable Integer id, @RequestBody PresentarRequest request) {
        try {
            DtoRegistrationDraft dto = registrationDraftService
                    .presentar(id, request.dateSubmission(), request.registryEntryNumber()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Observation registrada"),
        @ApiResponse(responseCode = "400", description = "The minuta is not presented"),
        @ApiResponse(responseCode = "404", description = "Minuta not found")
    })
    @PutMapping("/{id}/observe")
    @Operation(summary = "CU82 - Record a observation formulada by the Registro")
    public ResponseEntity<Object> observar(@PathVariable Integer id, @RequestBody ObservarRequest request) {
        try {
            DtoRegistrationDraft dto = registrationDraftService
                    .observar(id, request.registryNotes(), request.dateCorrection()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration definitiva registrada"),
        @ApiResponse(responseCode = "400", description = "The minuta is not presented"),
        @ApiResponse(responseCode = "404", description = "Minuta not found")
    })
    @PutMapping("/{id}/register")
    @Operation(summary = "CU82 - Record the registration definitiva of the minuta")
    public ResponseEntity<Object> inscribir(@PathVariable Integer id, @RequestBody InscribirRequest request) {
        try {
            DtoRegistrationDraft dto = registrationDraftService
                    .inscribir(id, request.dateReception(), request.finalRegistrationNumber()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
