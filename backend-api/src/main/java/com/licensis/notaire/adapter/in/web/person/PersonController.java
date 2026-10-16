package com.licensis.notaire.adapter.in.web.person;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.application.port.in.person.ManagePeopleUseCase;
import com.licensis.notaire.application.port.in.person.QueryPeopleUseCase;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonCommand;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult;
import com.licensis.notaire.audit.AuditPersonalDataRead;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * People. Calls inbound ports only; identification-type resolution and persistence live behind
 * {@code PersonOpsPort} (issue #1813).
 */
@RestController
@RequestMapping("/api/v1/people")
@Tag(name = "People", description = "API to manage people")
public class PersonController {

    record IdentificationTypeRef(Integer idIdentificationType) {}

    record PersonRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String identificationNumber,
            boolean isClient,
            String nationality,
            @JsonAlias("cuil") String taxId,
            String sex,
            Date birthDate,
            String maritalStatus,
            Integer marriageCount,
            String occupation,
            String address,
            String phone,
            String email,
            Integer notaryRegistrationNumber,
            @Schema(description = "Identification type id from GET /identification-types. Omitted on create: DNI. "
                    + "An id that is not in the catalog answers 400 (#1361).")
            Integer identificationTypeId,
            @Schema(description = "Legacy nested form of identificationTypeId; identificationTypeId wins when both are sent.")
            IdentificationTypeRef fkIdIdentificationType) {}

    private final QueryPeopleUseCase queryPeople;
    private final ManagePeopleUseCase managePeople;

    public PersonController(QueryPeopleUseCase queryPeople, ManagePeopleUseCase managePeople) {
        this.queryPeople = queryPeople;
        this.managePeople = managePeople;
    }

    private PersonCommand toCommand(PersonRequest request) {
        Integer nested = request.fkIdIdentificationType() != null
                ? request.fkIdIdentificationType().idIdentificationType()
                : null;
        return new PersonCommand(
                request.firstName(),
                request.lastName(),
                request.identificationNumber(),
                request.isClient(),
                request.nationality(),
                request.taxId(),
                request.sex(),
                request.birthDate(),
                request.maritalStatus(),
                request.marriageCount(),
                request.occupation(),
                request.address(),
                request.phone(),
                request.email(),
                request.notaryRegistrationNumber(),
                request.identificationTypeId(),
                nested);
    }

    private Map<String, Object> duplicateBody(SaveResult.Duplicate duplicate) {
        return Map.of("message", duplicate.message(), "existingPersonId", duplicate.existingPersonId());
    }

    @AuditPersonalDataRead(aggregated = true)
    @GetMapping
    @Operation(summary = "Get people, one page at a time (default 20, sorted by id)")
    public ResponseEntity<Page<PersonResponse>> getAllPeople(
            @ParameterObject @PageableDefault(size = 20, sort = "idPerson") Pageable pageable) {
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? "idPerson,asc"
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryPeople.findAll(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return ResponseEntity.ok(new PageImpl<>(
                result.content().stream().map(PersonResponse::from).toList(), pageable, result.totalElements()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @AuditPersonalDataRead
    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID")
    public ResponseEntity<PersonResponse> getPersonById(@PathVariable Integer id) {
        return queryPeople.findById(id)
                .map(PersonResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create new person")
    public ResponseEntity<Object> createPerson(@Valid @RequestBody PersonRequest request) {
        return switch (managePeople.create(toCommand(request))) {
            case SaveResult.Created created ->
                ResponseEntity.status(HttpStatus.CREATED).body(PersonResponse.from(created.view()));
            case SaveResult.Duplicate duplicate ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateBody(duplicate));
            case SaveResult.Failed failed -> ErrorResponses.createFailed(failed.cause());
            case SaveResult.Updated ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update person")
    public ResponseEntity<Object> updatePerson(@PathVariable Integer id, @Valid @RequestBody PersonRequest request) {
        return switch (managePeople.update(id, toCommand(request))) {
            case SaveResult.Updated updated -> ResponseEntity.ok(PersonResponse.from(updated.view()));
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.Duplicate duplicate ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateBody(duplicate));
            case SaveResult.Failed failed ->
                // A data constraint answers 400; any other failure keeps 409 on create path historically;
                // update used createFailed for non-duplicate exceptions (issue #579).
                ErrorResponses.createFailed(failed.cause());
            case SaveResult.Created ignored -> ResponseEntity.internalServerError().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person")
    public ResponseEntity<Object> deletePerson(@PathVariable Integer id) {
        return switch (managePeople.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict conflict ->
                ResponseEntity.status(HttpStatus.CONFLICT).body(conflict.message());
        };
    }

    @AuditPersonalDataRead
    @GetMapping("/search")
    @Operation(summary = "Search people by first name, last name, identification number or type (CU61)")
    public ResponseEntity<List<PersonResponse>> searchPeople(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String identificationNumber,
            @RequestParam(required = false) Integer idIdentificationType,
            @RequestParam(required = false) Boolean isClient) {

        return ResponseEntity.ok(queryPeople.search(
                        firstName, lastName, identificationNumber, idIdentificationType, isClient)
                .stream()
                .map(PersonResponse::from)
                .toList());
    }
}
