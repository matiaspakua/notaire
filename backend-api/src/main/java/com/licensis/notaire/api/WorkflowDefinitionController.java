package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workflow-definition")
@Tag(name = "Workflow Definition", description = "CU70 - Gestión de workflows de estados")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionRepository repository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowTransitionRepository transitionRepository;

    public WorkflowDefinitionController(WorkflowDefinitionRepository repository,
            WorkflowNodeRepository nodeRepository,
            WorkflowTransitionRepository transitionRepository) {
        this.repository = repository;
        this.nodeRepository = nodeRepository;
        this.transitionRepository = transitionRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los workflows")
    public ResponseEntity<List<DtoWorkflowDefinition>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(WorkflowDefinition::toDto).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener workflow por ID")
    public ResponseEntity<DtoWorkflowDefinition> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(wf -> ResponseEntity.ok(wf.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear workflow")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowDefinition dto) {
        try {
            WorkflowDefinition entity = new WorkflowDefinition();
            entity.setNombre(dto.getNombre());
            entity.setDescripcion(dto.getDescripcion());
            entity.setActivo(dto.isActivo());
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.toDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar workflow")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoWorkflowDefinition dto) {
        Optional<WorkflowDefinition> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowDefinition entity = existing.get();
            entity.setNombre(dto.getNombre());
            entity.setDescripcion(dto.getDescripcion());
            entity.setActivo(dto.isActivo());
            if (dto.getVersion() != null) {
                entity.setVersion(dto.getVersion());
            }
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar workflow")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!nodeRepository.findByWorkflowDefinitionId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el workflow tiene nodos asociados."));
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
