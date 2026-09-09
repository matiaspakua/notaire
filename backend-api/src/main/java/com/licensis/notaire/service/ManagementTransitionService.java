package com.licensis.notaire.service;

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
 * CU83 - Valida y aplica transiciones de estado de una gestión contra el
 * {@link WorkflowDefinition} del tipo de trámite asociado.
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
     * Valida que exista una {@link WorkflowTransition} desde el estado actual de la
     * gestión hacia {@code estadoDestino} y, de ser así, la aplica.
     */
    @Transactional
    public DeedManagement transicionar(Integer idManagement, String statusDestination) {
        DeedManagement management = managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idManagement));

        WorkflowDefinition workflowDefinition = resolveWorkflowDefinition(management);

        ManagementStatus statusActual = management.getFkIdManagementStatus();
        ManagementStatus destination = statusRepository.findByName(statusDestination)
                .orElseThrow(() -> new BusinessValidationException(
                        "Estado destino '" + statusDestination + "' no está definido en el sistema"));

        validarTransicion(workflowDefinition, statusActual, destination);

        management.setFkIdManagementStatus(destination);
        DeedManagement managementTransicionada = managementRepository.save(management);
        managementBitacoraService.registrarStatus(managementTransicionada, null);
        log.info("Gestión {} transicionada a estado '{}'", idManagement, destination.getName());
        return managementTransicionada;
    }

    private WorkflowDefinition resolveWorkflowDefinition(DeedManagement management) {
        List<Procedure> procedures = management.getProcedureList();
        if (procedures == null || procedures.isEmpty()) {
            throw new BusinessValidationException(
                    "La gestión " + management.getIdManagement() + " no tiene trámites asociados");
        }
        ProcedureType typeProcedure = procedures.get(0).getFkIdProcedureType();
        WorkflowDefinition workflowDefinition = typeProcedure != null ? typeProcedure.getWorkflowDefinition() : null;
        if (workflowDefinition == null) {
            throw new BusinessValidationException(
                    "La gestión " + management.getIdManagement() + " no tiene un workflow definido");
        }
        return workflowDefinition;
    }

    private void validarTransicion(WorkflowDefinition workflowDefinition, ManagementStatus origin,
            ManagementStatus destination) {
        List<WorkflowTransition> transiciones =
                workflowTransitionRepository.findByWorkflowDefinitionId(workflowDefinition.getId());
        boolean esValida = transiciones.stream().anyMatch(transicion ->
                coincideStatus(transicion.getOriginNode(), origin) && coincideStatus(transicion.getDestinationNode(), destination));
        if (!esValida) {
            throw new BusinessValidationException(
                    "Transición de '" + nameStatus(origin) + "' a '" + destination.getName() + "' no está permitida");
        }
    }

    private static boolean coincideStatus(WorkflowNode node, ManagementStatus status) {
        return node != null && node.getManagementStatus() != null && status != null
                && node.getManagementStatus().getIdManagementStatus().equals(status.getIdManagementStatus());
    }

    private static String nameStatus(ManagementStatus status) {
        return status != null ? status.getName() : "sin estado";
    }
}
