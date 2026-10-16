package com.licensis.notaire.unit.management;

import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.application.port.out.management.ManagementStatusLogPort;
import com.licensis.notaire.application.port.out.management.ManagementRepositoryPort;
import com.licensis.notaire.application.port.out.management.StatusRepositoryPort;
import com.licensis.notaire.application.port.out.management.WorkflowLookupPort;
import com.licensis.notaire.application.port.out.management.WorkflowTransitionValidatorPort;
import com.licensis.notaire.application.usecase.management.TransitionManagementUseCaseImpl;
import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementTransitionSnapshot;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TransitionManagementUseCaseImpl using fake domain ports (issue #1850).
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
        var initialStatus = ManagementStatus.restore(1, "Initial", null, 0);
        var intermediateStatus = ManagementStatus.restore(2, "Intermediate", null, 0);

        fakeManagementRepository.put(new ManagementTransitionSnapshot(1, 1, "Initial", 10));
        fakeManagementRepository.registerStatus(initialStatus);
        fakeManagementRepository.registerStatus(intermediateStatus);
        fakeStatusRepository.save(initialStatus);
        fakeStatusRepository.save(intermediateStatus);
        fakeWorkflowLookup.setWorkflowDefinitionId(10);
        fakeTransitionValidator.allowTransition(10, 1, 2);

        TransitionManagementOutput output = useCase.execute(1, "Intermediate");

        assertThat(output.managementId()).isEqualTo(1);
        var updated = fakeManagementRepository.findById(1);
        assertThat(updated).isNotNull();
        assertThat(updated.statusName()).isEqualTo("Intermediate");
        assertThat(fakeStatusLog.wasStatusRegistered(1)).isTrue();
    }

    @Test
    @DisplayName("Invalid transition is rejected with BusinessValidationException")
    void shouldRejectInvalidTransition() {
        var initialStatus = ManagementStatus.restore(1, "Initial", null, 0);
        var unreachableStatus = ManagementStatus.restore(3, "Unreachable", null, 0);

        fakeManagementRepository.put(new ManagementTransitionSnapshot(1, 1, "Initial", 10));
        fakeStatusRepository.save(initialStatus);
        fakeStatusRepository.save(unreachableStatus);
        fakeWorkflowLookup.setWorkflowDefinitionId(10);

        assertThatThrownBy(() -> useCase.execute(1, "Unreachable"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("is not allowed");

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
        var initialStatus = ManagementStatus.restore(1, "Initial", null, 0);

        fakeManagementRepository.put(new ManagementTransitionSnapshot(1, 1, "Initial", 10));
        fakeStatusRepository.save(initialStatus);
        fakeWorkflowLookup.setWorkflowDefinitionId(10);

        assertThatThrownBy(() -> useCase.execute(1, "DoesNotExist"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("is not defined");
    }

    private static class FakeManagementRepository implements ManagementRepositoryPort {
        private final java.util.Map<Integer, ManagementTransitionSnapshot> store = new java.util.HashMap<>();
        private final java.util.Map<Integer, ManagementStatus> statusesById = new java.util.HashMap<>();

        void put(ManagementTransitionSnapshot snapshot) {
            store.put(snapshot.idManagement(), snapshot);
        }

        void registerStatus(ManagementStatus status) {
            statusesById.put(status.id(), status);
        }

        @Override
        public ManagementTransitionSnapshot findById(Integer managementId) {
            return store.get(managementId);
        }

        @Override
        public ManagementTransitionSnapshot saveStatus(Integer managementId, Integer statusId) {
            ManagementTransitionSnapshot current = store.get(managementId);
            ManagementStatus status = statusesById.get(statusId);
            String name = status != null ? status.name() : null;
            ManagementTransitionSnapshot updated = new ManagementTransitionSnapshot(
                    managementId, statusId, name, current != null ? current.workflowDefinitionId() : null);
            store.put(managementId, updated);
            return updated;
        }
    }

    private static class FakeStatusRepository implements StatusRepositoryPort {
        private final java.util.Map<String, ManagementStatus> store = new java.util.HashMap<>();

        @Override
        public ManagementStatus findByName(String statusName) {
            return store.get(statusName);
        }

        public void save(ManagementStatus status) {
            store.put(status.name(), status);
            // Also register on management repo via shared knowledge in tests — handled in saveStatus via id map
        }
    }

    private static class FakeWorkflowLookup implements WorkflowLookupPort {
        private Integer workflowDefinitionId;

        @Override
        public Integer resolveWorkflowDefinitionId(Integer managementId) {
            if (workflowDefinitionId == null) {
                throw new BusinessValidationException("No workflow defined");
            }
            return workflowDefinitionId;
        }

        public void setWorkflowDefinitionId(Integer workflowDefinitionId) {
            this.workflowDefinitionId = workflowDefinitionId;
        }
    }

    private static class FakeWorkflowTransitionValidator implements WorkflowTransitionValidatorPort {
        private final java.util.List<TransitionKey> allowedTransitions = new java.util.ArrayList<>();

        @Override
        public boolean isTransitionValid(Integer workflowDefinitionId, Integer originStatusId,
                Integer destinationStatusId) {
            return allowedTransitions.stream()
                    .anyMatch(t -> t.matches(workflowDefinitionId, originStatusId, destinationStatusId));
        }

        public void allowTransition(Integer workflowId, Integer originStatusId, Integer destinationStatusId) {
            allowedTransitions.add(new TransitionKey(workflowId, originStatusId, destinationStatusId));
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
        public void registerStatus(Integer managementId, String detail) {
            registeredManagementIds.add(managementId);
        }

        public boolean wasStatusRegistered(Integer managementId) {
            return registeredManagementIds.contains(managementId);
        }
    }
}
