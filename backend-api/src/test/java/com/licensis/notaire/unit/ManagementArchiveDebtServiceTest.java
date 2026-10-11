package com.licensis.notaire.unit;


import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.FoldersInWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureFolderRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService;
import com.licensis.notaire.application.usecase.management.ManagementTransitionService;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
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
@DisplayName("ManagementArchiveDebtService tests")
@ExtendWith(MockitoExtension.class)
class ManagementArchiveDebtServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ManagementStatusRepository statusRepository;

    @Mock
    private GetPaymentStatusUseCase paymentStatus;

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
    @DisplayName("Calculate outstanding balance of a management")
    class CalcularSaldoPendingTests {

        @Test
        @DisplayName("Management with one procedure and budget returns that budget's balance")
        void shouldReturnSingleBudgetBalanceForSingleProcedure() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("5000.00"));

            java.math.BigDecimal pendingBalance = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Management with multiple procedures and budgets sums each budget's balance")
        void shouldSumBalancesAcrossMultipleProceduresAndBudgets() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("3000.00"));
            when(paymentStatus.pendingBalance(20)).thenReturn(new java.math.BigDecimal("1500.00"));

            java.math.BigDecimal pendingBalance = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("4500.00"));
        }

        @Test
        @DisplayName("Management whose budgets are fully paid returns zero")
        void shouldReturnZeroWhenAllBudgetsAreFullyPaid() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));

            java.math.BigDecimal pendingBalance = managementArchiveDebtService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should throw exception when management does not exist")
        void shouldThrowExceptionWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> managementArchiveDebtService.calculatePendingBalance(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Management not found");
        }
    }

    @Nested
    @DisplayName("Archive a management")
    class ArchivingTests {

        @Test
        @DisplayName("Archiving succeeds when the transition to Archivada is valid and there is no outstanding balance")
        void shouldArchiveWhenTransitionValidAndNoDebt() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));  // No outstanding balance — required to archive
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getFkIdManagementStatus()).isEqualTo(archivedStatus);
            assertThat(result.management().getPendingDebtAtArchiving()).isFalse();
        }

        @Test
        @DisplayName("Archiving is rejected when the transition to Archivada is invalid")
        void shouldRejectArchiveWhenTransitionInvalid() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));
            when(managementTransitionService.transition(1, "Archivada"))
                    .thenThrow(new BusinessValidationException("Transition is not allowed"));

            assertThatThrownBy(() -> managementArchiveDebtService.archiving(1))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Transition is not allowed");
        }

        @Test
        @DisplayName("CU16: Archive succeeds and records outstanding debt when the balance is positive")
        void shouldArchiveAndFlagDebtWhenBalancePositive() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("20000.00"));  // Outstanding balance exists
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isTrue();
            assertThat(result.pendingBalance()).isEqualByComparingTo(new java.math.BigDecimal("20000.00"));
        }

        @Test
        @DisplayName("Issue #169: Archive succeeds when the outstanding balance is zero")
        void shouldArchiveSuccessfullyWhenNoDebtPending() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));  // No outstanding balance
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isFalse();
            assertThat(result.pendingBalance()).isEqualByComparingTo(new java.math.BigDecimal("0.00"));
        }

        @Test
        @DisplayName("CU16: Archive succeeds and aggregates debt across multiple budgets")
        void shouldArchiveAndAggregateDebtAcrossMultipleBudgets() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("15000.00"));
            when(paymentStatus.pendingBalance(20)).thenReturn(new java.math.BigDecimal("25000.00"));
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);
            // Total deuda = 40000

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1);

            assertThat(result.management().getPendingDebtAtArchiving()).isTrue();
            assertThat(result.pendingBalance()).isEqualByComparingTo(new java.math.BigDecimal("40000.00"));
        }

        @Test
        @DisplayName("CU85: Archiving cascades all procedure folders of the management to Archivada")
        void shouldCascadeArchiveFoldersWhenManagementArchived() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);
            ProcedureFolder folder = new ProcedureFolder();
            folder.setStatus("Activa");

            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);
            when(procedureFolderRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(folder));

            managementArchiveDebtService.archiving(1);

            ArgumentCaptor<List<ProcedureFolder>> captor = ArgumentCaptor.forClass(List.class);
            verify(procedureFolderRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).extracting(ProcedureFolder::getStatus).containsExactly("Archivada");
        }

        @Test
        @DisplayName("CU85: Archiving is rejected when a procedure folder is in Espera and not confirmed")
        void shouldRejectArchiveWhenFolderOnHoldAndNotConfirmed() {
            ProcedureFolder folderEnWait = new ProcedureFolder();
            folderEnWait.setNumber(1);
            folderEnWait.setStatus("Espera");
            when(procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(1, "Espera"))
                    .thenReturn(List.of(folderEnWait));

            assertThatThrownBy(() -> managementArchiveDebtService.archiving(1, false))
                    .isInstanceOf(FoldersInWaitException.class);

            verify(managementRepository, never()).save(testManagement);
        }

        @Test
        @DisplayName("CU85: Archiving proceeds despite folders in Espera when explicitly confirmed")
        void shouldArchiveWhenFolderOnHoldButConfirmed() {
            ManagementStatus archivedStatus = new ManagementStatus(3, "Archivada");
            testManagement.setFkIdManagementStatus(archivedStatus);
            ProcedureFolder folderEnWait = new ProcedureFolder();
            folderEnWait.setNumber(1);
            folderEnWait.setStatus("Espera");

            when(procedureFolderRepository.findByFkIdManagementIdManagementAndStatus(1, "Espera"))
                    .thenReturn(List.of(folderEnWait));
            when(managementRepository.findById(1)).thenReturn(Optional.of(testManagement));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("0.00"));
            when(managementTransitionService.transition(1, "Archivada")).thenReturn(testManagement);
            when(managementRepository.save(testManagement)).thenReturn(testManagement);

            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(1, true);

            assertThat(result.management().getFkIdManagementStatus()).isEqualTo(archivedStatus);
        }
    }
}
