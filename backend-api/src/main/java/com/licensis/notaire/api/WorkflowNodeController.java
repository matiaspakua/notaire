package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/workflow-node")
@Tag(name = "Workflow Node", description = "CU70/CU71 - Nodos de workflows de estados")
public class WorkflowNodeController {

    private final WorkflowNodeRepository repository;
    private final WorkflowDefinitionRepository workflowRepository;
    private final EstadoDeGestionRepository estadoRepository;
    private final WorkflowTransitionRepository transitionRepository;

    public WorkflowNodeController(WorkflowNodeRepository repository,
            WorkflowDefinitionRepository workflowRepository,
            EstadoDeGestionRepository estadoRepository,
            WorkflowTransitionRepository transitionRepository) {
        this.repository = repository;
        this.workflowRepository = workflowRepository;
        this.estadoRepository = estadoRepository;
        this.transitionRepository = transitionRepository;
    }

    @GetMapping("/by-workflow/{workflowId}")
    @Operation(summary = "Obtener nodos por workflow")
    public ResponseEntity<List<DtoWorkflowNode>> getByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(repository.findByWorkflowDefinitionId(workflowId)
                .stream().map(WorkflowNode::toDto).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener nodo por ID")
    public ResponseEntity<DtoWorkflowNode> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(n -> ResponseEntity.ok(n.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nodo en un workflow")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowNode dto) {
        Optional<WorkflowDefinition> wf = workflowRepository.findById(dto.getWorkflowDefinitionId());
        if (wf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<EstadoDeGestion> estado = estadoRepository.findById(dto.getEstadoGestionId());
        if (estado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowNode node = new WorkflowNode();
            node.setWorkflowDefinition(wf.get());
            node.setEstadoDeGestion(estado.get());
            node.setTipo(WorkflowNodeType.valueOf(dto.getTipo()));
            node.setPosicionX(dto.getPosicionX());
            node.setPosicionY(dto.getPosicionY());
            node = repository.save(node);
            return ResponseEntity.status(HttpStatus.CREATED).body(node.toDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nodo")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoWorkflowNode dto) {
        Optional<WorkflowNode> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowNode node = existing.get();
            if (dto.getTipo() != null) {
                node.setTipo(WorkflowNodeType.valueOf(dto.getTipo()));
            }
            if (dto.getPosicionX() != null) {
                node.setPosicionX(dto.getPosicionX());
            }
            if (dto.getPosicionY() != null) {
                node.setPosicionY(dto.getPosicionY());
            }
            repository.save(node);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nodo")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (transitionRepository.existsByNodoOrigenIdOrNodoDestinoId(id, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el nodo tiene transiciones asociadas."));
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
