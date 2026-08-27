package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.service.GestionBitacoraService;
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
class GestionBitacoraServiceTest {

    @Mock
    private HistorialRepository historialRepository;

    @InjectMocks
    private GestionBitacoraService gestionBitacoraService;

    private GestionDeEscritura gestion;
    private EstadoDeGestion estadoInicial;

    @BeforeEach
    void setUp() {
        estadoInicial = new EstadoDeGestion(1, "Iniciada");
        gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        gestion.setFkIdEstadoDeGestion(estadoInicial);
    }

    @Test
    @DisplayName("Should record historial entry on gestión creation")
    void shouldRecordHistorialOnCreate() {
        ArgumentCaptor<Historial> captor = ArgumentCaptor.forClass(Historial.class);
        when(historialRepository.save(any(Historial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Historial resultado = gestionBitacoraService.registrarEstado(gestion, null);

        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdGestion()).isEqualTo(gestion);
        assertThat(captor.getValue().getFkIdEstadoGestion()).isEqualTo(estadoInicial);
        assertThat(resultado.getFkIdEstadoGestion()).isEqualTo(estadoInicial);
    }

    @Test
    @DisplayName("Should record historial entry on valid transition")
    void shouldRecordHistorialOnValidTransition() {
        EstadoDeGestion nuevoEstado = new EstadoDeGestion(2, "En trámite");
        gestion.setFkIdEstadoDeGestion(nuevoEstado);
        ArgumentCaptor<Historial> captor = ArgumentCaptor.forClass(Historial.class);
        when(historialRepository.save(any(Historial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gestionBitacoraService.registrarEstado(gestion, null);

        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdEstadoGestion()).isEqualTo(nuevoEstado);
    }

    @Test
    @DisplayName("Should record historial entry on archive")
    void shouldRecordHistorialOnArchive() {
        EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
        gestion.setFkIdEstadoDeGestion(archivada);
        ArgumentCaptor<Historial> captor = ArgumentCaptor.forClass(Historial.class);
        when(historialRepository.save(any(Historial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gestionBitacoraService.registrarEstado(gestion, "Archivado con deuda pendiente");

        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdEstadoGestion()).isEqualTo(archivada);
        assertThat(captor.getValue().getObservaciones()).isEqualTo("Archivado con deuda pendiente");
    }

    @Test
    @DisplayName("Should reject recording historial when gestión has no estado assigned")
    void shouldRejectRecordingWhenNoEstado() {
        gestion.setFkIdEstadoDeGestion(null);

        assertThatThrownBy(() -> gestionBitacoraService.registrarEstado(gestion, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should return ordered historial for a gestión")
    void shouldReturnOrderedHistorial() {
        Historial h1 = new Historial(1);
        h1.setFecha(new Date(1000));
        Historial h2 = new Historial(2);
        h2.setFecha(new Date(3000));
        Historial h3 = new Historial(3);
        h3.setFecha(new Date(2000));

        when(historialRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(h1, h2, h3));

        List<Historial> historial = gestionBitacoraService.obtenerHistorial(1);

        assertThat(historial).containsExactly(h1, h3, h2);
    }
}
