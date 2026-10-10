package com.licensis.notaire.unit;


import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService;
import com.licensis.notaire.application.usecase.management.ManagementResumenFinancieroService;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.domain.payment.PaymentDetails;
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
class ManagementResumenFinancieroServiceTest {

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private GetPaymentStatusUseCase paymentStatus;

    @Mock
    private QueryPaymentsUseCase paymentQueries;

    @Mock
    private ManagementArchiveDebtService managementArchiveDebtService;

    @InjectMocks
    private ManagementResumenFinancieroService managementResumenFinancieroService;

    private static Procedure procedureFor(Integer idBudget) {
        Budget budget = new Budget();
        budget.setIdBudget(idBudget);
        Procedure procedure = new Procedure();
        procedure.setFkIdBudget(budget);
        return procedure;
    }

    private static PaymentDetails paymentOf(java.math.BigDecimal amount) {
        return new PaymentDetails(null, null, amount, new Date(), null, null);
    }

    @Nested
    @DisplayName("Financial summary of a management")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Management with a único procedure and budget agrega its total, cobrado and balance")
        void shouldSummarizeSingleProcedureManagement() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(new java.math.BigDecimal("3000.00"));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("3000.00"));
            when(paymentQueries.findByBudget(10)).thenReturn(List.of(paymentOf(new java.math.BigDecimal("2000.00"))));

            DtoManagementFinancialSummary resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.idManagement()).isEqualTo(1);
            assertThat(resumen.totalPresupuestado()).isEqualByComparingTo(new java.math.BigDecimal("5000.00"));
            assertThat(resumen.totalCobrado()).isEqualByComparingTo(new java.math.BigDecimal("2000.00"));
            assertThat(resumen.pendingBalance()).isEqualByComparingTo(new java.math.BigDecimal("3000.00"));
        }

        @Test
        @DisplayName("Management with múltiples procedures and budgets suma the totales of each uno")
        void shouldAggregateMultipleProcedures() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(new java.math.BigDecimal("4500.00"));
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("3000.00"));
            when(paymentQueries.findByBudget(10)).thenReturn(List.of(paymentOf(new java.math.BigDecimal("2000.00"))));
            when(paymentStatus.pendingBalance(20)).thenReturn(new java.math.BigDecimal("1500.00"));
            when(paymentQueries.findByBudget(20)).thenReturn(List.of(paymentOf(new java.math.BigDecimal("1000.00"))));

            DtoManagementFinancialSummary resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.totalPresupuestado()).isEqualByComparingTo(new java.math.BigDecimal("7500.00"));
            assertThat(resumen.totalCobrado()).isEqualByComparingTo(new java.math.BigDecimal("3000.00"));
            assertThat(resumen.pendingBalance()).isEqualByComparingTo(new java.math.BigDecimal("4500.00"));
        }

        @Test
        @DisplayName("Management without payments registrados devuelve cobrado in cero")
        void shouldReturnZeroCollectedWhenNoPayments() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(new java.math.BigDecimal("5000.00"));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentStatus.pendingBalance(10)).thenReturn(new java.math.BigDecimal("5000.00"));
            when(paymentQueries.findByBudget(10)).thenReturn(List.of());

            DtoManagementFinancialSummary resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.totalCobrado()).isEqualByComparingTo(new java.math.BigDecimal("0.00"));
            assertThat(resumen.totalPresupuestado()).isEqualByComparingTo(new java.math.BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Should throw exception when management does not exist")
        void shouldThrowExceptionWhenManagementNotFound() {
            when(managementArchiveDebtService.calculatePendingBalance(999))
                    .thenThrow(new IllegalArgumentException("Management not found with ID: 999"));

            assertThatThrownBy(() -> managementResumenFinancieroService.getSummary(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Management not found");
        }
    }
}
