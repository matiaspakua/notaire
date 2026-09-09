package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.CarpetasEnWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.ManagementArchiveDebtService;
import com.licensis.notaire.service.ManagementTransitionService;
import com.licensis.notaire.service.PaymentService;
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
class ManagementArchiveDebtServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ManagementStatusRepository statusRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ManagementTransitionService managementTransitionService;

    @Mock
    private ProcedureFolderRepository procedureFolderRepository;

    @InjectMocks
    private ManagementArchiveDebtService managementArchiveDebtService;

    private DeedManagement testManagement;

    @BeforeEach
    void setUp() {
        testManagement = new DeedManagement();
        testManagement.setIdManagement(1);
        lenient().when(procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(1, "Espera"))
                .thenReturn(List.of());
        lenient().when(procedureFolderRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of());
    }

    private static Procedure procedureFor(Integer idBudget) {
        Budget budget = new Budget();
        budget.setIdBudget(idBudget);
        Procedure procedure = new Procedure();
        procedure.setFkIdBudget(budget);
        return procedure;
    }

    @Nested
    @DisplayName("Calcular saldo pendiente de una gestión")
    class CalcularSaldoPendingTests {

        @Test
        @DisplayName("Gestión con un único trámite y budget devuelve el saldo de ese budget")
        void shouldReturnSingleBudgetBalanceForSingleProcedure() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(5000.00f);

            Float saldo = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(saldo).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Gestión con múltiples trámites y presupuestos suma el saldo de cada budget")
        void shouldSumBalancesAcrossMultipleProceduresAndPresupuestos() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(3000.00f);
            when(paymentService.calculatePendingBalance(20)).thenReturn(1500.00f);

            Float saldo = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(saldo).isEqualTo(4500.00f);
        }

        @Test
        @DisplayName("Gestión cuyos presupuestos están totalmente pagados devuelve cero")
        void shouldReturnZeroWhenAllPresupuestosAreFullyPaid() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);

            Float saldo = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(saldo).isEqualTo(0.00f);
        }

        @Test
        @DisplayName("Should throw exception when gestión does not exist")
        void shouldThrowExceptionWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> managementArchiveDebtService.calculatePendingBalance(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gestión no encontrada");
        }
    }

    @Nested
    @DisplayName("Archivar gestión")
    class ArchivingTests {

        @Test
        @DisplayName("Archiving succeeds when the transition to Archivada is valid AND no deuda pendiente")
        void shouldArchiveWhenTransitionValidAndNoDebt() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);  // No deuda - required for archive
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getFkIdManagementStatus()).isEqualTo(archivada);
            assertThat(result.management().getPendingDebtAtArchiving()).isFalse();
        }

        @Test
        @DisplayName("Archiving is rejected when the transition to Archivada is invalid")
        void shouldRejectArchiveWhenTransitionInvalid() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);
            when(managementTransitionService.transicionar(1, "Archivada"))
                    .thenThrow(new BusinessValidationException("Transición no permitida"));

            assertThatThrownBy(() -> managementArchiveDebtService.archiving(1))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Transición no permitida");
        }

        @Test
        @DisplayName("CU16: Archive succeeds and records deuda pendiente when saldo is positive (warning, not a block)")
        void shouldArchiveAndFlagDebtWhenSaldoPositive() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(20000.00f);  // Deuda exists
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isTrue();
            assertThat(result.saldoPending()).isEqualTo(20000.00f);
        }

        @Test
        @DisplayName("Issue #169: Archive succeeds when saldo pendiente is zero")
        void shouldArchiveSuccessfullyWhenNoDeutaPending() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);  // No deuda
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isFalse();
            assertThat(result.saldoPending()).isEqualTo(0.00f);
        }

        @Test
        @DisplayName("CU16: Archive succeeds and aggregates deuda across multiple presupuestos")
        void shouldArchiveAndAggregateDebtAcrossMultiplePresupuestos() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(15000.00f);
            when(paymentService.calculatePendingBalance(20)).thenReturn(25000.00f);
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);
            // Total deuda = 40000

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isTrue();
            assertThat(result.saldoPending()).isEqualTo(40000.00f);
        }

        @Test
        @DisplayName("CU85: Archiving cascades all carpetas de trámite of the gestión to Archivada")
        void shouldCascadeArchiveCarpetasWhenManagementArchived() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);
            ProcedureFolder folder = new ProcedureFolder();
            folder.setStatus("Activa");

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);
            when(procedureFolderRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(folder));

            managementArchiveDebtService.archiving(1);

            ArgumentCaptor<List<ProcedureFolder>> captor = ArgumentCaptor.forClass(List.class);
            verify(procedureFolderRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).extracting(ProcedureFolder::getStatus).containsExactly("Archivada");
        }

        @Test
        @DisplayName("CU85: Archiving is rejected when a carpeta de trámite is in Espera and not confirmed")
        void shouldRejectArchiveWhenFolderEnWaitAndNotConfirmed() {
            ProcedureFolder folderEnWait = new ProcedureFolder();
            folderEnWait.setNumber(1);
            folderEnWait.setStatus("Espera");
            when(procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(1, "Espera"))
                    .thenReturn(List.of(folderEnWait));

            assertThatThrownBy(() -> managementArchiveDebtService.archiving(1, false))
                    .isInstanceOf(CarpetasEnWaitException.class);

            verify(managementRepository, never()).save(testManagement);
        }

        @Test
        @DisplayName("CU85: Archiving proceeds despite carpetas en Espera when explicitly confirmed")
        void shouldArchiveWhenFolderEnWaitButConfirmed() {
            ManagementStatus archivada = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivada);
            ProcedureFolder folderEnWait = new ProcedureFolder();
            folderEnWait.setNumber(1);
            folderEnWait.setStatus("Espera");

            when(procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(1, "Espera"))
                    .thenReturn(List.of(folderEnWait));
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(0.00f);
            when(managementTransitionService.transicionar(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1, true);

            assertThat(result.management().getFkIdManagementStatus()).isEqualTo(archivada);
        }
    }
}
