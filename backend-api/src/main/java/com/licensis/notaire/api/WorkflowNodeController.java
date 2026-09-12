package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.repository.ManagementStatusRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workflow-node")
@Tag(name = "Workflow Node", description = "CU70/CU71 - Nodos de workflows de estados")
public class WorkflowNodeController {

    private final WorkflowNodeRepository repository;
    private final WorkflowDefinitionRepository workflowRepository;
    private final ManagementStatusRepository statusRepository;
    private final WorkflowTransitionRepository transitionRepository;

    public WorkflowNodeController(WorkflowNodeRepository repository,
            WorkflowDefinitionRepository workflowRepository,
            ManagementStatusRepository statusRepository,
            WorkflowTransitionRepository transitionRepository) {
        this.repository = repository;
        this.workflowRepository = workflowRepository;
        this.statusRepository = statusRepository;
        this.transitionRepository = transitionRepository;
    }

    @GetMapping("/by-workflow/{workflowId}")
    @Operation(summary = "Obtener nodos por workflow")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoWorkflowNode>> getByWorkflow(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(repository.findByWorkflowDefinitionId(workflowId)
                .stream().map(WorkflowNode::toDto).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener nodo por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoWorkflowNode> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(n -> ResponseEntity.ok(n.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nodo en un workflow")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowNode dto) {
        Optional<WorkflowDefinition> wf = workflowRepository.findById(dto.getWorkflowDefinitionId());
        if (wf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<ManagementStatus> status = statusRepository.findById(dto.getStatusManagementId());
        if (status.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowNode node = new WorkflowNode();
            node.setWorkflowDefinition(wf.get());
            node.setManagementStatus(status.get());
            node.setType(WorkflowNodeType.valueOf(dto.getType()));
            node.setPositionX(dto.getPositionX());
            node.setPositionY(dto.getPositionY());
            node = repository.save(node);
            return ResponseEntity.status(HttpStatus.CREATED).body(node.toDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nodo")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoWorkflowNode dto) {
        Optional<WorkflowNode> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowNode node = existing.get();
            if (dto.getType() != null) {
                node.setType(WorkflowNodeType.valueOf(dto.getType()));
            }
            if (dto.getPositionX() != null) {
                node.setPositionX(dto.getPositionX());
            }
            if (dto.getPositionY() != null) {
                node.setPositionY(dto.getPositionY());
            }
            repository.save(node);
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
    @Operation(summary = "Eliminar nodo")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (transitionRepository.existsByOriginNodeIdOrDestinationNodeId(id, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el nodo tiene transiciones asociadas."));
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
