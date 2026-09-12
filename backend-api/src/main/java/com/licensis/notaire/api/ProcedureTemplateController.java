package com.licensis.notaire.api;

import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/plantilla-tramite")
@Tag(name = "PlantillaTramite", description = "API para plantillas de tramite (documentos por tipo de tramite)")
public class ProcedureTemplateController {

    private final ProcedureTemplateRepository repository;

    public ProcedureTemplateController(ProcedureTemplateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProcedureTemplate>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/tipo-tramite/{idProcedureType}")
    @Operation(summary = "Obtener plantillas de tramite por tipo de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProcedureTemplate>> getByTypeProcedure(@PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(repository.findByProcedureTypeIdProcedureType(idProcedureType));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Obtener plantilla por clave compuesta (CU55)")
    @Transactional(readOnly = true)
    public ResponseEntity<ProcedureTemplate> getById(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType) {
        return repository.findById(new ProcedureTemplatePK(idProcedureType, idDocumentType))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PostMapping
    @Operation(summary = "Crear plantilla de tramite (CU55)")
    public ResponseEntity<ProcedureTemplate> create(@RequestBody ProcedureTemplate entity) {
        ProcedureTemplate saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Actualizar plantilla de tramite (CU55)")
    public ResponseEntity<ProcedureTemplate> update(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType,
            @RequestBody ProcedureTemplate entity) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(idProcedureType, idDocumentType);
        if (!repository.existsById(pk)) {
            return ResponseEntity.notFound().build();
        }
        entity.setProcedureTemplatePK(pk);
        return ResponseEntity.ok(repository.save(entity));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Eliminar plantilla de tramite (CU55)")
    public ResponseEntity<Void> delete(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(idProcedureType, idDocumentType);
        if (!repository.existsById(pk)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(pk);
        return ResponseEntity.noContent().build();
    }
}
