package com.licensis.notaire.adapter.in.web.deed;

import java.util.Date;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.licensis.notaire.adapter.in.web.person.PersonController.PersonResponse;
import com.licensis.notaire.application.port.in.deed.ManageDeedsUseCase;
import com.licensis.notaire.application.port.in.deed.QueryDeedsUseCase;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedCommand;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.NotaryView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult;
import com.licensis.notaire.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Deeds. Calls inbound ports only; folio link, status vocabulary, and signing live behind
 * {@code DeedOpsPort} (issue #1811).
 */
@RestController
@RequestMapping("/api/v1/deeds")
@Tag(name = "Deeds", description = "API for managing deeds")
public class DeedController {

    record DeedRequest(
            Integer number,
            String body,
            // Optional: frontend create form omits status; default applied in the ops adapter (CU05).
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            Integer idFolio) {}

    record DeedResponse(
            Integer idDeed,
            int number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            int version) {}

    private final QueryDeedsUseCase queryDeeds;
    private final ManageDeedsUseCase manageDeeds;

    public DeedController(QueryDeedsUseCase queryDeeds, ManageDeedsUseCase manageDeeds) {
        this.queryDeeds = queryDeeds;
        this.manageDeeds = manageDeeds;
    }

    private DeedCommand toCommand(DeedRequest request) {
        return new DeedCommand(
                request.number(),
                request.body(),
                request.status(),
                request.dateDeedrecording(),
                request.dateRegistration(),
                request.registrationEntryNumber(),
                request.notes(),
                request.idFolio());
    }

    private DeedResponse toResponse(DeedView view) {
        return new DeedResponse(
                view.idDeed(),
                view.number(),
                view.body(),
                view.status(),
                view.dateDeedrecording(),
                view.dateRegistration(),
                view.registrationEntryNumber(),
                view.notes(),
                view.version());
    }

    private PersonResponse toPersonResponse(NotaryView notary) {
        return new PersonResponse(
                notary.personId(),
                notary.firstName(),
                notary.lastName(),
                notary.identificationNumber(),
                notary.isClient(),
                notary.nationality(),
                notary.taxId(),
                notary.sex(),
                notary.birthDate(),
                notary.maritalStatus(),
                notary.marriageCount(),
                notary.occupation(),
                notary.address(),
                notary.phone(),
                notary.email(),
                notary.notaryRegistrationNumber(),
                notary.identificationTypeId(),
                notary.version());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Status outside the vocabulary", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @Operation(summary = "Get all deeds, optionally only the of a status")
    public ResponseEntity<Page<DeedResponse>> getAll(
            @Parameter(description = "Deed status (any letter case; any other value responds 400). "
                    + "Without this parameter, every deed is listed.",
                    schema = @Schema(allowableValues = {"Sin Firmar", "Firmada", "Anulada", "No Paso", "Inscripta"}))
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? ""
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryDeeds.findAll(status, pageable.getPageNumber(), pageable.getPageSize(), sort);
        return ResponseEntity.ok(new PageImpl<>(
                result.content().stream().map(this::toResponse).toList(), pageable, result.totalElements()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get deed by ID")
    public ResponseEntity<DeedResponse> getById(@PathVariable Integer id) {
        return queryDeeds.findById(id)
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
    @Operation(summary = "Create deed")
    public ResponseEntity<DeedResponse> create(@Valid @RequestBody DeedRequest request) {
        return switch (manageDeeds.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update deed")
    public ResponseEntity<DeedResponse> update(@PathVariable Integer id, @Valid @RequestBody DeedRequest request) {
        return switch (manageDeeds.update(id, toCommand(request))) {
            case SaveResult.Updated updated -> ResponseEntity.ok(toResponse(updated.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete deed")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageDeeds.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @GetMapping("/available-notaries")
    @Operation(summary = "Get the list of available notaries (with a registry)")
    public ResponseEntity<List<PersonResponse>> getAvailableNotaries() {
        return ResponseEntity.ok(queryDeeds.findAvailableNotaries().stream().map(this::toPersonResponse).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search deeds by number")
    public ResponseEntity<List<DeedResponse>> searchDeeds(@RequestParam(required = false) Integer number) {
        return ResponseEntity.ok(queryDeeds.searchByNumber(number).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "The deed is not in status 'Sin Firmar' or does not have folio assigned"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PostMapping("/{id}/sign")
    @Operation(summary = "Sign deed",
            description = "Transitions a deed 'Sin Firmar' with folio assigned to the status 'Firmada'")
    public ResponseEntity<DeedResponse> sign(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(manageDeeds.sign(id)));
    }
}
