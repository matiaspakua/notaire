package com.licensis.notaire.adapter.in.web.substitution;

import com.licensis.notaire.application.usecase.substitution.SubstitutionService;
import com.licensis.notaire.business.Substitution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/suplencia")
@Tag(name = "Suplencia", description = "API para gestionar suplencia")
public class SubstitutionController {

    private static final Logger log = LoggerFactory.getLogger(SubstitutionController.class);

    private final SubstitutionService service;

    public SubstitutionController(SubstitutionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los suplencia")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Substitution>> getAll() {
        try {
            return ResponseEntity.ok(service.findAll());
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
    public ResponseEntity<Substitution> getById(@PathVariable Integer id) {
        try {
            return service.findById(id)
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
    public ResponseEntity<Object> create(@RequestBody Substitution entity) {
        try {
            Substitution saved = service.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Substitution entity) {
        try {
            if (!service.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            entity.setIdSubstitution(id);
            service.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update substitution id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
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
