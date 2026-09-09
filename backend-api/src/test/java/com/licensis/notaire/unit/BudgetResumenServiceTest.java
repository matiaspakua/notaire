package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoBudgetResumen;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.PaymentService;
import com.licensis.notaire.service.BudgetResumenService;
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
class BudgetResumenServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BudgetResumenService budgetResumenService;

    private Budget budget;

    @BeforeEach
    void setUp() {
        budget = new Budget();
        budget.setIdBudget(10);
        budget.setNumber(100);
    }

    private static Procedure procedureFor(DeedManagement management) {
        Procedure procedure = new Procedure();
        procedure.setFkIdManagement(management);
        return procedure;
    }

    private Payment paymentOf(Integer idPayment, float amount) {
        Payment payment = new Payment();
        payment.setIdPayment(idPayment);
        payment.setAmount(amount);
        payment.setDate(new Date());
        payment.setBudget(budget);
        return payment;
    }

    @Nested
    @DisplayName("Resumen financiero de un budget")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Presupuesto sin payments muestra saldo igual al total y lista vacía")
        void shouldReturnFullBalanceWhenNoPayments() {
            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setNumber(500);
            management.setEncabezado("Gestión Test");

            when(budgetRepository.findById(10)).thenReturn(Optional.of(budget));
            when(procedureRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of(procedureFor(management)));
            when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of());
            when(paymentService.calcularSaldoPending(10)).thenReturn(5000.00f);

            DtoBudgetResumen resumen = budgetResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPending()).isEqualTo(5000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).isEmpty();
            assertThat(resumen.idManagement()).isEqualTo(1);
            assertThat(resumen.numberManagement()).isEqualTo(500);
            assertThat(resumen.encabezadoManagement()).isEqualTo("Gestión Test");
        }

        @Test
        @DisplayName("Presupuesto con un pago reduce el saldo y lo incluye en la lista")
        void shouldReturnReducedBalanceWithOnePayment() {
            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setNumber(500);
            management.setEncabezado("Gestión Test");

            when(budgetRepository.findById(10)).thenReturn(Optional.of(budget));
            when(procedureRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of(procedureFor(management)));
            when(paymentService.findPaymentsByBudget(10)).thenReturn(List.of(paymentOf(1, 2000.00f)));
            when(paymentService.calcularSaldoPending(10)).thenReturn(3000.00f);

            DtoBudgetResumen resumen = budgetResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPending()).isEqualTo(3000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).hasSize(1);
            assertThat(resumen.payments().get(0).idPayment()).isEqualTo(1);
            assertThat(resumen.payments().get(0).amount()).isEqualTo(2000.00f);
            assertThat(resumen.payments().get(0).idBudget()).isEqualTo(10);
        }

        @Test
        @DisplayName("Presupuesto con múltiples payments muestra el saldo neto y todos los payments")
        void shouldReturnNetBalanceWithMultiplePayments() {
            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setNumber(500);
            management.setEncabezado("Gestión Test");

            when(budgetRepository.findById(10)).thenReturn(Optional.of(budget));
            when(procedureRepository.findByFkIdBudgetIdBudget(10)).thenReturn(List.of(procedureFor(management)));
            when(paymentService.findPaymentsByBudget(10))
                    .thenReturn(List.of(paymentOf(1, 2000.00f), paymentOf(2, 1000.00f)));
            when(paymentService.calcularSaldoPending(10)).thenReturn(2000.00f);

            DtoBudgetResumen resumen = budgetResumenService.obtenerResumen(10);

            assertThat(resumen.saldoPending()).isEqualTo(2000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw exception when budget does not exist")
        void shouldThrowExceptionWhenBudgetNotFound() {
            when(budgetRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> budgetResumenService.obtenerResumen(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presupuesto no encontrado");
        }
    }
}
