package com.licensis.notaire.adapter.in.web.folio;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.adapter.in.web.support.CreatedResponses;
import com.licensis.notaire.adapter.in.web.support.DeedRef;
import com.licensis.notaire.adapter.in.web.support.NotaryRef;
import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase;
import com.licensis.notaire.application.port.in.folio.ManageFoliosUseCase.SaveFolioCommand;
import com.licensis.notaire.application.port.in.folio.QueryFoliosUseCase;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.domain.folio.FolioInUseException;
import com.licensis.notaire.domain.folio.FolioInvalidException;
import com.licensis.notaire.domain.folio.FolioNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Folio instances. Calls inbound ports only; Utilizado lock and deed-link rules live in the use cases (issue #1727).
 */
@RestController
@RequestMapping("/api/v1/folios")
@Tag(name = "Folio", description = "API for managing folio")
public class FolioController {

    private static final Logger log = LoggerFactory.getLogger(FolioController.class);

    public record FolioTypeRef(Integer idFolioType, String name, String notes, boolean enabled, boolean isAuxiliary) {
        public static FolioTypeRef from(Folio folio) {
            if (folio == null || folio.folioTypeId() == null) {
                return null;
            }
            return new FolioTypeRef(
                    folio.folioTypeId(),
                    folio.folioTypeName(),
                    folio.folioTypeNotes(),
                    folio.folioTypeEnabled(),
                    folio.folioTypeAuxiliary());
        }
    }

    public record FolioResponse(
            Integer idFolio,
            int number,
            int year,
            String status,
            String notes,
            FolioTypeRef fkIdFolioType,
            NotaryRef fkIdNotaryPerson,
            DeedRef fkIdDeed,
            int version) {
        public static FolioResponse from(Folio folio) {
            NotaryRef notary = folio.notaryId() == null
                    ? null
                    : new NotaryRef(
                            folio.notaryId(),
                            folio.notaryFirstName(),
                            folio.notaryLastName(),
                            folio.notaryIdentificationNumber(),
                            folio.notaryRegistrationNumber());
            DeedRef deed = folio.deedId() == null
                    ? null
                    : new DeedRef(
                            folio.deedId(),
                            folio.deedNumber() == null ? 0 : folio.deedNumber(),
                            folio.deedStatus());
            return new FolioResponse(
                    folio.id(),
                    folio.number(),
                    folio.year(),
                    folio.status(),
                    folio.notes(),
                    FolioTypeRef.from(folio),
                    notary,
                    deed,
                    folio.version());
        }
    }

    record FolioRequest(
            int number,
            int year,
            @NotBlank String status,
            String notes,
            Integer typeFolioId,
            Integer notaryId,
            Integer deedId) {}

    private final QueryFoliosUseCase query;
    private final ManageFoliosUseCase manage;

    public FolioController(QueryFoliosUseCase query, ManageFoliosUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    private SaveFolioCommand toCommand(FolioRequest request) {
        return new SaveFolioCommand(
                request.number(),
                request.year(),
                request.status(),
                request.notes(),
                request.typeFolioId(),
                request.notaryId(),
                request.deedId());
    }

    @GetMapping
    @Operation(summary = "Get all folios")
    public ResponseEntity<List<FolioResponse>> getAll() {
        try {
            return ResponseEntity.ok(query.findAll().stream().map(FolioResponse::from).toList());
        } catch (Exception e) {
            log.error("Failed to list folios", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search folios by status")
    public ResponseEntity<List<FolioResponse>> search(@RequestParam String status) {
        return ResponseEntity.ok(query.findByStatus(status).stream().map(FolioResponse::from).toList());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether a folio is in use")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return query.isInUse(id)
                .map(inUse -> ResponseEntity.ok(Map.of("inUse", inUse)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get folio by ID")
    public ResponseEntity<FolioResponse> getById(@PathVariable Integer id) {
        return query.findById(id)
                .map(folio -> ResponseEntity.ok(FolioResponse.from(folio)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create folio")
    public ResponseEntity<FolioResponse> create(@Valid @RequestBody FolioRequest request) {
        try {
            Folio saved = manage.create(toCommand(request));
            return CreatedResponses.of(FolioResponse.from(saved), "/api/v1/folios", saved.id());
        } catch (FolioInvalidException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to create folio", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update folio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody FolioRequest request) {
        try {
            Folio saved = manage.update(id, toCommand(request));
            return ResponseEntity.ok(FolioResponse.from(saved));
        } catch (FolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (FolioInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (FolioInvalidException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update folio {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete folio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (FolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (FolioInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to delete folio {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
