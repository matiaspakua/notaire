package com.licensis.notaire.unit;

import com.licensis.notaire.application.usecase.workflow.WorkflowTraceService;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace.DtoTestimonyMovementEntry;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CU83 - WorkflowTraceService unit tests")
class WorkflowTraceServiceTest {

    private WorkflowNode makeNode(int id, int statusId, WorkflowNodeType type) {
        WorkflowNode node = new WorkflowNode(id);
        node.setType(type);
        ManagementStatus status = new ManagementStatus(statusId);
        status.setName("Status " + statusId);
        node.setManagementStatus(status);
        return node;
    }

    private History makeHistory(int id, int statusId, long epochMillis) {
        History history = new History(id, new Date(epochMillis));
        history.setFkIdManagementStatus(new ManagementStatus(statusId));
        return history;
    }

    private TestimonyMovement makeMovement(
            int id, long entryEpoch, Long exitEpoch, boolean registered) {
        TestimonyMovement movement = new TestimonyMovement(
                id, new Date(entryEpoch), registered, 1);
        if (exitEpoch != null) {
            movement.setDateExit(new Date(exitEpoch));
        }
        return movement;
    }

    private WorkflowTraceService serviceWithManagement(DeedManagement management) {
        DeedManagementRepository managementRepository = Mockito.mock(DeedManagementRepository.class);
        HistoryRepository historyRepository = Mockito.mock(HistoryRepository.class);
        WorkflowNodeRepository nodeRepository = Mockito.mock(WorkflowNodeRepository.class);
        WorkflowTransitionRepository transitionRepository =
                Mockito.mock(WorkflowTransitionRepository.class);

        Integer managementId = management.getIdManagement();
        WorkflowDefinition definition =
                management.getProcedureList().get(0).getFkIdProcedureType().getWorkflowDefinition();

        Mockito.when(managementRepository.findById(managementId)).thenReturn(Optional.of(management));
        Mockito.when(historyRepository.findByFkIdManagementIdManagement(managementId))
                .thenReturn(List.of());
        Mockito.when(nodeRepository.findByWorkflowDefinitionId(definition.getId()))
                .thenReturn(List.of(makeNode(1, 1, WorkflowNodeType.INITIAL)));
        Mockito.when(transitionRepository.findByWorkflowDefinitionId(definition.getId()))
                .thenReturn(List.of());

        return new WorkflowTraceService(
                managementRepository, historyRepository, nodeRepository, transitionRepository);
    }

    private DeedManagement managementWithMovements(List<TestimonyMovement> movements) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(1);
        definition.setName("Standard");
        definition.setActive(true);

        ProcedureType procedureType = new ProcedureType();
        procedureType.setIdProcedureType(1);
        procedureType.setWorkflowDefinition(definition);

        Testimony testimony = new Testimony(10);
        testimony.setTestimonyMovementList(new ArrayList<>(movements));

        Deed deed = new Deed(20);
        deed.setTestimonyList(List.of(testimony));

        Procedure procedure = new Procedure(30);
        procedure.setFkIdProcedureType(procedureType);
        procedure.setFkIdDeed(deed);

