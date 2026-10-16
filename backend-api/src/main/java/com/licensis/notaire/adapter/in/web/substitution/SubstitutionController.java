package com.licensis.notaire.adapter.in.web.substitution;

import java.util.Date;
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.licensis.notaire.application.port.in.substitution.ManageSubstitutionsUseCase;
import com.licensis.notaire.application.port.in.substitution.QuerySubstitutionsUseCase;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.PersonView;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionCommand;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Substitutions. Calls inbound ports only; person hydration and persistence live behind
 * {@code SubstitutionOpsPort} (issue #1807).
 */
@RestController
@RequestMapping("/api/v1/substitutions")
@Tag(name = "Substitution", description = "API for managing substitution")
public class SubstitutionController {

    /**
     * Nested person shape for frontend {@code Substitution.fkIdSubstitute/fkIdSubstituted}
     * ({@code DtoPerson}: personId, name, lastName). Request only needs personId.
     */
    record PersonRef(Integer personId, String name, String lastName) {
        PersonRef(Integer personId) {
            this(personId, null, null);
        }
    }

    /**
     * Accepts Bruno/API flat IDs ({@code substitutePersonId}) and the frontend/E2E nested
     * form ({@code fkIdSubstitute: { personId }}).
     */
    record SubstitutionRequest(
            @NotNull Date dateStart,
            @NotNull Date dateEnd,
            String notes,
            Integer substitutePersonId,
            Integer substitutedPersonId,
            PersonRef fkIdSubstitute,
            PersonRef fkIdSubstituted) {}

    // Flat IDs kept for Bruno + SubstitutionControllerIntegrationTest; nested refs for UI.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record SubstitutionResponse(
            Integer idSubstitution,
            Date dateStart,
            Date dateEnd,
            String notes,
            Integer substitutePersonId,
            Integer substitutedPersonId,
            PersonRef fkIdSubstitute,
            PersonRef fkIdSubstituted,
            int version) {}

    private final QuerySubstitutionsUseCase querySubstitutions;
    private final ManageSubstitutionsUseCase manageSubstitutions;

    public SubstitutionController(
            QuerySubstitutionsUseCase querySubstitutions, ManageSubstitutionsUseCase manageSubstitutions) {
        this.querySubstitutions = querySubstitutions;
        this.manageSubstitutions = manageSubstitutions;
    }

    private Integer resolveSubstituteId(SubstitutionRequest request) {
        if (request.substitutePersonId() != null) {
            return request.substitutePersonId();
        }
        return request.fkIdSubstitute() != null ? request.fkIdSubstitute().personId() : null;
    }

    private Integer resolveSubstitutedId(SubstitutionRequest request) {
        if (request.substitutedPersonId() != null) {
            return request.substitutedPersonId();
        }
        return request.fkIdSubstituted() != null ? request.fkIdSubstituted().personId() : null;
    }

    private SubstitutionCommand toCommand(SubstitutionRequest request) {
        return new SubstitutionCommand(
                request.dateStart(),
                request.dateEnd(),
                request.notes(),
                resolveSubstituteId(request),
                resolveSubstitutedId(request));
    }

    private PersonRef toPersonRef(PersonView person) {
        if (person == null || person.personId() == null) {
            return null;
        }
        return new PersonRef(person.personId(), person.name(), person.lastName());
    }

    private SubstitutionResponse toResponse(SubstitutionView view) {
        return new SubstitutionResponse(
                view.idSubstitution(),
                view.dateStart(),
                view.dateEnd(),
                view.notes(),
                view.substitutePersonId(),
                view.substitutedPersonId(),
                toPersonRef(view.substitute()),
                toPersonRef(view.substituted()),
                view.version());
    }

    @GetMapping
    @Operation(summary = "Get all substitution")
    public ResponseEntity<List<SubstitutionResponse>> getAll() {
        return ResponseEntity.ok(querySubstitutions.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get substitution by ID")
    public ResponseEntity<SubstitutionResponse> getById(@PathVariable Integer id) {
        return querySubstitutions.findById(id)
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
    @Operation(summary = "Create substitution")
    public ResponseEntity<Object> create(@Valid @RequestBody SubstitutionRequest request) {
        return switch (manageSubstitutions.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.view()));
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update substitution")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody SubstitutionRequest request) {
        return switch (manageSubstitutions.update(id, toCommand(request))) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete substitution")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageSubstitutions.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.ServerError ignored -> ResponseEntity.internalServerError().build();
        };
    }
}
