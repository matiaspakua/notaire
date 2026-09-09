package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoManagementResumenFinanciero;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.ManagementArchiveDebtService;
import com.licensis.notaire.service.ManagementResumenFinancieroService;
import com.licensis.notaire.service.PaymentService;
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
    private PaymentService paymentService;

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

    private static Payment paymentOf(float amount) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setDate(new Date());
        return payment;
    }

    @Nested
    @DisplayName("Resumen financiero de una gestión")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Gestión con un único trámite y budget agrega su total, cobrado y saldo")
        void shouldSummarizeSingleProcedureManagement() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(3000.00f);
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(3000.00f);
            when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of(paymentOf(2000.00f)));

            DtoManagementResumenFinanciero resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.idManagement()).isEqualTo(1);
            assertThat(resumen.totalPresupuestado()).isEqualTo(5000.00f);
            assertThat(resumen.totalCobrado()).isEqualTo(2000.00f);
            assertThat(resumen.saldoPending()).isEqualTo(3000.00f);
        }

        @Test
        @DisplayName("Gestión con múltiples trámites y presupuestos suma los totales de cada uno")
        void shouldAggregateMultipleProcedures() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(4500.00f);
            when(procedureRepository.findByFkIdManagementIdManagement(1))
                    .thenReturn(List.of(procedureFor(10), procedureFor(20)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(3000.00f);
            when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of(paymentOf(2000.00f)));
            when(paymentService.calculatePendingBalance(20)).thenReturn(1500.00f);
            when(paymentService.findPaymentsByBudget(20)).thenReturn(List.of(paymentOf(1000.00f)));

            DtoManagementResumenFinanciero resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.totalPresupuestado()).isEqualTo(7500.00f);
            assertThat(resumen.totalCobrado()).isEqualTo(3000.00f);
            assertThat(resumen.saldoPending()).isEqualTo(4500.00f);
        }

        @Test
        @DisplayName("Gestión sin payments registrados devuelve cobrado en cero")
        void shouldReturnZeroCollectedWhenNoPayments() {
            when(managementArchiveDebtService.calculatePendingBalance(1)).thenReturn(5000.00f);
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedureFor(10)));
            when(paymentService.calculatePendingBalance(10)).thenReturn(5000.00f);
            when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of());

            DtoManagementResumenFinanciero resumen = managementResumenFinancieroService.getSummary(1);

            assertThat(resumen.totalCobrado()).isEqualTo(0.00f);
            assertThat(resumen.totalPresupuestado()).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Should throw exception when gestión does not exist")
        void shouldThrowExceptionWhenManagementNotFound() {
            when(managementArchiveDebtService.calculatePendingBalance(999))
                    .thenThrow(new IllegalArgumentException("Gestión no encontrada con ID: 999"));

            assertThatThrownBy(() -> managementResumenFinancieroService.getSummary(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gestión no encontrada");
        }
    }
}