        DeedManagement management = new DeedManagement();
        management.setIdManagement(100);
        management.setNumber(1001);
        management.setEncabezado("Trace with testimony");
        management.setDateStart(new Date(1_000L));
        management.setFkIdManagementStatus(new ManagementStatus(12));
        management.setProcedureList(List.of(procedure));
        return management;
    }

    private DeedManagement managementWithoutTestimony() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(2);
        definition.setName("Standard");
        definition.setActive(true);

        ProcedureType procedureType = new ProcedureType();
        procedureType.setIdProcedureType(2);
        procedureType.setWorkflowDefinition(definition);

        Procedure procedure = new Procedure(40);
        procedure.setFkIdProcedureType(procedureType);

        DeedManagement management = new DeedManagement();
        management.setIdManagement(200);
        management.setNumber(2002);
        management.setEncabezado("Trace without testimony");
        management.setDateStart(new Date(1_000L));
        management.setFkIdManagementStatus(new ManagementStatus(2));
        management.setProcedureList(List.of(procedure));
        return management;
    }

    @Test
    @DisplayName("Should mark all nodes pending when history is empty")
    void shouldMarkAllNodesPendingWhenHistoryIsEmpty() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.FINAL));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, List.of());

        assertThat(statuses).containsEntry(1, "pending").containsEntry(2, "pending");
    }

    @Test
    @DisplayName("Should mark latest history status in_progress and earlier ones completed")
    void shouldMarkLatestStatusInProgressAndEarlierCompleted() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.INTERMEDIATE),
                makeNode(3, 30, WorkflowNodeType.FINAL));
        List<History> history = List.of(
                makeHistory(1, 10, 1000L),
                makeHistory(2, 20, 2000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, history);

        assertThat(statuses)
                .containsEntry(1, "completed")
                .containsEntry(2, "in_progress")
                .containsEntry(3, "pending");
    }

    @Test
    @DisplayName("Should order history by date regardless of list order")
    void shouldOrderHistoryByDateRegardlessOfListOrder() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.FINAL));
        List<History> history = List.of(
                makeHistory(2, 20, 5000L),
                makeHistory(1, 10, 1000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, history);

        assertThat(statuses).containsEntry(1, "completed").containsEntry(2, "in_progress");
    }

    @Test
    @DisplayName("Should keep node in_progress when the same status repeats in history")
    void shouldKeepNodeInProgressWhenSameStatusRepeats() {
        List<WorkflowNode> nodes = List.of(makeNode(1, 10, WorkflowNodeType.INITIAL));
        List<History> history = List.of(
                makeHistory(1, 10, 1000L),
                makeHistory(2, 10, 2000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, history);

        assertThat(statuses).containsEntry(1, "in_progress");
    }

    @Test
    @DisplayName("Should throw when gestion does not exist")
    void shouldThrowWhenManagementDoesNotExist() {
        DeedManagementRepository managementRepository = Mockito.mock(DeedManagementRepository.class);
        Mockito.when(managementRepository.findById(99)).thenReturn(Optional.empty());
        WorkflowTraceService service = new WorkflowTraceService(
                managementRepository,
                Mockito.mock(HistoryRepository.class),
                Mockito.mock(WorkflowNodeRepository.class),
                Mockito.mock(WorkflowTransitionRepository.class));

        assertThatThrownBy(() -> service.buildTrace(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should include testimony movements chronologically with returnedObserved derived")
    void shouldIncludeAllMovementsInChronologicalOrder() {
        TestimonyMovement laterOpen = makeMovement(2, 5_000L, null, false);
        TestimonyMovement earlierReturned = makeMovement(1, 1_000L, 2_000L, false);
        TestimonyMovement registered = makeMovement(3, 3_000L, 4_000L, true);
        DeedManagement management = managementWithMovements(
                List.of(laterOpen, earlierReturned, registered));
        WorkflowTraceService service = serviceWithManagement(management);

        DtoManagementWorkflowTrace trace = service.buildTrace(100);

        assertThat(trace.getTestimonyMovements()).hasSize(3);
        assertThat(trace.getTestimonyMovements())
                .extracting(DtoTestimonyMovementEntry::getDateEntry)
                .containsExactly(new Date(1_000L), new Date(3_000L), new Date(5_000L));
        assertThat(trace.getTestimonyMovements().get(0).isReturnedObserved()).isTrue();
        assertThat(trace.getTestimonyMovements().get(1).isReturnedObserved()).isFalse();
        assertThat(trace.getTestimonyMovements().get(2).isReturnedObserved()).isFalse();
        assertThat(trace.getTestimonyMovements().get(0).getDateExit()).isEqualTo(new Date(2_000L));
        assertThat(trace.getTestimonyMovements().get(1).getDateRegistration()).isNull();
    }

    @Test
    @DisplayName("Should omit testimony movements when management has no testimony")
    void shouldOmitMovementsWhenNoTestimony() {
        DeedManagement management = managementWithoutTestimony();
        WorkflowTraceService service = serviceWithManagement(management);

        DtoManagementWorkflowTrace trace = service.buildTrace(200);

        assertThat(trace.getTestimonyMovements()).isEmpty();
        assertThat(trace.getNodes()).isNotEmpty();
        assertThat(trace.getNodeStatuses()).isNotEmpty();
    }

    @Test
    @DisplayName("Should keep prior movements when a reentry appends a new movement")
    void shouldIncludeMovementsWhenTestimonyHasMovements() {
        TestimonyMovement firstReturned = makeMovement(1, 1_000L, 2_000L, false);
        TestimonyMovement secondReturned = makeMovement(2, 3_000L, 4_000L, false);
        TestimonyMovement reentryOpen = makeMovement(3, 5_000L, null, false);
        DeedManagement management = managementWithMovements(
                List.of(firstReturned, secondReturned, reentryOpen));
        WorkflowTraceService service = serviceWithManagement(management);

        DtoManagementWorkflowTrace trace = service.buildTrace(100);

        assertThat(trace.getTestimonyMovements()).hasSize(3);
        long returnedCount = trace.getTestimonyMovements().stream()
                .filter(DtoTestimonyMovementEntry::isReturnedObserved)
                .count();
        assertThat(returnedCount).isEqualTo(2);
    }
}
