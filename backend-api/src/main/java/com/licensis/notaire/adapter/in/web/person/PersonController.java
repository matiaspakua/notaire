package com.licensis.notaire.adapter.in.web.person;

import com.licensis.notaire.exception.DuplicatePersonException;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.application.usecase.person.PersonService;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
            Integer identificationTypeId,
            IdentificationTypeRef fkIdIdentificationType) {}

    public record PersonResponse(
            Integer personId,
            String firstName,
            @NotBlank String lastName,
            @NotBlank String identificationNumber,
            boolean isClient,
            String nationality,
            String taxId,
            String sex,
            Date birthDate,
            String maritalStatus,
            Integer marriageCount,
            String occupation,
            String address,
            String phone,
            String email,
            Integer notaryRegistrationNumber,
            Integer identificationTypeId,
            int version) {

        public static PersonResponse from(Person person) {
        Integer typeId = person.getFkIdIdentificationType() != null
                    ? person.getFkIdIdentificationType().getIdIdentificationType()
                    : null;
            return new PersonResponse(
                    person.getPersonId(),
                    person.getFirstName(),
                    person.getLastName(),
                    person.getIdentificationNumber(),
                    person.getIsClient(),
                    person.getNationality(),
                    person.getTaxId(),
                    person.getSex(),
                    person.getBirthDate(),
                    person.getMaritalStatus(),
                    person.getMarriageCount(),
                    person.getOccupation(),
                    person.getAddress(),
                    person.getPhone(),
                    person.getEmail(),
                    person.getNotaryRegistrationNumber(),
                    typeId,
                    person.getVersion());
        }
    }

    private final PersonService personService;
    private final IdentificationTypeRepository identificationTypeRepository;

    public PersonController(PersonService personService, IdentificationTypeRepository identificationTypeRepository) {
        this.personService = personService;
        this.identificationTypeRepository = identificationTypeRepository;
    }

    private Integer resolveIdentificationTypeId(PersonRequest request) {
        if (request.identificationTypeId() != null) {
            return request.identificationTypeId();
        }
        if (request.fkIdIdentificationType() != null) {
            return request.fkIdIdentificationType().idIdentificationType();
        }
        return null;
    }

    private void applyRequest(Person person, PersonRequest request, IdentificationType type) {
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setIdentificationNumber(request.identificationNumber());
        person.setIsClient(request.isClient());
        person.setNationality(request.nationality());
        person.setTaxId(request.taxId());
        person.setSex(request.sex());
        person.setBirthDate(request.birthDate());
        person.setMaritalStatus(request.maritalStatus());
        person.setMarriageCount(request.marriageCount());
        person.setOccupation(request.occupation());
        person.setAddress(request.address());
        person.setPhone(request.phone());
        person.setEmail(request.email());
        person.setNotaryRegistrationNumber(request.notaryRegistrationNumber());
        if (type != null) {
            person.setFkIdIdentificationType(type);
        }
    }

    private IdentificationType resolveType(PersonRequest request, IdentificationType fallback) {
        Integer typeId = resolveIdentificationTypeId(request);
        if (typeId != null) {
            return identificationTypeRepository.findById(typeId).orElse(fallback);
        }
        return fallback;
    }

    @GetMapping
    @Operation(summary = "Get people, one page at a time (default 20, sorted by id)")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PersonResponse>> getAllPeople(
            @PageableDefault(size = 20, sort = "idPerson") Pageable pageable) {
        return ResponseEntity.ok(personService.findAll(pageable).map(PersonResponse::from));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<PersonResponse> getPersonById(@PathVariable Integer id) {
        return personService.findById(id)
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
        try {
            IdentificationType defaultType = identificationTypeRepository.findById(1)
                    .orElseGet(() -> {
                        IdentificationType ti = new IdentificationType();
                        ti.setName("DNI");
                        return identificationTypeRepository.save(ti);
                    });
            IdentificationType type = resolveType(request, defaultType);
            Person person = new Person();
            applyRequest(person, request, type);
            Person saved = personService.save(person);
            return ResponseEntity.status(HttpStatus.CREATED).body(PersonResponse.from(saved));
        } catch (DuplicatePersonException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateBody(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update person")
    public ResponseEntity<Object> updatePerson(@PathVariable Integer id, @Valid @RequestBody PersonRequest request) {
        return personService.findById(id)
                .map(existing -> {
                    IdentificationType type = resolveType(request, existing.getFkIdIdentificationType());
                    applyRequest(existing, request, type);
                    try {
                        Person updated = personService.save(existing);
                        return ResponseEntity.ok((Object) PersonResponse.from(updated));
                    } catch (DuplicatePersonException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) duplicateBody(e));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> duplicateBody(DuplicatePersonException e) {
        return Map.of("message", e.getMessage(), "existingPersonId", e.getExistingPersonId());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person")
    public ResponseEntity<Object> deletePerson(@PathVariable Integer id) {
        if (personService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            personService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete: person is referenced by other records.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search people by first name, last name, identification number or type (CU61)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PersonResponse>> searchPeople(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String identificationNumber,
            @RequestParam(required = false) Integer idIdentificationType,
            @RequestParam(required = false) Boolean isClient) {

        List<PersonResponse> people = personService.search(firstName, lastName, identificationNumber,
                idIdentificationType, isClient).stream().map(PersonResponse::from).toList();
        return ResponseEntity.ok(people);
    }
}
