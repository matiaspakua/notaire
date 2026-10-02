package com.licensis.notaire.adapter.in.web.substitution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.licensis.notaire.application.usecase.substitution.SubstitutionService;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.PersonRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suplencia")
@Tag(name = "Suplencia", description = "API para gestionar suplencia")
public class SubstitutionController {

    private static final Logger log = LoggerFactory.getLogger(SubstitutionController.class);

    /**
     * Nested person shape for frontend {@code Suplencia.fkIdSubstitute/fkIdSubstituted}
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

    private final SubstitutionService service;
    private final PersonRepository personRepository;

    public SubstitutionController(SubstitutionService service, PersonRepository personRepository) {
        this.service = service;
        this.personRepository = personRepository;
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

    private PersonRef toPersonRef(Person person) {
        if (person == null || person.getPersonId() == null) {
            return null;
        }
        return new PersonRef(person.getPersonId(), person.getFirstName(), person.getLastName());
    }

    private SubstitutionResponse toResponse(Substitution entity) {
        Person substitute = entity.getFkIdSubstitute();
        Person substituted = entity.getFkIdSubstituted();
        Integer substituteId = substitute != null ? substitute.getPersonId() : null;
        Integer substitutedId = substituted != null ? substituted.getPersonId() : null;
        return new SubstitutionResponse(
                entity.getIdSubstitution(),
                entity.getDateStart(),
                entity.getDateEnd(),
                entity.getNotes(),
                substituteId,
                substitutedId,
                toPersonRef(substitute),
                toPersonRef(substituted),
                entity.getVersion());
    }

    private boolean applyRequest(Substitution entity, SubstitutionRequest request) {
        Integer substituteId = resolveSubstituteId(request);
        Integer substitutedId = resolveSubstitutedId(request);
        if (substituteId == null || substitutedId == null) {
            return false;
        }
        Person substitute = personRepository.findById(substituteId).orElse(null);
        Person substituted = personRepository.findById(substitutedId).orElse(null);
        if (substitute == null || substituted == null) {
            return false;
        }
        entity.setDateStart(request.dateStart());
        entity.setDateEnd(request.dateEnd());
        entity.setNotes(request.notes());
        entity.setFkIdSubstitute(substitute);
        entity.setFkIdSubstituted(substituted);
        return true;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los suplencia")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SubstitutionResponse>> getAll() {
        try {
            return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
        } catch (Exception e) {
            log.error("Failed to get all substitutions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener suplencia por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<SubstitutionResponse> getById(@PathVariable Integer id) {
        try {
            return service.findById(id)
                    .map(this::toResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to get substitution by id {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo suplencia")
    public ResponseEntity<Object> create(@Valid @RequestBody SubstitutionRequest request) {
        try {
            Substitution entity = new Substitution();
            if (!applyRequest(entity, request)) {
                return ResponseEntity.badRequest().build();
            }
            Substitution saved = service.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (Exception e) {
            log.error("Failed to create substitution", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar suplencia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody SubstitutionRequest request) {
        return service.findById(id).map(existing -> {
            try {
                if (!applyRequest(existing, request)) {
                    return ResponseEntity.badRequest().<Void>build();
                }
                service.save(existing);
                return ResponseEntity.ok().<Void>build();
            } catch (Exception e) {
                log.error("Failed to update substitution id {}", id, e);
                return ResponseEntity.internalServerError().<Void>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar suplencia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            if (!service.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            service.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete substitution id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
