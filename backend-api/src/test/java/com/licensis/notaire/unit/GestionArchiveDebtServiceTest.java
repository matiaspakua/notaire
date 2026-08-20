package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.GestionArchiveDebtService;
import com.licensis.notaire.service.PagoService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU16", "RF-22", "RF-37"})
@DisplayName("GestionArchiveDebtService Tests")
@ExtendWith(MockitoExtension.class)
class GestionArchiveDebtServiceTest {

    @Mock
    private GestionDeEscrituraRepository gestionRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private EstadoDeGestionRepository estadoRepository;

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private GestionArchiveDebtService gestionArchiveDebtService;

    private GestionDeEscritura testGestion;

    @BeforeEach
    void setUp() {
        testGestion = new GestionDeEscritura();
        testGestion.setIdGestion(1);
    }

    private static Tramite tramiteFor(Integer idPresupuesto) {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(idPresupuesto);
        Tramite tramite = new Tramite();
        tramite.setFkIdPresupuesto(presupuesto);
        return tramite;
    }

    @Nested
    @DisplayName("Calcular saldo pendiente de una gestión")
    class CalcularSaldoPendienteTests {

        @Test
        @DisplayName("Gestión con un único trámite y presupuesto devuelve el saldo de ese presupuesto")
        void shouldReturnSinglePresupuestoBalanceForSingleTramite() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(5000.00f);

            Float saldo = gestionArchiveDebtService.calcularSaldoPendiente(1);

            assertThat(saldo).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Gestión con múltiples trámites y presupuestos suma el saldo de cada presupuesto")
        void shouldSumBalancesAcrossMultipleTramitesAndPresupuestos() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1))
                    .thenReturn(List.of(tramiteFor(10), tramiteFor(20)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(3000.00f);
            when(pagoService.calcularSaldoPendiente(20)).thenReturn(1500.00f);

            Float saldo = gestionArchiveDebtService.calcularSaldoPendiente(1);

            assertThat(saldo).isEqualTo(4500.00f);
        }

        @Test
        @DisplayName("Gestión cuyos presupuestos están totalmente pagados devuelve cero")
        void shouldReturnZeroWhenAllPresupuestosAreFullyPaid() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);

            Float saldo = gestionArchiveDebtService.calcularSaldoPendiente(1);

            assertThat(saldo).isEqualTo(0.00f);
        }

        @Test
        @DisplayName("Should throw exception when gestión does not exist")
        void shouldThrowExceptionWhenGestionNotFound() {
            when(gestionRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gestionArchiveDebtService.calcularSaldoPendiente(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gestión no encontrada");
        }
    }
}
