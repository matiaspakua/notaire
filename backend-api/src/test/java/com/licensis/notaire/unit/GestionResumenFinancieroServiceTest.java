package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoGestionResumenFinanciero;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.GestionArchiveDebtService;
import com.licensis.notaire.service.GestionResumenFinancieroService;
import com.licensis.notaire.service.PagoService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU47", "CU02"})
@DisplayName("GestionResumenFinancieroService Tests")
@ExtendWith(MockitoExtension.class)
class GestionResumenFinancieroServiceTest {

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private PagoService pagoService;

    @Mock
    private GestionArchiveDebtService gestionArchiveDebtService;

    @InjectMocks
    private GestionResumenFinancieroService gestionResumenFinancieroService;

    private static Tramite tramiteFor(Integer idPresupuesto) {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(idPresupuesto);
        Tramite tramite = new Tramite();
        tramite.setFkIdPresupuesto(presupuesto);
        return tramite;
    }

    private static Pago pagoOf(float monto) {
        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setFecha(new Date());
        return pago;
    }

    @Nested
    @DisplayName("Resumen financiero de una gestión")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Gestión con un único trámite y presupuesto agrega su total, cobrado y saldo")
        void shouldSummarizeSingleTramiteGestion() {
            when(gestionArchiveDebtService.calcularSaldoPendiente(1)).thenReturn(3000.00f);
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(3000.00f);
            when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of(pagoOf(2000.00f)));

            DtoGestionResumenFinanciero resumen = gestionResumenFinancieroService.obtenerResumen(1);

            assertThat(resumen.idGestion()).isEqualTo(1);
            assertThat(resumen.totalPresupuestado()).isEqualTo(5000.00f);
            assertThat(resumen.totalCobrado()).isEqualTo(2000.00f);
            assertThat(resumen.saldoPendiente()).isEqualTo(3000.00f);
        }

        @Test
        @DisplayName("Gestión con múltiples trámites y presupuestos suma los totales de cada uno")
        void shouldAggregateMultipleTramites() {
            when(gestionArchiveDebtService.calcularSaldoPendiente(1)).thenReturn(4500.00f);
            when(tramiteRepository.findByFkIdGestionIdGestion(1))
                    .thenReturn(List.of(tramiteFor(10), tramiteFor(20)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(3000.00f);
            when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of(pagoOf(2000.00f)));
            when(pagoService.calcularSaldoPendiente(20)).thenReturn(1500.00f);
            when(pagoService.findPagosByPresupuesto(20)).thenReturn(List.of(pagoOf(1000.00f)));

            DtoGestionResumenFinanciero resumen = gestionResumenFinancieroService.obtenerResumen(1);

            assertThat(resumen.totalPresupuestado()).isEqualTo(7500.00f);
            assertThat(resumen.totalCobrado()).isEqualTo(3000.00f);
            assertThat(resumen.saldoPendiente()).isEqualTo(4500.00f);
        }

        @Test
        @DisplayName("Gestión sin pagos registrados devuelve cobrado en cero")
        void shouldReturnZeroCollectedWhenNoPayments() {
            when(gestionArchiveDebtService.calcularSaldoPendiente(1)).thenReturn(5000.00f);
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(5000.00f);
            when(pagoService.findPagosByPresupuesto(10)).thenReturn(List.of());

            DtoGestionResumenFinanciero resumen = gestionResumenFinancieroService.obtenerResumen(1);

            assertThat(resumen.totalCobrado()).isEqualTo(0.00f);
            assertThat(resumen.totalPresupuestado()).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Should throw exception when gestión does not exist")
        void shouldThrowExceptionWhenGestionNotFound() {
            when(gestionArchiveDebtService.calcularSaldoPendiente(999))
                    .thenThrow(new IllegalArgumentException("Gestión no encontrada con ID: 999"));

            assertThatThrownBy(() -> gestionResumenFinancieroService.obtenerResumen(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gestión no encontrada");
        }
    }
}
