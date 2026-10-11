package com.licensis.notaire.adapter.in.web.concept;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.application.usecase.concept.ConceptService;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/concepts")
@Tag(name = "Conceptos", description = "API for managing concepts")
public class ConceptController {

    private static final Logger log = LoggerFactory.getLogger(ConceptController.class);

    private final ConceptService service;
    private final BudgetTemplateRepository templateRepository;

    public ConceptController(ConceptService service,
            BudgetTemplateRepository templateRepository) {
        this.service = service;
        this.templateRepository = templateRepository;
    }

    @GetMapping
    @Operation(summary = "Get all concepts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoConcept>> getAllConcepts() {
        List<DtoConcept> result = service.findAll().stream()
                .map(Concept::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @Operation(summary = "Search concepts by name")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoConcept>> searchConcepts(@RequestParam String name) {
        List<DtoConcept> result = service.searchByName(name).stream()
                .map(Concept::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Check whether a concept is in uso")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isConceptInUse(@PathVariable Integer id) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !templateRepository.findByConceptIdConcept(id).isEmpty();
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get concept by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoConcept> getConceptById(@PathVariable Integer id) {
        return service.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Create concept")
    public ResponseEntity<Object> createConcept(@RequestBody DtoConcept dto) {
        try {
            if (dto.getEnabled() == null) {
                dto.setEnabled(true);
            }
            Concept entity = new Concept();
            entity.setAttributes(dto);
            entity = service.create(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            log.warn("Error creating concept: {}", e.getMessage());
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update concept")
    public ResponseEntity<Object> updateConcept(@PathVariable Integer id, @RequestBody DtoConcept dto) {
        try {
            var existing = service.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "This concept is in uso and cannot modificarse. Cree a new concept."));
            }
            dto.setIdConcept(id);
            Concept entity = existing.get();
            if (dto.getEnabled() == null) {
                dto.setEnabled(entity.getEnabled());
            }
            entity.setAttributes(dto);
            service.update(id, entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating concept id {}: {}", id, e.getMessage());
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete concept")
    public ResponseEntity<Object> deleteConcept(@PathVariable Integer id) {
        try {
            if (service.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Not can delete: the concept is siendo utilizado in budget templates."));
            }
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting concept id {}: {}", id, e.getMessage());
            return ErrorResponses.conflict(e);
        }
    }
}
