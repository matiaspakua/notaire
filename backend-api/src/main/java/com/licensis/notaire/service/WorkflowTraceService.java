package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoManagementWorkflowTrace;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace.DtoHistoryEntry;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
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

    private final DeedManagementRepository managementRepository;
    private final HistoryRepository historyRepository;
    private final WorkflowNodeRepository workflowNodeRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;

    public WorkflowTraceService(
            DeedManagementRepository managementRepository,
            HistoryRepository historyRepository,
            WorkflowNodeRepository workflowNodeRepository,
            WorkflowTransitionRepository workflowTransitionRepository) {
        this.managementRepository = managementRepository;
        this.historyRepository = historyRepository;
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
    public DtoManagementWorkflowTrace buildTrace(Integer managementId) {
        // 1. Load gestión
        DeedManagement management = managementRepository.findById(managementId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gestion not found with id: " + managementId));

        // 2. Resolve workflow definition from the first tramite's tipo
        List<Procedure> procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new IllegalArgumentException("Gestion " + managementId + " has no tramites");
        }

        ProcedureType typeProcedure = procedures.get(0).getFkIdProcedureType();
        if (typeProcedure == null) {
            throw new IllegalArgumentException(
                    "Tramite for gestion " + managementId + " has no tipo de tramite");
        }

        WorkflowDefinition workflowDef = typeProcedure.getWorkflowDefinition();
        if (workflowDef == null) {
            throw new IllegalArgumentException(
                    "TipoDeTramite " + typeProcedure.getIdProcedureType()
                            + " has no workflow definition assigned");
        }

        // 3. Fetch nodes and transitions
        List<WorkflowNode> nodeEntities = workflowNodeRepository
                .findByWorkflowDefinitionId(workflowDef.getId());
        List<WorkflowTransition> transitionEntities = workflowTransitionRepository
                .findByWorkflowDefinitionId(workflowDef.getId());

        // 4. Fetch historial for the gestión
        List<History> historyEntities = historyRepository
                .findByFkIdManagementIdManagement(managementId);

        // 5. Compute node statuses
        Map<Integer, String> nodeStatuses = computeNodeStatuses(nodeEntities, historyEntities);

        // 6. Assemble DTO
        DtoManagementWorkflowTrace trace = new DtoManagementWorkflowTrace();
        trace.setManagementId(management.getIdManagement());
        trace.setNumber(management.getNumber());
        trace.setEncabezado(management.getEncabezado());
        trace.setDateStart(management.getDateStart());
        trace.setStatusActual(management.getFkIdManagementStatus() != null
                ? management.getFkIdManagementStatus().getName() : null);

        // Workflow definition DTO
        DtoWorkflowDefinition defDto = new DtoWorkflowDefinition();
        defDto.setId(workflowDef.getId());
        defDto.setName(workflowDef.getName());
        defDto.setDescription(workflowDef.getDescription());
        defDto.setActive(workflowDef.isActive());
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
        List<DtoHistoryEntry> historyDtos = new ArrayList<>();
        for (History h : historyEntities) {
            DtoHistoryEntry entry = new DtoHistoryEntry();
            entry.setIdHistory(h.getIdHistory());
            entry.setStatusManagementId(h.getFkIdManagementStatus().getIdManagementStatus());
            entry.setStatusManagementName(h.getFkIdManagementStatus().getName());
            entry.setDate(h.getDate());
            entry.setNotes(h.getNotes());
            historyDtos.add(entry);
        }
        trace.setHistory(historyDtos);

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
            List<WorkflowNode> nodes, List<History> historyList) {

        // Sort historial by fecha ASC
        List<History> sorted = new ArrayList<>(historyList);
        sorted.sort(Comparator.comparing(History::getDate));

        // Collect distinct estadoGestionIds in order
        List<Integer> distinctStatusIds = new ArrayList<>();
        for (History h : sorted) {
            Integer statusId = h.getFkIdManagementStatus().getIdManagementStatus();
            if (!distinctStatusIds.contains(statusId)) {
                distinctStatusIds.add(statusId);
            }
        }

        // Classify each node
        Map<Integer, String> statuses = new HashMap<>();
        for (WorkflowNode node : nodes) {
            Integer nodeStatusId = node.getManagementStatus().getIdManagementStatus();
            int idx = distinctStatusIds.indexOf(nodeStatusId);
            if (idx == -1) {
                statuses.put(node.getId(), "pending");
            } else if (idx == distinctStatusIds.size() - 1) {
                statuses.put(node.getId(), "in_progress");
            } else {
                statuses.put(node.getId(), "completed");
            }
        }
        return statuses;
    }
}
