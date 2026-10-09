package com.licensis.notaire.adapter.in.web.copy;

import com.licensis.notaire.application.usecase.copy.CopyService;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/copia")
@Tag(name = "Copia", description = "API para gestionar copias")
public class CopyController {

    private static final Logger log = LoggerFactory.getLogger(CopyController.class);

    record CopyRequest(
            Integer number,
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId) {}

    record CopyResponse(
            Integer idCopy,
            int number,
            Date datePrinting,
            Date dateWithdrawal,
            String notes,
            Integer personId,
            Integer testimonyId,
            int version) {}

    private final CopyService service;
    private final PersonRepository personRepository;
    private final TestimonyRepository testimonyRepository;

    public CopyController(CopyService service, PersonRepository personRepository,
            TestimonyRepository testimonyRepository) {
        this.service = service;
        this.personRepository = personRepository;
        this.testimonyRepository = testimonyRepository;
    }

    private CopyResponse toResponse(Copy copy) {
        Integer personId = copy.getFkIdPerson() != null ? copy.getFkIdPerson().getPersonId() : null;
        Integer testimonyId = copy.getFkIdTestimony() != null ? copy.getFkIdTestimony().getIdTestimony() : null;
        return new CopyResponse(
                copy.getIdCopy(),
                copy.getNumber(),
                copy.getDatePrinting(),
                copy.getDateWithdrawal(),
                copy.getNotes(),
                personId,
                testimonyId,
                copy.getVersion());
    }

    private void applyRequest(Copy copy, CopyRequest request) {
        if (request.number() != null) {
            copy.setNumber(request.number());
        }
        copy.setDatePrinting(request.datePrinting());
        copy.setDateWithdrawal(request.dateWithdrawal());
        copy.setNotes(request.notes());
        if (request.personId() != null) {
            Person person = personRepository.findById(request.personId()).orElse(null);
            copy.setFkIdPerson(person);
        } else {
            copy.setFkIdPerson(null);
        }
        if (request.testimonyId() != null) {
            Testimony testimony = testimonyRepository.findById(request.testimonyId()).orElse(null);
            copy.setFkIdTestimony(testimony);
        } else {
            copy.setFkIdTestimony(null);
        }
    }

    @GetMapping
    @Operation(summary = "Obtener todas las copias")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CopyResponse>> getAll() {
        return ResponseEntity.ok(service.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener copia por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<CopyResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva copia")
    public ResponseEntity<Object> create(@Valid @RequestBody CopyRequest request) {
        if (request.testimonyId() != null
                && !service.canCreateCopyForTestimony(request.testimonyId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El testimonio ya tiene un movimiento inscripto y no admite nuevas copias."));
        }
        try {
            Copy entity = new Copy();
            applyRequest(entity, request);
            entity = service.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create copia", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar copia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody CopyRequest request) {
        return service.findById(id).map(existing -> {
            try {
                applyRequest(existing, request);
                service.save(existing);
                return ResponseEntity.ok().<Void>build();
            } catch (Exception e) {
                log.error("Failed to update copia id {}", id, e);
                return ResponseEntity.internalServerError().<Void>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar copia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete copia id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
