package com.licensis.notaire.unit.management;

import com.licensis.notaire.application.port.in.management.TransitionManagementOutput;
import com.licensis.notaire.application.port.out.management.ManagementBitacoraPort;
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
    private FakeBitacora fakeBitacora;

    @BeforeEach
    void setUp() {
        fakeManagementRepository = new FakeManagementRepository();
        fakeStatusRepository = new FakeStatusRepository();
        fakeWorkflowLookup = new FakeWorkflowLookup();
        fakeTransitionValidator = new FakeWorkflowTransitionValidator();
        fakeBitacora = new FakeBitacora();

        useCase = new TransitionManagementUseCaseImpl(fakeManagementRepository, fakeStatusRepository,
                fakeWorkflowLookup, fakeTransitionValidator, fakeBitacora);
    }

    @Test
    @DisplayName("Transición válida se aplica y retorna management actualizado")
    void shouldApplyValidTransition() {
        // Arrange
        var statusInicial = new ManagementStatus();
        statusInicial.setIdManagementStatus(1);
        statusInicial.setName("Inicial");

        var statusIntermedio = new ManagementStatus();
        statusIntermedio.setIdManagementStatus(2);
        statusIntermedio.setName("Intermedio");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(statusInicial);
        management.setNumber(123);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(statusInicial);
        fakeStatusRepository.save(statusIntermedio);
        fakeWorkflowLookup.setWorkflow(workflow);
        fakeTransitionValidator.allowTransition(workflow, statusInicial, statusIntermedio);

        // Act
        TransitionManagementOutput output = useCase.execute(1, "Intermedio");

        // Assert
        assertThat(output.managementId()).isEqualTo(1);
        var updatedManagement = fakeManagementRepository.findById(1);
        assertThat(updatedManagement).isNotNull();
        assertThat(updatedManagement.getFkIdManagementStatus().getName()).isEqualTo("Intermedio");
        assertThat(fakeBitacora.wasStatusRegistered(1)).isTrue();
    }

    @Test
    @DisplayName("Transición inválida es rechazada con BusinessValidationException")
    void shouldRejectInvalidTransition() {
        // Arrange
        var statusInicial = new ManagementStatus();
        statusInicial.setIdManagementStatus(1);
        statusInicial.setName("Inicial");

        var statusInasequible = new ManagementStatus();
        statusInasequible.setIdManagementStatus(3);
        statusInasequible.setName("Inalcanzable");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(statusInicial);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(statusInicial);
        fakeStatusRepository.save(statusInasequible);
        fakeWorkflowLookup.setWorkflow(workflow);
        // Validator has no allowed transitions

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1, "Inalcanzable"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("no está permitida");

        // Verify bitacora was not called
        assertThat(fakeBitacora.wasStatusRegistered(1)).isFalse();
    }

    @Test
    @DisplayName("Management no encontrado lanza ResourceNotFoundException")
    void shouldThrowResourceNotFoundWhenManagementNotFound() {
        assertThatThrownBy(() -> useCase.execute(999, "Intermedio"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("Status destino no definido lanza BusinessValidationException")
    void shouldThrowBusinessValidationWhenStatusNotDefined() {
        // Arrange
        var statusInicial = new ManagementStatus();
        statusInicial.setIdManagementStatus(1);
        statusInicial.setName("Inicial");

        var management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(statusInicial);

        var workflow = new WorkflowDefinition();
        workflow.setId(1);

        fakeManagementRepository.save(management);
        fakeStatusRepository.save(statusInicial);
        fakeWorkflowLookup.setWorkflow(workflow);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1, "NoExiste"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("no está definido");
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

    private static class FakeBitacora implements ManagementBitacoraPort {
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
