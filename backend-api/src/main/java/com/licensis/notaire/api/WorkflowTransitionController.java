package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowTransition;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workflow-transition")
@Tag(name = "Workflow Transition", description = "CU71 - Transiciones entre estados de un workflow")
public class WorkflowTransitionController {

    private final WorkflowTransitionRepository repository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowDefinitionRepository workflowRepository;

    public WorkflowTransitionController(WorkflowTransitionRepository repository,
            WorkflowNodeRepository nodeRepository,
            WorkflowDefinitionRepository workflowRepository) {
        this.repository = repository;
        this.nodeRepository = nodeRepository;
        this.workflowRepository = workflowRepository;
    }

    @GetMapping("/by-workflow/{workflowId}")
    @Operation(summary = "Obtener transiciones por workflow")
    public ResponseEntity<List<DtoWorkflowTransition>> getByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(repository.findByWorkflowDefinitionId(workflowId)
                .stream().map(WorkflowTransition::toDto).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener transición por ID")
    public ResponseEntity<DtoWorkflowTransition> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(t -> ResponseEntity.ok(t.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear transición entre dos nodos")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowTransition dto) {
        Optional<WorkflowDefinition> wf = workflowRepository.findById(dto.getWorkflowDefinitionId());
        if (wf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<WorkflowNode> origen = nodeRepository.findById(dto.getNodoOrigenId());
        if (origen.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<WorkflowNode> destino = nodeRepository.findById(dto.getNodoDestinoId());
        if (destino.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(wf.get());
            transition.setNodoOrigen(origen.get());
            transition.setNodoDestino(destino.get());
            transition.setCondicion(dto.getCondicion());
            transition.setDescripcion(dto.getDescripcion());
            transition = repository.save(transition);
            return ResponseEntity.status(HttpStatus.CREATED).body(transition.toDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar transición")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
