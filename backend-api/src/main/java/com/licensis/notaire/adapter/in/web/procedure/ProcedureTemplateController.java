package com.licensis.notaire.adapter.in.web.procedure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    record ProcedureTemplatePkRequest(
            @JsonProperty("fkIdProcedureType") Integer fkIdProcedureType,
            @JsonProperty("fkIdDocumentType") Integer fkIdDocumentType) {}

    record ProcedureTemplateRequest(
            String notes,
            Integer fkIdProcedureType,
            Integer fkIdDocumentType,
            @JsonProperty("procedureTemplatePK") ProcedureTemplatePkRequest procedureTemplatePK) {}

    record ProcedureTemplateResponse(
            Integer fkIdProcedureType,
            Integer fkIdDocumentType,
            String notes,
            int version) {}

    private final ProcedureTemplateRepositoryPort repository;

    public ProcedureTemplateController(ProcedureTemplateRepositoryPort repository) {
        this.repository = repository;
    }

    private Integer resolveProcedureTypeId(ProcedureTemplateRequest request) {
        if (request.fkIdProcedureType() != null) {
            return request.fkIdProcedureType();
        }
        return request.procedureTemplatePK() != null
                ? request.procedureTemplatePK().fkIdProcedureType() : null;
    }

    private Integer resolveDocumentTypeId(ProcedureTemplateRequest request) {
        if (request.fkIdDocumentType() != null) {
            return request.fkIdDocumentType();
        }
        return request.procedureTemplatePK() != null
                ? request.procedureTemplatePK().fkIdDocumentType() : null;
    }

    private ProcedureTemplateResponse toResponse(ProcedureTemplate entity) {
        ProcedureTemplatePK pk = entity.getProcedureTemplatePK();
        return new ProcedureTemplateResponse(
                pk != null ? pk.getFkIdProcedureType() : null,
                pk != null ? pk.getFkIdDocumentType() : null,
                entity.getNotes(),
                entity.getVersion());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProcedureTemplateResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/tipo-tramite/{idProcedureType}")
    @Operation(summary = "Obtener plantillas de tramite por tipo de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProcedureTemplateResponse>> getByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(repository.findByProcedureTypeIdProcedureType(idProcedureType)
                .stream().map(this::toResponse).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Obtener plantilla por clave compuesta (CU55)")
    @Transactional(readOnly = true)
    public ResponseEntity<ProcedureTemplateResponse> getById(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType) {
        return repository.findById(new ProcedureTemplatePK(idProcedureType, idDocumentType))
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
    @Operation(summary = "Crear plantilla de tramite (CU55)")
    public ResponseEntity<ProcedureTemplateResponse> create(
            @Valid @RequestBody ProcedureTemplateRequest request) {
        Integer procedureTypeId = resolveProcedureTypeId(request);
        Integer documentTypeId = resolveDocumentTypeId(request);
        if (procedureTypeId == null || documentTypeId == null) {
            return ResponseEntity.badRequest().build();
        }
        ProcedureTemplate entity = new ProcedureTemplate(
                new ProcedureTemplatePK(procedureTypeId, documentTypeId));
        entity.setNotes(request.notes());
        ProcedureTemplate saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{idProcedureType}/{idDocumentType}")
    @Operation(summary = "Actualizar plantilla de tramite (CU55)")
    public ResponseEntity<ProcedureTemplateResponse> update(@PathVariable Integer idProcedureType,
            @PathVariable Integer idDocumentType,
            @Valid @RequestBody ProcedureTemplateRequest request) {
        ProcedureTemplatePK pk = new ProcedureTemplatePK(idProcedureType, idDocumentType);
        return repository.findById(pk).map(existing -> {
            existing.setNotes(request.notes());
            return ResponseEntity.ok(toResponse(repository.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
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
