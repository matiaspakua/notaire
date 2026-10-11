package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.application.usecase.management.ManagementStatusLogService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU13"})
@DisplayName("ManagementStatusLogService tests")
@ExtendWith(MockitoExtension.class)
class ManagementStatusLogServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private ManagementStatusLogService statusLogService;

    private DeedManagement management;
    private ManagementStatus initialStatus;

    @BeforeEach
    void setUp() {
        initialStatus = new ManagementStatus(1, "Iniciada");
        management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(initialStatus);
    }

    @Test
    @DisplayName("Should record history entry on management creation")
    void shouldRecordHistoryOnCreate() {
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        History result = statusLogService.registerStatus(management, null);

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagement()).isEqualTo(management);
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(initialStatus);
        assertThat(result.getFkIdManagementStatus()).isEqualTo(initialStatus);
    }

    @Test
    @DisplayName("Should record history entry on valid transition")
    void shouldRecordHistoryOnValidTransition() {
        ManagementStatus newStatus = new ManagementStatus(2, "In procedure");
        management.setFkIdManagementStatus(newStatus);
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        statusLogService.registerStatus(management, null);

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("Should record history entry on archive")
    void shouldRecordHistoryOnArchive() {
        ManagementStatus archivada = new ManagementStatus(3, "Archivada");
        management.setFkIdManagementStatus(archivada);
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        statusLogService.registerStatus(management, "Archived with outstanding debt");

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(archivada);
        assertThat(captor.getValue().getNotes()).isEqualTo("Archived with outstanding debt");
    }

    @Test
    @DisplayName("Should reject recording history when management has not status assigned")
    void shouldRejectRecordingWhenNoStatus() {
        management.setFkIdManagementStatus(null);

        assertThatThrownBy(() -> statusLogService.registerStatus(management, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should return ordered history for a management")
    void shouldReturnOrderedHistory() {
        History h1 = new History(1);
        h1.setDate(new Date(1000));
        History h2 = new History(2);
        h2.setDate(new Date(3000));
        History h3 = new History(3);
        h3.setDate(new Date(2000));

        when(historyRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(h1, h2, h3));

        List<History> history = statusLogService.getHistory(1);

        assertThat(history).containsExactly(h1, h3, h2);
    }
    @Test
    @DisplayName("#1383 Should order same-day entries by idHistory (insertion order)")
    void shouldOrderSameDayEntriesByIdHistory() {
        Date sameDay = new Date(86_400_000L);
        History h1 = new History(1, sameDay);
        History h2 = new History(2, sameDay);
        History h3 = new History(3, sameDay);
        History earlier = new History(9, new Date(1000));

        when(historyRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(h3, h1, earlier, h2));

        List<History> history = statusLogService.getHistory(1);

        assertThat(history).containsExactly(earlier, h1, h2, h3);
    }
}
