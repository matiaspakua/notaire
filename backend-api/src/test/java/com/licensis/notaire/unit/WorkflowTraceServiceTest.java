package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.WorkflowNode;
import com.licensis.notaire.negocio.WorkflowNodeType;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
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

    private WorkflowNode makeNode(int id, int estadoId, WorkflowNodeType tipo) {
        WorkflowNode node = new WorkflowNode(id);
        node.setTipo(tipo);
        EstadoDeGestion estado = new EstadoDeGestion(estadoId);
        estado.setNombre("Estado " + estadoId);
        node.setEstadoDeGestion(estado);
        return node;
    }

    private Historial makeHistorial(int id, int estadoId, long epochMillis) {
        Historial historial = new Historial(id, new Date(epochMillis));
        historial.setFkIdEstadoGestion(new EstadoDeGestion(estadoId));
        return historial;
    }

    @Test
    @DisplayName("Should mark all nodes pending when historial is empty")
    void shouldMarkAllNodesPendingWhenHistorialIsEmpty() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.FINAL));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, List.of());

        assertThat(statuses).containsEntry(1, "pending").containsEntry(2, "pending");
    }

    @Test
    @DisplayName("Should mark latest historial estado in_progress and earlier ones completed")
    void shouldMarkLatestEstadoInProgressAndEarlierCompleted() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.INTERMEDIATE),
                makeNode(3, 30, WorkflowNodeType.FINAL));
        List<Historial> historial = List.of(
                makeHistorial(1, 10, 1000L),
                makeHistorial(2, 20, 2000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, historial);

        assertThat(statuses)
                .containsEntry(1, "completed")
                .containsEntry(2, "in_progress")
                .containsEntry(3, "pending");
    }

    @Test
    @DisplayName("Should order historial by fecha regardless of list order")
    void shouldOrderHistorialByFechaRegardlessOfListOrder() {
        List<WorkflowNode> nodes = List.of(
                makeNode(1, 10, WorkflowNodeType.INITIAL),
                makeNode(2, 20, WorkflowNodeType.FINAL));
        List<Historial> historial = List.of(
                makeHistorial(2, 20, 5000L),
                makeHistorial(1, 10, 1000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, historial);

        assertThat(statuses).containsEntry(1, "completed").containsEntry(2, "in_progress");
    }

    @Test
    @DisplayName("Should keep node in_progress when the same estado repeats in historial")
    void shouldKeepNodeInProgressWhenSameEstadoRepeats() {
        List<WorkflowNode> nodes = List.of(makeNode(1, 10, WorkflowNodeType.INITIAL));
        List<Historial> historial = List.of(
                makeHistorial(1, 10, 1000L),
                makeHistorial(2, 10, 2000L));

        Map<Integer, String> statuses = WorkflowTraceService.computeNodeStatuses(nodes, historial);

        assertThat(statuses).containsEntry(1, "in_progress");
    }

    @Test
    @DisplayName("Should throw when gestion does not exist")
    void shouldThrowWhenGestionDoesNotExist() {
        GestionDeEscrituraRepository gestionRepository = Mockito.mock(GestionDeEscrituraRepository.class);
        Mockito.when(gestionRepository.findById(99)).thenReturn(Optional.empty());
        WorkflowTraceService service = new WorkflowTraceService(
                gestionRepository,
                Mockito.mock(HistorialRepository.class),
                Mockito.mock(WorkflowNodeRepository.class),
                Mockito.mock(WorkflowTransitionRepository.class));

        assertThatThrownBy(() -> service.buildTrace(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
