package com.licensis.notaire.adapter.in.web.workflow;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ResponseEntity<DtoWorkflowTransition> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(t -> ResponseEntity.ok(t.toDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida: workflowDefinitionId, originNodeId y "
            + "destinationNodeId son obligatorios"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear transición entre dos nodos")
    public ResponseEntity<Object> create(@RequestBody DtoWorkflowTransition dto) {
        Integer workflowId = RequiredFields.require(dto.getWorkflowDefinitionId(), "workflowDefinitionId");
        Integer originId = RequiredFields.require(dto.getOriginNodeId(), "originNodeId");
        Integer destinationId = RequiredFields.require(dto.getDestinationNodeId(), "destinationNodeId");
        Optional<WorkflowDefinition> wf = workflowRepository.findById(workflowId);
        if (wf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<WorkflowNode> origin = nodeRepository.findById(originId);
        if (origin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<WorkflowNode> destination = nodeRepository.findById(destinationId);
        if (destination.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowTransition transition = new WorkflowTransition();
            transition.setWorkflowDefinition(wf.get());
            transition.setOriginNode(origin.get());
            transition.setDestinationNode(destination.get());
            transition.setCondition(dto.getCondition());
            transition.setDescription(dto.getDescription());
            transition = repository.save(transition);
            return ResponseEntity.status(HttpStatus.CREATED).body(transition.toDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida: originNodeId y destinationNodeId son "
            + "obligatorios"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar transición")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoWorkflowTransition dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Integer originId = RequiredFields.require(dto.getOriginNodeId(), "originNodeId");
        Integer destinationId = RequiredFields.require(dto.getDestinationNodeId(), "destinationNodeId");
        Optional<WorkflowNode> origin = nodeRepository.findById(originId);
        if (origin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<WorkflowNode> destination = nodeRepository.findById(destinationId);
        if (destination.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            WorkflowTransition transition = repository.findById(id).get();
            transition.setOriginNode(origin.get());
            transition.setDestinationNode(destination.get());
            transition.setCondition(dto.getCondition());
            transition.setDescription(dto.getDescription());
            return ResponseEntity.ok(repository.save(transition).toDto());
        } catch (Exception e) {
            // A data constraint answers 400; any other failure keeps 409 (issue #579, slice 3).
            return ErrorResponses.createFailed(e);
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
        return ResponseEntity.noContent().build();
    }
}
