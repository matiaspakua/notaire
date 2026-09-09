package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.service.ManagementBitacoraService;
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
@DisplayName("GestionBitacoraService Tests")
@ExtendWith(MockitoExtension.class)
class ManagementBitacoraServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private ManagementBitacoraService managementBitacoraService;

    private DeedManagement management;
    private ManagementStatus statusInicial;

    @BeforeEach
    void setUp() {
        statusInicial = new ManagementStatus(1, "Iniciada");
        management = new DeedManagement();
        management.setIdManagement(1);
        management.setFkIdManagementStatus(statusInicial);
    }

    @Test
    @DisplayName("Should record history entry on gestión creation")
    void shouldRecordHistoryOnCreate() {
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        History resultado = managementBitacoraService.registrarStatus(management, null);

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagement()).isEqualTo(management);
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(statusInicial);
        assertThat(resultado.getFkIdManagementStatus()).isEqualTo(statusInicial);
    }

    @Test
    @DisplayName("Should record history entry on valid transition")
    void shouldRecordHistoryOnValidTransition() {
        ManagementStatus nuevoStatus = new ManagementStatus(2, "En trámite");
        management.setFkIdManagementStatus(nuevoStatus);
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        managementBitacoraService.registrarStatus(management, null);

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(nuevoStatus);
    }

    @Test
    @DisplayName("Should record history entry on archive")
    void shouldRecordHistoryOnArchive() {
        ManagementStatus archivada = new ManagementStatus(3, "Archivada");
        management.setFkIdManagementStatus(archivada);
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        when(historyRepository.save(any(History.class))).thenAnswer(invocation -> invocation.getArgument(0));

        managementBitacoraService.registrarStatus(management, "Archivado con deuda pendiente");

        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdManagementStatus()).isEqualTo(archivada);
        assertThat(captor.getValue().getNotes()).isEqualTo("Archivado con deuda pendiente");
    }

    @Test
    @DisplayName("Should reject recording history when gestión has no status assigned")
    void shouldRejectRecordingWhenNoStatus() {
        management.setFkIdManagementStatus(null);

        assertThatThrownBy(() -> managementBitacoraService.registrarStatus(management, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should return ordered history for a gestión")
    void shouldReturnOrderedHistory() {
        History h1 = new History(1);
        h1.setDate(new Date(1000));
        History h2 = new History(2);
        h2.setDate(new Date(3000));
        History h3 = new History(3);
        h3.setDate(new Date(2000));

        when(historyRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(h1, h2, h3));

        List<History> history = managementBitacoraService.obtenerHistory(1);

        assertThat(history).containsExactly(h1, h3, h2);
    }
}
