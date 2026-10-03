package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU83 — Validates and applies management status transitions against the
 * {@link WorkflowDefinition} of the associated procedure type.
 */
@Service
public class ManagementTransitionService {

    private static final Logger log = LoggerFactory.getLogger(ManagementTransitionService.class);

    private final DeedManagementRepository managementRepository;
    private final ManagementStatusRepository statusRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final ManagementBitacoraService managementBitacoraService;

    public ManagementTransitionService(DeedManagementRepository managementRepository,
            ManagementStatusRepository statusRepository,
            WorkflowTransitionRepository workflowTransitionRepository,
            ManagementBitacoraService managementBitacoraService) {
        this.managementRepository = managementRepository;
        this.statusRepository = statusRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.managementBitacoraService = managementBitacoraService;
    }

    /**
     * Validates that a {@link WorkflowTransition} exists from the management's current
     * status to {@code statusDestination} and, if so, applies it.
     */
    @Transactional
    public DeedManagement transition(Integer idManagement, String statusDestination) {
        DeedManagement management = managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Management not found with ID: " + idManagement));

        WorkflowDefinition workflowDefinition = resolveWorkflowDefinition(management);

        ManagementStatus currentStatus = management.getFkIdManagementStatus();
        ManagementStatus destination = statusRepository.findByName(statusDestination)
                .orElseThrow(() -> new BusinessValidationException(
                        "Destination status '" + statusDestination + "' is not defined in the system"));

        validateTransition(workflowDefinition, currentStatus, destination);

        management.setFkIdManagementStatus(destination);
        DeedManagement transitioned = managementRepository.save(management);
        managementBitacoraService.registerStatus(transitioned, null);
        log.info("Management {} transitioned to status '{}'", idManagement, destination.getName());
        return transitioned;
    }

    private WorkflowDefinition resolveWorkflowDefinition(DeedManagement management) {
        List<Procedure> procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new BusinessValidationException(
                    "Management " + management.getIdManagement() + " has no associated procedures");
        }
        ProcedureType procedureType = procedures.get(0).getFkIdProcedureType();
        WorkflowDefinition workflowDefinition = procedureType != null ? procedureType.getWorkflowDefinition() : null;
        if (workflowDefinition == null) {
            throw new BusinessValidationException(
                    "Management " + management.getIdManagement() + " has no workflow defined");
        }
        return workflowDefinition;
    }

    private void validateTransition(WorkflowDefinition workflowDefinition, ManagementStatus origin,
            ManagementStatus destination) {
        List<WorkflowTransition> transitions =
                workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinition.getId());
        boolean valid = transitions.stream().anyMatch(transition ->
                statusMatches(transition.getOriginNode(), origin)
                        && statusMatches(transition.getDestinationNode(), destination));
        if (!valid) {
            throw new BusinessValidationException(
                    "Transition from '" + statusName(origin) + "' to '" + destination.getName()
                            + "' is not allowed");
        }
    }

    private static boolean statusMatches(WorkflowNode node, ManagementStatus status) {
        return node != null && node.getManagementStatus() != null && status != null
                && node.getManagementStatus().getIdManagementStatus().equals(status.getIdManagementStatus());
    }

    private static String statusName(ManagementStatus status) {
        return status != null ? status.getName() : "no status";
    }
}
