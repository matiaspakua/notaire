package com.licensis.notaire.unit;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.service.WorkflowTraceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        status.setName("Estado " + statusId);
        node.setManagementStatus(status);
        return node;
    }

    private History makeHistory(int id, int statusId, long epochMillis) {
        History history = new History(id, new Date(epochMillis));
        history.setFkIdManagementStatus(new ManagementStatus(statusId));
        return history;
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
}
