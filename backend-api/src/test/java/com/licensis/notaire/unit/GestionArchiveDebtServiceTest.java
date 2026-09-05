package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.CarpetasEnEsperaException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.CarpetaTramiteRepository;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.GestionArchiveDebtService;
import com.licensis.notaire.service.GestionTransitionService;
import com.licensis.notaire.service.PagoService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Mock
    private GestionTransitionService gestionTransitionService;

    @Mock
    private CarpetaTramiteRepository carpetaTramiteRepository;

    @InjectMocks
    private GestionArchiveDebtService gestionArchiveDebtService;

    private GestionDeEscritura testGestion;

    @BeforeEach
    void setUp() {
        testGestion = new GestionDeEscritura();
        testGestion.setIdGestion(1);
        lenient().when(carpetaTramiteRepository.findByFkIdGestionIdGestionAndEstado(1, "Espera"))
                .thenReturn(List.of());
        lenient().when(carpetaTramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of());
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

    @Nested
    @DisplayName("Archivar gestión")
    class ArchivarTests {

        @Test
        @DisplayName("Archiving succeeds when the transition to Archivada is valid AND no deuda pendiente")
        void shouldArchiveWhenTransitionValidAndNoDeuda() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);  // No deuda - required for archive
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);

            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(1);

            assertThat(result.gestion().getFkIdEstadoDeGestion()).isEqualTo(archivada);
            assertThat(result.gestion().getDeudaPendienteAlArchivar()).isFalse();
        }

        @Test
        @DisplayName("Archiving is rejected when the transition to Archivada is invalid")
        void shouldRejectArchiveWhenTransitionInvalid() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);
            when(gestionTransitionService.transicionar(1, "Archivada"))
                    .thenThrow(new BusinessValidationException("Transición no permitida"));

            assertThatThrownBy(() -> gestionArchiveDebtService.archivar(1))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Transición no permitida");
        }

        @Test
        @DisplayName("CU16: Archive succeeds and records deuda pendiente when saldo is positive (warning, not a block)")
        void shouldArchiveAndFlagDeudaWhenSaldoPositive() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(20000.00f);  // Deuda exists
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);

            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(1);

            assertThat(result.gestion().getDeudaPendienteAlArchivar()).isTrue();
            assertThat(result.saldoPendiente()).isEqualTo(20000.00f);
        }

        @Test
        @DisplayName("Issue #169: Archive succeeds when saldo pendiente is zero")
        void shouldArchiveSuccessfullyWhenNoDeutaPending() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);  // No deuda
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);

            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(1);

            assertThat(result.gestion().getDeudaPendienteAlArchivar()).isFalse();
            assertThat(result.saldoPendiente()).isEqualTo(0.00f);
        }

        @Test
        @DisplayName("CU16: Archive succeeds and aggregates deuda across multiple presupuestos")
        void shouldArchiveAndAggregateDeudaAcrossMultiplePresupuestos() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1))
                    .thenReturn(List.of(tramiteFor(10), tramiteFor(20)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(15000.00f);
            when(pagoService.calcularSaldoPendiente(20)).thenReturn(25000.00f);
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);
            // Total deuda = 40000

            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(1);

            assertThat(result.gestion().getDeudaPendienteAlArchivar()).isTrue();
            assertThat(result.saldoPendiente()).isEqualTo(40000.00f);
        }

        @Test
        @DisplayName("CU85: Archiving cascades all carpetas de trámite of the gestión to Archivada")
        void shouldCascadeArchiveCarpetasWhenGestionArchived() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);
            CarpetaTramite carpeta = new CarpetaTramite();
            carpeta.setEstado("Activa");

            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);
            when(carpetaTramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(carpeta));

            gestionArchiveDebtService.archivar(1);

            ArgumentCaptor<List<CarpetaTramite>> captor = ArgumentCaptor.forClass(List.class);
            verify(carpetaTramiteRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).extracting(CarpetaTramite::getEstado).containsExactly("Archivada");
        }

        @Test
        @DisplayName("CU85: Archiving is rejected when a carpeta de trámite is in Espera and not confirmed")
        void shouldRejectArchiveWhenCarpetaEnEsperaAndNotConfirmed() {
            CarpetaTramite carpetaEnEspera = new CarpetaTramite();
            carpetaEnEspera.setNumero(1);
            carpetaEnEspera.setEstado("Espera");
            when(carpetaTramiteRepository.findByFkIdGestionIdGestionAndEstado(1, "Espera"))
                    .thenReturn(List.of(carpetaEnEspera));

            assertThatThrownBy(() -> gestionArchiveDebtService.archivar(1, false))
                    .isInstanceOf(CarpetasEnEsperaException.class);

            verify(gestionRepository, never()).save(testGestion);
        }

        @Test
        @DisplayName("CU85: Archiving proceeds despite carpetas en Espera when explicitly confirmed")
        void shouldArchiveWhenCarpetaEnEsperaButConfirmed() {
            EstadoDeGestion archivada = new EstadoDeGestion(3, "Archivada");
            testGestion.setFkIdEstadoDeGestion(archivada);
            CarpetaTramite carpetaEnEspera = new CarpetaTramite();
            carpetaEnEspera.setNumero(1);
            carpetaEnEspera.setEstado("Espera");

            when(carpetaTramiteRepository.findByFkIdGestionIdGestionAndEstado(1, "Espera"))
                    .thenReturn(List.of(carpetaEnEspera));
            when(gestionRepository.findById(1)).thenReturn(Optional.of(testGestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramiteFor(10)));
            when(pagoService.calcularSaldoPendiente(10)).thenReturn(0.00f);
            when(gestionTransitionService.transicionar(1, "Archivada")).thenReturn(testGestion);
            when(gestionRepository.save(testGestion)).thenReturn(testGestion);

            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(1, true);

            assertThat(result.gestion().getFkIdEstadoDeGestion()).isEqualTo(archivada);
        }
    }
}
