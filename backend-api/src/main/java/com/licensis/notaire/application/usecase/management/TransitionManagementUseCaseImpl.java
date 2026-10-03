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
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case orchestration for CU83 — management status transition.
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

    // Without a transaction, managementRepository.findById(...) returns a DeedManagement whose
    // session closes before workflowLookup.resolveWorkflowDefinition(...) touches the lazy
    // procedureList collection, throwing LazyInitializationException (see #1006).
    @Override
    @Transactional
    public TransitionManagementOutput execute(Integer managementId, String statusDestination) {
        DeedManagement management = managementRepository.findById(managementId);
        if (management == null) {
            throw new ResourceNotFoundException("Management not found with ID: " + managementId);
        }

        WorkflowDefinition workflowDefinition = workflowLookup.resolveWorkflowDefinition(management);

        ManagementStatus currentStatus = management.getFkIdManagementStatus();
        ManagementStatus destination = statusRepository.findByName(statusDestination);
        if (destination == null) {
            throw new BusinessValidationException(
                    "Destination status '" + statusDestination + "' is not defined in the system");
        }

        if (!transitionValidator.isTransitionValid(workflowDefinition, currentStatus, destination)) {
            throw new BusinessValidationException("Transition from '" + statusName(currentStatus)
                    + "' to '" + destination.getName() + "' is not allowed");
        }

        management.setFkIdManagementStatus(destination);
        DeedManagement transitioned = managementRepository.save(management);
        bitacora.registerStatus(transitioned, null);
        LOG.info("Management {} transitioned to status '{}'", managementId, destination.getName());

        return new TransitionManagementOutput(transitioned.getIdManagement());
    }

    private static String statusName(ManagementStatus status) {
        return status != null ? status.getName() : "no status";
    }
}
