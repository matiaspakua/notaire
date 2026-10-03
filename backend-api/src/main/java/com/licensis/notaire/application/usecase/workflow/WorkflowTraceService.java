package com.licensis.notaire.application.usecase.workflow;

import com.licensis.notaire.dto.DtoManagementWorkflowTrace;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace.DtoHistoryEntry;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace.DtoTestimonyMovementEntry;
import com.licensis.notaire.dto.DtoWorkflowDefinition;
import com.licensis.notaire.dto.DtoWorkflowNode;
import com.licensis.notaire.dto.DtoWorkflowTransition;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowTraceService {

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
     * Builds the aggregated workflow trace for a given management.
     * <p>
     * Requires {@code @Transactional(readOnly = true)} because it navigates
     * LAZY associations ({@code DeedManagement.procedureList} and
     * {@code ProcedureType.workflowDefinition}) inside the same persistence context.
     */
    @Transactional(readOnly = true)
    public DtoManagementWorkflowTrace buildTrace(Integer managementId) {
        DeedManagement management = managementRepository.findById(managementId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Management not found with id: " + managementId));

        List<Procedure> procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new IllegalArgumentException(
                    "Management " + managementId + " has no procedures");
        }

        ProcedureType procedureType = procedures.get(0).getFkIdProcedureType();
        if (procedureType == null) {
            throw new IllegalArgumentException(
                    "Procedure for management " + managementId + " has no procedure type");
        }

        WorkflowDefinition workflowDef = procedureType.getWorkflowDefinition();
        if (workflowDef == null) {
            throw new IllegalArgumentException(
                    "ProcedureType " + procedureType.getIdProcedureType()
                            + " has no workflow definition assigned");
        }

        List<WorkflowNode> nodeEntities = workflowNodeRepository
                .findByWorkflowDefinitionId(workflowDef.getId());
        List<WorkflowTransition> transitionEntities = workflowTransitionRepository
                .findByWorkflowDefinitionId(workflowDef.getId());

        List<History> historyEntities = historyRepository
                .findByFkIdManagementIdManagement(managementId);

        Map<Integer, String> nodeStatuses = computeNodeStatuses(nodeEntities, historyEntities);

        DtoManagementWorkflowTrace trace = new DtoManagementWorkflowTrace();
        trace.setManagementId(management.getIdManagement());
        trace.setNumber(management.getNumber());
        trace.setEncabezado(management.getEncabezado());
        trace.setDateStart(management.getDateStart());
        trace.setStatusActual(management.getFkIdManagementStatus() != null
                ? management.getFkIdManagementStatus().getName() : null);

        DtoWorkflowDefinition defDto = new DtoWorkflowDefinition();
        defDto.setId(workflowDef.getId());
        defDto.setName(workflowDef.getName());
        defDto.setDescription(workflowDef.getDescription());
        defDto.setActive(workflowDef.isActive());
        defDto.setVersion(workflowDef.getVersion());
        trace.setWorkflowDefinition(defDto);

        List<DtoWorkflowNode> nodeDtos = new ArrayList<>();
        for (WorkflowNode node : nodeEntities) {
            nodeDtos.add(node.toDto());
        }
        trace.setNodes(nodeDtos);

        List<DtoWorkflowTransition> transitionDtos = new ArrayList<>();
        for (WorkflowTransition transition : transitionEntities) {
            transitionDtos.add(transition.toDto());
        }
        trace.setTransitions(transitionDtos);

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

        trace.setNodeStatuses(nodeStatuses);
        trace.setTestimonyMovements(extractTestimonyMovements(procedures));

        return trace;
    }

    /**
     * Resolves the first procedure with a deed that has a non-empty testimony list,
     * then maps its movements chronologically with derived {@code returnedObserved}.
     */
    static List<DtoTestimonyMovementEntry> extractTestimonyMovements(List<Procedure> procedures) {
        List<DtoTestimonyMovementEntry> empty = List.of();
        if (procedures == null || procedures.isEmpty()) {
            return empty;
        }
        for (Procedure procedure : procedures) {
            Deed deed = procedure.getFkIdDeed();
            if (deed == null) {
                continue;
            }
            List<Testimony> testimonies = deed.getTestimonyList();
            if (testimonies == null || testimonies.isEmpty()) {
                continue;
            }
            Testimony testimony = testimonies.get(0);
            List<TestimonyMovement> movements = testimony.getTestimonyMovementList();
            if (movements == null || movements.isEmpty()) {
                return empty;
            }
            List<TestimonyMovement> sorted = new ArrayList<>(movements);
            sorted.sort(Comparator.comparing(
                    TestimonyMovement::getDateEntry,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            List<DtoTestimonyMovementEntry> entries = new ArrayList<>(sorted.size());
            for (TestimonyMovement movement : sorted) {
                entries.add(toMovementEntry(movement));
            }
            return entries;
        }
        return empty;
    }

    static DtoTestimonyMovementEntry toMovementEntry(TestimonyMovement movement) {
        DtoTestimonyMovementEntry entry = new DtoTestimonyMovementEntry();
        entry.setDateEntry(movement.getDateEntry());
        entry.setDateExit(movement.getDateExit());
        entry.setDateRegistration(movement.getDateRegistration());
        entry.setReturnedObserved(isReturnedObserved(movement));
        return entry;
    }

    /**
     * A movement returned observed when it left the registry without being registered.
     */
    static boolean isReturnedObserved(TestimonyMovement movement) {
        return movement.getDateExit() != null && !movement.getRegistered();
    }

    /**
     * Computes per-node status by matching against the management's history.
     * <p>
     * Rules:
     * <ul>
     *   <li>If the node's status id matches the <strong>latest</strong>
     *       distinct status in history → {@code "in_progress"}</li>
     *   <li>If it matches an earlier distinct status → {@code "completed"}</li>
     *   <li>If it does not appear in history at all → {@code "pending"}</li>
     * </ul>
     */
    public static Map<Integer, String> computeNodeStatuses(
            List<WorkflowNode> nodes, List<History> historyList) {

        List<History> sorted = new ArrayList<>(historyList);
        sorted.sort(Comparator.comparing(History::getDate));

        List<Integer> distinctStatusIds = new ArrayList<>();
        for (History h : sorted) {
            Integer statusId = h.getFkIdManagementStatus().getIdManagementStatus();
            if (!distinctStatusIds.contains(statusId)) {
                distinctStatusIds.add(statusId);
            }
        }

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
