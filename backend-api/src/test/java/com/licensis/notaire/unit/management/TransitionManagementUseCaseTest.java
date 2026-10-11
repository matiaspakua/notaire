package com.licensis.notaire.unit.management;

import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.application.port.out.management.ManagementStatusLogPort;
import com.licensis.notaire.application.port.out.management.ManagementRepositoryPort;
import com.licensis.notaire.application.port.out.management.StatusRepositoryPort;
import com.licensis.notaire.application.port.out.management.WorkflowLookupPort;
import com.licensis.notaire.application.port.out.management.WorkflowTransitionValidatorPort;
import com.licensis.notaire.application.usecase.management.TransitionManagementUseCaseImpl;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TransitionManagementUseCaseImpl using fake ports.
 */
@DisplayName("TransitionManagementUseCase — validates workflow transitions (CU83)")
class TransitionManagementUseCaseTest {

    private TransitionManagementUseCaseImpl useCase;
    private FakeManagementRepository fakeManagementRepository;
    private FakeStatusRepository fakeStatusRepository;
    private FakeWorkflowLookup fakeWorkflowLookup;
    private FakeWorkflowTransitionValidator fakeTransitionValidator;
    private FakeStatusLog fakeStatusLog;

    @BeforeEach
    void setUp() {
        fakeManagementRepository = new FakeManagementRepository();
        fakeStatusRepository = new FakeStatusRepository();
        fakeWorkflowLookup = new FakeWorkflowLookup();
        fakeTransitionValidator = new FakeWorkflowTransitionValidator();
        fakeStatusLog = new FakeStatusLog();

        useCase = new TransitionManagementUseCaseImpl(fakeManagementRepository, fakeStatusRepository,
                fakeWorkflowLookup, fakeTransitionValidator, fakeStatusLog);
    }

    @Test
    @DisplayName("Valid transition is applied and returns updated management")
    void shouldApplyValidTransition() {
        // Arrange
        var initialStatus = new ManagementStatus();
        initialStatus.setIdManagementStatus(1);
        initialStatus.setName("Initial");

        var intermediateStatus = new ManagementStatus();
        intermediateStatus.setIdManagementStatus(2);
        intermediateStatus.setName("Intermediate");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(initialStatus);
        management.setNumber(123);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(initialStatus);
        fakeStatusRepository.save(intermediateStatus);
        fakeWorkflowLookup.setWorkflow(workflow);
        fakeTransitionValidator.allowTransition(workflow, initialStatus, intermediateStatus);

        // Act
        TransitionManagementOutput output = useCase.execute(1, "Intermediate");

        // Assert
        assertThat(output.managementId()).isEqualTo(1);
        var updatedManagement = fakeManagementRepository.findById(1);
        assertThat(updatedManagement).isNotNull();
        assertThat(updatedManagement.getFkIdManagementStatus().getName()).isEqualTo("Intermediate");
        assertThat(fakeStatusLog.wasStatusRegistered(1)).isTrue();
    }

    @Test
    @DisplayName("Invalid transition is rejected with BusinessValidationException")
    void shouldRejectInvalidTransition() {
        // Arrange
        var initialStatus = new ManagementStatus();
        initialStatus.setIdManagementStatus(1);
        initialStatus.setName("Initial");

        var unreachableStatus = new ManagementStatus();
        unreachableStatus.setIdManagementStatus(3);
        unreachableStatus.setName("Unreachable");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(initialStatus);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(initialStatus);
        fakeStatusRepository.save(unreachableStatus);
        fakeWorkflowLookup.setWorkflow(workflow);
        // Validator has no allowed transitions

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1, "Unreachable"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("is not allowed");

        // Verify the status log was not called
        assertThat(fakeStatusLog.wasStatusRegistered(1)).isFalse();
    }

    @Test
    @DisplayName("Missing management throws ResourceNotFoundException")
    void shouldThrowResourceNotFoundWhenManagementNotFound() {
        assertThatThrownBy(() -> useCase.execute(999, "Intermediate"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Undefined destination status throws BusinessValidationException")
    void shouldThrowBusinessValidationWhenStatusNotDefined() {
        // Arrange
        var initialStatus = new ManagementStatus();
        initialStatus.setIdManagementStatus(1);
        initialStatus.setName("Initial");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(initialStatus);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(initialStatus);
        fakeWorkflowLookup.setWorkflow(workflow);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1, "DoesNotExist"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("is not defined");
    }

    // --- Fake Ports ---

    private static class FakeManagementRepository implements ManagementRepositoryPort {
        private final java.util.Map<Integer, DeedManagement> store = new java.util.HashMap<>();

        @Override
        public DeedManagement findById(Integer managementId) {
            return store.get(managementId);
        }

        @Override
        public DeedManagement save(DeedManagement management) {
            store.put(management.getIdManagement(), management);
            return management;
        }
    }

    private static class FakeStatusRepository implements StatusRepositoryPort {
        private final java.util.Map<String, ManagementStatus> store = new java.util.HashMap<>();

        @Override
        public ManagementStatus findByName(String statusName) {
            return store.get(statusName);
        }

        public void save(ManagementStatus status) {
            store.put(status.getName(), status);
        }
    }

    private static class FakeWorkflowLookup implements WorkflowLookupPort {
        private WorkflowDefinition workflow;

        @Override
        public WorkflowDefinition resolveWorkflowDefinition(DeedManagement management) {
            if (workflow == null) {
                throw new BusinessValidationException("No workflow defined");
            }
            return workflow;
        }

        public void setWorkflow(WorkflowDefinition workflow) {
            this.workflow = workflow;
        }
    }

    private static class FakeWorkflowTransitionValidator
            implements WorkflowTransitionValidatorPort {
        private final java.util.List<TransitionKey> allowedTransitions = new java.util.ArrayList<>();

        @Override
        public boolean isTransitionValid(WorkflowDefinition workflowDefinition, ManagementStatus origin,
                ManagementStatus destination) {
            return allowedTransitions.stream()
                    .anyMatch(t -> t.matches(workflowDefinition.getId(), origin.getIdManagementStatus(),
                            destination.getIdManagementStatus()));
        }

        public void allowTransition(WorkflowDefinition workflow, ManagementStatus origin,
                ManagementStatus destination) {
            allowedTransitions.add(new TransitionKey(workflow.getId(), origin.getIdManagementStatus(),
                    destination.getIdManagementStatus()));
        }

        private record TransitionKey(Integer workflowId, Integer originStatusId, Integer destStatusId) {
            boolean matches(Integer wfId, Integer originId, Integer destId) {
                return workflowId.equals(wfId) && originStatusId.equals(originId)
                        && destStatusId.equals(destId);
            }
        }
    }

    private static class FakeStatusLog implements ManagementStatusLogPort {
        private final java.util.Set<Integer> registeredManagementIds = new java.util.HashSet<>();

        @Override
        public void registerStatus(DeedManagement management, String detail) {
            registeredManagementIds.add(management.getIdManagement());
        }

        public boolean wasStatusRegistered(Integer managementId) {
            return registeredManagementIds.contains(managementId);
        }
    }
}
