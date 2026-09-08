package com.licensis.notaire.api;

import com.licensis.notaire.exception.DuplicatePersonException;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/people")
@Tag(name = "People", description = "API to manage people")
public class PersonController {

    private final PersonService personService;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    public PersonController(PersonService personService, TipoIdentificacionRepository tipoIdentificacionRepository) {
        this.personService = personService;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
    }

    @GetMapping
    @Operation(summary = "Get all people")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Person>> getAllPeople() {
        List<Person> people = personService.findAll();
        return ResponseEntity.ok(people);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Person> getPersonById(@PathVariable Integer id) {
        return personService.findById(id)
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
    public ResponseEntity<Object> createPerson(@Valid @RequestBody Person person) {
        try {
            if (person.getFkIdIdentificationType() == null) {
                TipoIdentificacion defaultTipo = tipoIdentificacionRepository.findById(1)
                        .orElseGet(() -> {
                            TipoIdentificacion ti = new TipoIdentificacion();
                            ti.setNombre("DNI");
                            return tipoIdentificacionRepository.save(ti);
                        });
                person.setFkIdIdentificationType(defaultTipo);
            }
            Person saved = personService.save(person);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
    public ResponseEntity<Object> updatePerson(@PathVariable Integer id, @Valid @RequestBody Person person) {
        return personService.findById(id)
                .map(existing -> {
                    person.setPersonId(id);
                    person.setVersion(existing.getVersion());
                    if (person.getFkIdIdentificationType() == null) {
                        person.setFkIdIdentificationType(existing.getFkIdIdentificationType());
                    }
                    try {
                        Person updated = personService.save(person);
                        return ResponseEntity.ok((Object) updated);
                    } catch (DuplicatePersonException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) duplicateBody(e));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body((Object) e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> duplicateBody(DuplicatePersonException e) {
        return Map.of("message", e.getMessage(), "existingPersonId", e.getIdPersonaExistente());
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
            // Person is still referenced (usuario, folio, presupuesto, …):
            // surface a 409 instead of a raw 500.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete: person is referenced by other records.");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search people by first name, last name, identification number or type (CU61)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Person>> searchPeople(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String identificationNumber,
            @RequestParam(required = false) Integer idTipoIdentificacion,
            @RequestParam(required = false) Boolean isClient) {

        List<Person> people = personService.search(firstName, lastName, identificationNumber,
                idTipoIdentificacion, isClient);
        return ResponseEntity.ok(people);
    }
}
