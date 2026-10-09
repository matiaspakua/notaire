package com.licensis.notaire.adapter.in.web.procedure;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureTypeRepositoryPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureRepositoryPort;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.business.WorkflowDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tipo-tramite")
@Tag(name = "Tipo de Tramite", description = "API para tipos de tramite")
public class ProcedureTypeController {

    private final ProcedureTypeRepositoryPort repository;
    private final BudgetTemplateRepository budgetTemplateRepository;
    private final ProcedureRepositoryPort procedureRepository;
    private final ProcedureTemplateRepositoryPort procedureTemplateRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public ProcedureTypeController(ProcedureTypeRepositoryPort repository,
                                   BudgetTemplateRepository budgetTemplateRepository,
                                   ProcedureRepositoryPort procedureRepository,
                                   ProcedureTemplateRepositoryPort procedureTemplateRepository,
                                   WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.repository = repository;
        this.budgetTemplateRepository = budgetTemplateRepository;
        this.procedureRepository = procedureRepository;
        this.procedureTemplateRepository = procedureTemplateRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Obtener todos los tipos de tramite")
    public ResponseEntity<List<DtoProcedureType>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream()
                .map(ProcedureType::getDto)
                .toList());
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Buscar tipos de tramite por nombre")
    public ResponseEntity<List<DtoProcedureType>> search(@RequestParam String name) {
        return ResponseEntity.ok(repository.findByNameContaining(name).stream()
                .map(ProcedureType::getDto)
                .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtener tipo de tramite por ID")
    public ResponseEntity<DtoProcedureType> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si el tipo de tramite está en uso")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !procedureTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !budgetTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !procedureRepository.findByFkIdProcedureTypeIdProcedureType(id).isEmpty();
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear tipo de tramite")
    public ResponseEntity<Object> create(@RequestBody DtoProcedureType dto) {
        try {
            dto.setEnabled(true);
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            if (dto.getAssociatesProperties() == null) {
                dto.setAssociatesProperties(false);
            }
            ProcedureType entity = new ProcedureType();
            entity.setAtributos(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "version no es la almacenada: otro usuario modificó el registro")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de tramite")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoProcedureType dto) {
        Optional<ProcedureType> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!procedureTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !budgetTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !procedureRepository.findByFkIdProcedureTypeIdProcedureType(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este tipo de trámite está en uso y no puede modificarse. Cree uno nuevo."));
        }
        try {
            dto.setIdProcedureType(id);
            ProcedureType entity = existing.get();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de tramite")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!procedureTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !budgetTemplateRepository.findByProcedureTypeIdProcedureType(id).isEmpty()
                || !procedureRepository.findByFkIdProcedureTypeIdProcedureType(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el tipo de trámite está siendo utilizado en plantillas o trámites."));
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Documented shape of the workflow assignment body. The handler reads a map so that a missing
     * {@code workflowDefinitionId} (400) can be told apart from an explicit {@code null}
     * (unassign); issue #655, Owner decision Oct 9.
     */
    @Schema(name = "WorkflowAssignmentRequest")
    public record WorkflowAssignmentRequest(
            @Schema(description = "Workflow activo a asignar, o null para desasignar el actual",
                    requiredMode = Schema.RequiredMode.REQUIRED, nullable = true)
            Integer workflowDefinitionId) {
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400",
            description = "Cuerpo vacío o inválido: workflowDefinitionId es obligatorio (entero, o null para desasignar)"),
    @ApiResponse(responseCode = "404", description = "Tipo de trámite o workflow no encontrado"),
    @ApiResponse(responseCode = "409", description = "El workflow seleccionado no está activo")
})
    @PutMapping("/{id}/workflow")
    @Operation(summary = "Asignar o desasignar workflow a tipo de tramite")
    public ResponseEntity<Object> assignWorkflow(@PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WorkflowAssignmentRequest.class)))
            @RequestBody Map<String, Object> body) {
        RequiredFields.check().present(body.containsKey("workflowDefinitionId"), "workflowDefinitionId").orThrow();
        Object wfIdObj = body.get("workflowDefinitionId");
        if (wfIdObj != null && !(wfIdObj instanceof Integer)) {
            throw new BusinessValidationException("workflowDefinitionId: debe ser un número entero o null");
        }
        Optional<ProcedureType> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ProcedureType type = existing.get();
        if (wfIdObj == null) {
            type.setWorkflowDefinition(null);
            repository.save(type);
            return ResponseEntity.ok(type.getDto());
        }
        Integer wfId = (Integer) wfIdObj;
        Optional<WorkflowDefinition> wf = workflowDefinitionRepository.findById(wfId);
        if (wf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!wf.get().isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El workflow seleccionado no está activo."));
        }
        type.setWorkflowDefinition(wf.get());
        repository.save(type);
        return ResponseEntity.ok(type.getDto());
    }
}
