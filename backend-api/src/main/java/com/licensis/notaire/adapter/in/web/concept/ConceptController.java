package com.licensis.notaire.adapter.in.web.concept;

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
@RequestMapping("/api/v1/conceptos")
@Tag(name = "Conceptos", description = "API para gestionar conceptos")
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
    @Operation(summary = "Obtener todos los conceptos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoConcept>> getAllConcepts() {
        List<DtoConcept> result = service.findAll().stream()
                .map(Concept::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar conceptos por nombre")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoConcept>> searchConcepts(@RequestParam String name) {
        List<DtoConcept> result = service.searchByName(name).stream()
                .map(Concept::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si un concepto está en uso")
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
    @Operation(summary = "Obtener concepto por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoConcept> getConceptById(@PathVariable Integer id) {
        return service.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo concepto")
    public ResponseEntity<Object> createConcept(@RequestBody DtoConcept dto) {
        try {
            if (dto.getEnabled() == null) {
                dto.setEnabled(true);
            }
            Concept entity = new Concept();
            entity.setAtributos(dto);
            entity = service.create(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            log.warn("Error creating concept: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar concepto")
    public ResponseEntity<Object> updateConcept(@PathVariable Integer id, @RequestBody DtoConcept dto) {
        try {
            var existing = service.findById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Este concepto está en uso y no puede modificarse. Cree un nuevo concepto."));
            }
            dto.setIdConcept(id);
            Concept entity = existing.get();
            if (dto.getEnabled() == null) {
                dto.setEnabled(entity.getEnabled());
            }
            entity.setAtributos(dto);
            service.update(id, entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating concept id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar concepto")
    public ResponseEntity<Object> deleteConcept(@PathVariable Integer id) {
        try {
            if (service.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (!templateRepository.findByConceptIdConcept(id).isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "No se puede eliminar: el concepto está siendo utilizado en plantillas de presupuesto."));
            }
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting concept id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
