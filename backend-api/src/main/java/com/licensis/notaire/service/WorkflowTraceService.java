package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoGestionWorkflowTrace;
import com.licensis.notaire.dto.DtoGestionWorkflowTrace.DtoHistorialEntry;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.WorkflowDefinition;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowTransition;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowTraceService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTraceService.class);

    private final GestionDeEscrituraRepository gestionRepository;
    private final HistorialRepository historialRepository;
    private final WorkflowNodeRepository workflowNodeRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowTraceService(
            GestionDeEscrituraRepository gestionRepository,
            HistorialRepository historialRepository,
            WorkflowNodeRepository workflowNodeRepository,
            WorkflowTransitionRepository workflowTransitionRepository) {
        this.gestionRepository = gestionRepository;
        this.historialRepository = historialRepository;
        this.workflowNodeRepository = workflowNodeRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    /**
     * Builds the aggregated workflow trace for a given gestión.
     * <p>
     * Requires {@code @Transactional(readOnly = true)} because it navigates
     * LAZY associations ({@code GestionDeEscritura.tramiteList} and
     * {@code TipoDeTramite.workflowDefinition}) inside the same persistence context.
     */
    @Transactional(readOnly = true)
    public DtoGestionWorkflowTrace buildTrace(Integer gestionId) {
        // 1. Load gestión
        GestionDeEscritura gestion = gestionRepository.findById(gestionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gestion not found with id: " + gestionId));

        // 2. Resolve workflow definition from the first tramite's tipo
        List<Tramite> tramites = gestion.getTramiteList();
        if (tramites == null || tramites.isEmpty()) {
            throw new IllegalArgumentException("Gestion " + gestionId + " has no tramites");
        }

        TipoDeTramite tipoTramite = tramites.get(0).getFkIdTipoTramite();
        if (tipoTramite == null) {
            throw new IllegalArgumentException(
                    "Tramite for gestion " + gestionId + " has no tipo de tramite");
        }

        WorkflowDefinition workflowDef = tipoTramite.getWorkflowDefinition();
        if (workflowDef == null) {
            throw new IllegalArgumentException(
                    "TipoDeTramite " + tipoTramite.getIdTipoTramite()
                            + " has no workflow definition assigned");
        }

        // 3. Fetch nodes and transitions
        List<WorkflowNode> nodeEntities = workflowNodeRepository
                .findByWorkflowDefinitionId(workflowDef.getId());
        List<WorkflowTransition> transitionEntities = workflowTransitionRepository
                .findByWorkflowDefinitionId(workflowDef.getId());

        // 4. Fetch historial for the gestión
        List<Historial> historialEntities = historialRepository
                .findByFkIdGestionIdGestion(gestionId);

        // 5. Compute node statuses
        Map<Integer, String> nodeStatuses = computeNodeStatuses(nodeEntities, historialEntities);

        // 6. Assemble DTO
        DtoGestionWorkflowTrace trace = new DtoGestionWorkflowTrace();
        trace.setGestionId(gestion.getIdGestion());
        trace.setNumero(gestion.getNumero());
        trace.setEncabezado(gestion.getEncabezado());
        trace.setFechaInicio(gestion.getFechaInicio());
        trace.setEstadoActual(gestion.getFkIdEstadoDeGestion() != null
                ? gestion.getFkIdEstadoDeGestion().getNombre() : null);

        // Workflow definition DTO
        DtoWorkflowDefinition defDto = new DtoWorkflowDefinition();
        defDto.setId(workflowDef.getId());
        defDto.setNombre(workflowDef.getNombre());
        defDto.setDescripcion(workflowDef.getDescripcion());
        defDto.setActivo(workflowDef.isActivo());
        defDto.setVersion(workflowDef.getVersion());
        trace.setWorkflowDefinition(defDto);

        // Node DTOs
        List<DtoWorkflowNode> nodeDtos = new ArrayList<>();
        for (WorkflowNode node : nodeEntities) {
            nodeDtos.add(node.toDto());
        }
        trace.setNodes(nodeDtos);

        // Transition DTOs
        List<DtoWorkflowTransition> transitionDtos = new ArrayList<>();
        for (WorkflowTransition transition : transitionEntities) {
            transitionDtos.add(transition.toDto());
        }
        trace.setTransitions(transitionDtos);

        // Historial DTOs
        List<DtoHistorialEntry> historialDtos = new ArrayList<>();
        for (Historial h : historialEntities) {
            DtoHistorialEntry entry = new DtoHistorialEntry();
            entry.setIdHistorial(h.getIdHistorial());
            entry.setEstadoGestionId(h.getFkIdEstadoGestion().getIdEstadoGestion());
            entry.setEstadoGestionNombre(h.getFkIdEstadoGestion().getNombre());
            entry.setFecha(h.getFecha());
            entry.setObservaciones(h.getObservaciones());
            historialDtos.add(entry);
        }
        trace.setHistorial(historialDtos);

        // Node statuses
        trace.setNodeStatuses(nodeStatuses);

        return trace;
    }

    /**
     * Computes per-node status by matching against the gestión's historial.
     * <p>
     * Rules:
     * <ul>
     *   <li>If the node's estadoGestionId matches the <strong>latest</strong>
     *       distinct estado in historial → {@code "in_progress"}</li>
     *   <li>If it matches an earlier distinct estado → {@code "completed"}</li>
     *   <li>If it does not appear in historial at all → {@code "pending"}</li>
     * </ul>
     */
    public static Map<Integer, String> computeNodeStatuses(
            List<WorkflowNode> nodes, List<Historial> historialList) {

        // Sort historial by fecha ASC
        List<Historial> sorted = new ArrayList<>(historialList);
        sorted.sort(Comparator.comparing(Historial::getFecha));

        // Collect distinct estadoGestionIds in order
        List<Integer> distinctEstadoIds = new ArrayList<>();
        for (Historial h : sorted) {
            Integer estadoId = h.getFkIdEstadoGestion().getIdEstadoGestion();
            if (!distinctEstadoIds.contains(estadoId)) {
                distinctEstadoIds.add(estadoId);
            }
        }

        // Classify each node
        Map<Integer, String> statuses = new HashMap<>();
        for (WorkflowNode node : nodes) {
            Integer nodeEstadoId = node.getEstadoDeGestion().getIdEstadoGestion();
            int idx = distinctEstadoIds.indexOf(nodeEstadoId);
            if (idx == -1) {
                statuses.put(node.getId(), "pending");
            } else if (idx == distinctEstadoIds.size() - 1) {
                statuses.put(node.getId(), "in_progress");
            } else {
                statuses.put(node.getId(), "completed");
            }
        }
        return statuses;
    }
}
