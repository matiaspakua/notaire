package com.licensis.notaire.unit;

import com.licensis.notaire.adapter.in.web.payment.PaymentWebMapper;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.application.port.out.payment.BudgetDescriptor;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.application.usecase.payment.BudgetSummaryService;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.dto.DtoBudgetResumen;
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

/**
 * CU47 financial summary as exposed on the wire.
 *
 * <p>Formerly {@code BudgetResumenServiceTest}: same expectations, now pinned on
 * {@link BudgetSummaryService} plus the inbound-adapter mapping to
 * {@link DtoBudgetResumen}, which is what the REST client actually sees.
 */
@RequirementCoverage({"CU47"})
@DisplayName("PresupuestoResumenService Tests")
@ExtendWith(MockitoExtension.class)
class BudgetSummaryDtoTest {

    private static final int BUDGET_ID = 10;
    private static final int BUDGET_NUMBER = 100;

    @Mock
    private BudgetLookupPort budgets;

    @Mock
    private GetPaymentStatusUseCase paymentStatus;

    @Mock
    private QueryPaymentsUseCase paymentQueries;

    @InjectMocks
    private BudgetSummaryService budgetSummaryService;

    private BudgetDescriptor descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new BudgetDescriptor(BUDGET_ID, BUDGET_NUMBER, 1, 500, "Gestión Test");
    }

    private PaymentDetails paymentOf(Integer idPayment, float amount) {
        return new PaymentDetails(idPayment, BUDGET_ID, amount, new Date(), null, null);
    }

    private DtoBudgetResumen summaryOf(Integer budgetId) {
        return PaymentWebMapper.toDto(budgetSummaryService.summary(budgetId));
    }

    @Nested
    @DisplayName("Resumen financiero de un budget")
    class ObtenerResumenTests {

        @Test
        @DisplayName("Presupuesto sin payments muestra saldo igual al total y lista vacía")
        void shouldReturnFullBalanceWhenNoPayments() {
            when(budgets.findDescriptor(BUDGET_ID)).thenReturn(Optional.of(descriptor));
            when(paymentQueries.findByBudget(BUDGET_ID)).thenReturn(List.of());
            when(paymentStatus.pendingBalance(BUDGET_ID)).thenReturn(5000.00f);

            DtoBudgetResumen resumen = summaryOf(BUDGET_ID);

            assertThat(resumen.pendingBalance()).isEqualTo(5000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).isEmpty();
            assertThat(resumen.idManagement()).isEqualTo(1);
            assertThat(resumen.numberManagement()).isEqualTo(500);
            assertThat(resumen.encabezadoManagement()).isEqualTo("Gestión Test");
        }

        @Test
        @DisplayName("Presupuesto con un pago reduce el saldo y lo incluye en la lista")
        void shouldReturnReducedBalanceWithOnePayment() {
            when(budgets.findDescriptor(BUDGET_ID)).thenReturn(Optional.of(descriptor));
            when(paymentQueries.findByBudget(BUDGET_ID)).thenReturn(List.of(paymentOf(1, 2000.00f)));
            when(paymentStatus.pendingBalance(BUDGET_ID)).thenReturn(3000.00f);

            DtoBudgetResumen resumen = summaryOf(BUDGET_ID);

            assertThat(resumen.pendingBalance()).isEqualTo(3000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).hasSize(1);
            assertThat(resumen.payments().get(0).idPayment()).isEqualTo(1);
            assertThat(resumen.payments().get(0).amount()).isEqualTo(2000.00f);
            assertThat(resumen.payments().get(0).idBudget()).isEqualTo(BUDGET_ID);
        }

        @Test
        @DisplayName("Presupuesto con múltiples payments muestra el saldo neto y todos los payments")
        void shouldReturnNetBalanceWithMultiplePayments() {
            when(budgets.findDescriptor(BUDGET_ID)).thenReturn(Optional.of(descriptor));
            when(paymentQueries.findByBudget(BUDGET_ID))
                    .thenReturn(List.of(paymentOf(1, 2000.00f), paymentOf(2, 1000.00f)));
            when(paymentStatus.pendingBalance(BUDGET_ID)).thenReturn(2000.00f);

            DtoBudgetResumen resumen = summaryOf(BUDGET_ID);

            assertThat(resumen.pendingBalance()).isEqualTo(2000.00f);
            assertThat(resumen.total()).isEqualTo(5000.00f);
            assertThat(resumen.payments()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw exception when budget does not exist")
        void shouldThrowExceptionWhenBudgetNotFound() {
            when(budgets.findDescriptor(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> summaryOf(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presupuesto no encontrado");
        }
    }
}
