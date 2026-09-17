package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.out.management.ManagementBitacoraPort;
import com.licensis.notaire.application.port.out.management.ManagementRepositoryPort;
import com.licensis.notaire.application.port.out.management.StatusRepositoryPort;
import com.licensis.notaire.application.port.out.management.WorkflowLookupPort;
import com.licensis.notaire.application.port.out.management.WorkflowTransitionValidatorPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use case orchestration for CU83 - Transición de estado de gestión.
 * Validates and applies status transitions for a management against its workflow definition.
 */
public class TransitionManagementUseCaseImpl implements TransitionManagementUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(TransitionManagementUseCaseImpl.class);

    private final ManagementRepositoryPort managementRepository;
    private final StatusRepositoryPort statusRepository;
    private final WorkflowLookupPort workflowLookup;
    private final WorkflowTransitionValidatorPort transitionValidator;
    private final ManagementBitacoraPort bitacora;

    public TransitionManagementUseCaseImpl(ManagementRepositoryPort managementRepository,
            StatusRepositoryPort statusRepository, WorkflowLookupPort workflowLookup,
            WorkflowTransitionValidatorPort transitionValidator, ManagementBitacoraPort bitacora) {
        this.managementRepository = managementRepository;
        this.statusRepository = statusRepository;
        this.workflowLookup = workflowLookup;
        this.transitionValidator = transitionValidator;
        this.bitacora = bitacora;
    }

    @Override
    public TransitionManagementOutput execute(Integer managementId, String statusDestination) {
        DeedManagement management = managementRepository.findById(managementId);
        if (management == null) {
            throw new ResourceNotFoundException("Gestión no encontrada con ID: " + managementId);
        }

        WorkflowDefinition workflowDefinition = workflowLookup.resolveWorkflowDefinition(management);

        ManagementStatus statusActual = management.getFkIdManagementStatus();
        ManagementStatus destination = statusRepository.findByName(statusDestination);
        if (destination == null) {
            throw new BusinessValidationException(
                    "Estado destino '" + statusDestination + "' no está definido en el sistema");
        }

        if (!transitionValidator.isTransitionValid(workflowDefinition, statusActual, destination)) {
            throw new BusinessValidationException("Transición de '" + nameStatus(statusActual)
                    + "' a '" + destination.getName() + "' no está permitida");
        }

        management.setFkIdManagementStatus(destination);
        DeedManagement transicionada = managementRepository.save(management);
        bitacora.registerStatus(transicionada, null);
        LOG.info("Gestión {} transicionada a estado '{}'", managementId, destination.getName());

        return new TransitionManagementOutput(transicionada.getIdManagement());
    }

    private static String nameStatus(ManagementStatus status) {
        return status != null ? status.getName() : "sin estado";
    }
}
