package com.licensis.notaire.service.unit;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.EstadoPago;
import com.licensis.notaire.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService Unit Tests")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private PagoService pagoService;

    private Presupuesto testPresupuesto;
    private Pago testPago;
    private Date testDate;

    @BeforeEach
    void setUp() {
        testDate = new Date();

        testPresupuesto = new Presupuesto();
        testPresupuesto.setIdPresupuesto(1);
        testPresupuesto.setNumero(100);
        testPresupuesto.setMontoInmueble(5000f);

        testPago = new Pago();
        testPago.setIdPago(1);
        testPago.setMonto(1000f);
        testPago.setFecha(testDate);
        testPago.setPresupuesto(testPresupuesto);
    }

    @Test
    @DisplayName("Should process valid pago")
    void shouldProcessValidPago() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(null);
        when(pagoRepository.save(any(Pago.class))).thenReturn(testPago);

        Pago result = pagoService.procesarPago(1, 1000f, testDate, "Test");

        assertThat(result).isNotNull()
                .extracting(Pago::getMonto, Pago::getIdPago)
                .containsExactly(1000f, 1);

        verify(presupuestoRepository, times(2)).findById(1);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when presupuesto not found")
    void shouldThrowExceptionWhenPresupuestoNotFound() {
        when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.procesarPago(999, 1000f, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(presupuestoRepository, times(1)).findById(999);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when monto is null")
    void shouldThrowExceptionWhenMontoIsNull() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

        assertThatThrownBy(() -> pagoService.procesarPago(1, null, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto del pago debe ser mayor a cero");

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when monto is zero")
    void shouldThrowExceptionWhenMontoIsZero() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

        assertThatThrownBy(() -> pagoService.procesarPago(1, 0f, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto del pago debe ser mayor a cero");

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when monto is negative")
    void shouldThrowExceptionWhenMontoIsNegative() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

        assertThatThrownBy(() -> pagoService.procesarPago(1, -100f, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto del pago debe ser mayor a cero");

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should consult pago by id")
    void shouldConsultPagoById() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(testPago));

        Optional<Pago> result = pagoService.consultarPago(1);

        assertThat(result).isPresent()
                .contains(testPago);

        verify(pagoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when pago not found")
    void shouldReturnEmptyWhenPagoNotFound() {
        when(pagoRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Pago> result = pagoService.consultarPago(999);

        assertThat(result).isEmpty();

        verify(pagoRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find pagos by presupuesto")
    void shouldFindPagosByPresupuesto() {
        List<Pago> pagos = new ArrayList<>();
        pagos.add(testPago);

        when(pagoRepository.findByFkIdPresupuestoIdPresupuesto(1))
                .thenReturn(pagos);

        List<Pago> result = pagoService.findPagosByPresupuesto(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPago);

        verify(pagoRepository, times(1)).findByFkIdPresupuestoIdPresupuesto(1);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente correctly")
    void shouldCalculateSaldoPendiente() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(1000f);

        Float result = pagoService.calcularSaldoPendiente(1);

        assertThat(result).isEqualTo(4000f);

        verify(presupuestoRepository, times(1)).findById(1);
        verify(pagoRepository, times(1)).sumMontoByPresupuestoId(1);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente with null totalPagado")
    void shouldCalculateSaldoPendienteWithNullTotal() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(null);

        Float result = pagoService.calcularSaldoPendiente(1);

        assertThat(result).isEqualTo(5000f);

        verify(presupuestoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should report estado SIN_PAGOS when presupuesto has no pagos")
    void shouldCalculateEstadoPagoSinPagos() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(null);

        EstadoPago result = pagoService.calcularEstadoPago(1);

        assertThat(result).isEqualTo(EstadoPago.SIN_PAGOS);
    }

    @Test
    @DisplayName("Should report estado PARCIAL when saldo pendiente is positive but some pagos exist")
    void shouldCalculateEstadoPagoParcial() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(1000f);

        EstadoPago result = pagoService.calcularEstadoPago(1);

        assertThat(result).isEqualTo(EstadoPago.PARCIAL);
    }

    @Test
    @DisplayName("Should report estado SALDADO when saldo pendiente is zero")
    void shouldCalculateEstadoPagoSaldado() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
        when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(5000f);

        EstadoPago result = pagoService.calcularEstadoPago(1);

        assertThat(result).isEqualTo(EstadoPago.SALDADO);
    }

    @Test
    @DisplayName("Should find all pagos")
    void shouldFindAll() {
        List<Pago> pagos = new ArrayList<>();
        pagos.add(testPago);

        when(pagoRepository.findAll()).thenReturn(pagos);

        List<Pago> result = pagoService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPago);

        verify(pagoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find pagos by fecha range")
    void shouldFindPagosByFechaRange() {
        Date startDate = new Date(testDate.getTime() - 86400000);
        Date endDate = new Date(testDate.getTime() + 86400000);
        List<Pago> pagos = new ArrayList<>();
        pagos.add(testPago);

        when(pagoRepository.findByFechaBetween(startDate, endDate))
                .thenReturn(pagos);

        List<Pago> result = pagoService.findPagosByFechaRange(startDate, endDate);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPago);

        verify(pagoRepository, times(1)).findByFechaBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should delete pago")
    void shouldDeletePago() {
        when(pagoRepository.existsById(1)).thenReturn(true);

        pagoService.deletePago(1);

        verify(pagoRepository, times(1)).existsById(1);
        verify(pagoRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pago")
    void shouldThrowExceptionWhenDeletingNonExistentPago() {
        when(pagoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> pagoService.deletePago(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pago no encontrado");

        verify(pagoRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should edit pago")
    void shouldEditarPago() {
        Pago editedPago = new Pago();
        editedPago.setIdPago(1);
        editedPago.setMonto(2000f);
        editedPago.setFecha(testDate);

        when(pagoRepository.findById(1)).thenReturn(Optional.of(testPago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(editedPago);

        Pago result = pagoService.editarPago(1, 2000f, testDate, "Edited");

        assertThat(result).isNotNull()
                .extracting(Pago::getMonto)
                .isEqualTo(2000f);

        verify(pagoRepository, times(1)).findById(1);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent pago")
    void shouldThrowExceptionWhenEditingNonExistentPago() {
        when(pagoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.editarPago(999, 1000f, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pago no encontrado");

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Should throw exception when editing pago with invalid monto")
    void shouldThrowExceptionWhenEditingWithInvalidMonto() {
        when(pagoRepository.findById(1)).thenReturn(Optional.of(testPago));

        assertThatThrownBy(() -> pagoService.editarPago(1, -100f, testDate, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto del pago debe ser mayor a cero");

        verify(pagoRepository, never()).save(any(Pago.class));
    }
}
