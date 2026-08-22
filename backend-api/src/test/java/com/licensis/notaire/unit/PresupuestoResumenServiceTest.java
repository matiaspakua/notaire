package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoPresupuestoResumen;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.PagoService;
import com.licensis.notaire.service.PresupuestoResumenService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU47"})
@DisplayName("PresupuestoResumenService Tests")
@ExtendWith(MockitoExtension.class)
class PresupuestoResumenServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PresupuestoResumenService presupuestoResumenService;

    private Presupuesto presupuesto;

    @BeforeEach
    void setUp() {
        presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(10);
        presupuesto.setNumero(100);
    }

    private static Tramite tramiteFor(GestionDeEscritura gestion) {
        Tramite tramite = new Tramite();
        tramite.setFkIdGestion(gestion);
        return tramite;
    }

    private Pago pagoOf(Integer idPago, float monto) {
        Pago pago = new Pago();
        pago.setIdPago(idPago);
        pago.setMonto(monto);
        pago.setFecha(new Date());
        pago.setPresupuesto(presupuesto);
        return pago;
    }

    @Nested
    @DisplayName("Resumen financiero de un presupuesto")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Presupuesto sin pagos muestra saldo igual al total y lista vacía")
        void shouldReturnFullBalanceWhenNoPayments() {
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setNumero(500);
            gestion.setEncabezado("Gestión Test");

            when(presupuestoRepository.findById(10)).thenReturn(Optional.of(presupuesto));
            when(tramiteRepository.findByFkIdPresupuestoIdPresupuesto(10)).thenReturn(List.of(tramiteFor(gestion)));
            when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of());
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(5000.00f);

            DtoPresupuestoResumen resumen = presupuestoResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPendiente()).isEqualTo(5000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.pagos()).isEmpty();
            assertThat(resumen.idGestion()).isEqualTo(1);
            assertThat(resumen.numeroGestion()).isEqualTo(500);
            assertThat(resumen.encabezadoGestion()).isEqualTo("Gestión Test");
        }

        @Test
        @DisplayName("Presupuesto con un pago reduce el saldo y lo incluye en la lista")
        void shouldReturnReducedBalanceWithOnePayment() {
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setNumero(500);
            gestion.setEncabezado("Gestión Test");

            when(presupuestoRepository.findById(10)).thenReturn(Optional.of(presupuesto));
            when(tramiteRepository.findByFkIdPresupuestoIdPresupuesto(10)).thenReturn(List.of(tramiteFor(gestion)));
            when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of(pagoOf(1, 2000.00f)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(3000.00f);

            DtoPresupuestoResumen resumen = presupuestoResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPendiente()).isEqualTo(3000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.pagos()).hasSize(1);
            assertThat(resumen.pagos().get(0).idPago()).isEqualTo(1);
            assertThat(resumen.pagos().get(0).monto()).isEqualTo(2000.00f);
            assertThat(resumen.pagos().get(0).idPresupuesto()).isEqualTo(10);
        }

        @Test
        @DisplayName("Presupuesto con múltiples pagos muestra el saldo neto y todos los pagos")
        void shouldReturnNetBalanceWithMultiplePayments() {
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(1);
            gestion.setNumero(500);
            gestion.setEncabezado("Gestión Test");

            when(presupuestoRepository.findById(10)).thenReturn(Optional.of(presupuesto));
            when(tramiteRepository.findByFkIdPresupuestoIdPresupuesto(10)).thenReturn(List.of(tramiteFor(gestion)));
            when(pagoService.findPagosByPresupuesto(10))
                    .thenReturn(List.of(pagoOf(1, 2000.00f), pagoOf(2, 1000.00f)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(2000.00f);

            DtoPresupuestoResumen resumen = presupuestoResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPendiente()).isEqualTo(2000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.pagos()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw exception when presupuesto does not exist")
        void shouldThrowExceptionWhenPresupuestoNotFound() {
            when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> presupuestoResumenService.obtenerResumen(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presupuesto no encontrado");
        }
    }
}
