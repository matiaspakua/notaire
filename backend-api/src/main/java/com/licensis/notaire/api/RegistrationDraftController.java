package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoRegistrationDraft;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.service.RegistrationDraftService;
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
 * REST API para generar y hacer seguimiento de la Minuta de Inscripción
 * (CU82).
 */
@RestController
@RequestMapping("/api/v1/minutas-inscripcion")
@Tag(name = "Minutas de Inscripción", description = "API para generar y hacer seguimiento de la minuta de inscripción")
public class RegistrationDraftController {

    private final RegistrationDraftService registrationDraftService;

    public RegistrationDraftController(RegistrationDraftService registrationDraftService) {
        this.registrationDraftService = registrationDraftService;
    }

    public record GenerateRequest(Integer idDeed) {
    }

    public record PresentarRequest(Date dateSubmission, String registryEntryNumber) {
    }

    public record ObservarRequest(String registryNotes, Date dateCorrection) {
    }

    public record InscribirRequest(Date dateReception, String finalRegistrationNumber) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Minuta no encontrada")
    })
    @GetMapping("/{id}")
    @Operation(summary = "CU82 - Consultar una minuta de inscripción por ID")
    public ResponseEntity<DtoRegistrationDraft> getById(@PathVariable Integer id) {
        return registrationDraftService.findById(id)
                .map(draft -> ResponseEntity.ok(draft.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Minuta generada"),
        @ApiResponse(responseCode = "400", description = "Datos catastrales/registrales incompletos o escritura no firmada"),
        @ApiResponse(responseCode = "404", description = "Escritura no encontrada")
    })
    @PostMapping
    @Operation(summary = "CU82 - Generar la minuta de inscripción para una escritura sobre un inmueble")
    public ResponseEntity<Object> generate(@RequestBody GenerateRequest request) {
        try {
            DtoRegistrationDraft dto = registrationDraftService.generate(request.idDeed()).getDto();
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Presentación registrada"),
        @ApiResponse(responseCode = "400", description = "La minuta no está en estado Generada"),
        @ApiResponse(responseCode = "404", description = "Minuta no encontrada")
    })
    @PutMapping("/{id}/presentar")
    @Operation(summary = "CU82 - Registrar la presentación de la minuta ante el Registro")
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
        @ApiResponse(responseCode = "200", description = "Observación registrada"),
        @ApiResponse(responseCode = "400", description = "La minuta no está presentada"),
        @ApiResponse(responseCode = "404", description = "Minuta no encontrada")
    })
    @PutMapping("/{id}/observar")
    @Operation(summary = "CU82 - Registrar una observación formulada por el Registro")
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
        @ApiResponse(responseCode = "200", description = "Inscripción definitiva registrada"),
        @ApiResponse(responseCode = "400", description = "La minuta no está presentada"),
        @ApiResponse(responseCode = "404", description = "Minuta no encontrada")
    })
    @PutMapping("/{id}/inscribir")
    @Operation(summary = "CU82 - Registrar la inscripción definitiva de la minuta")
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
