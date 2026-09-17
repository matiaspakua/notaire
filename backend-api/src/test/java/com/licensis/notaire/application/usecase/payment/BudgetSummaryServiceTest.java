package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.BudgetSummary;
import com.licensis.notaire.application.port.out.payment.BudgetDescriptor;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.ChargeLine;
import com.licensis.notaire.domain.payment.PaymentDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the CU47 budget financial summary use case.
 */
@DisplayName("BudgetSummaryService (CU47)")
class BudgetSummaryServiceTest {

    private static final Integer BUDGET_ID = 1;

    private InMemoryPaymentRepository payments;
    private InMemoryBudgetLookup budgets;
    private BudgetSummaryService budgetSummary;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRepository();
        budgets = new InMemoryBudgetLookup();
        budgetSummary = new BudgetSummaryService(
                budgets,
                new PaymentStatusService(payments, budgets),
                new PaymentQueryService(payments));

        budgets.given(BUDGET_ID, new BudgetCharges(List.of(ChargeLine.charge(10000f, null)), null, 0f));
        budgets.given(BUDGET_ID, new BudgetDescriptor(BUDGET_ID, 55, 7, 99, "Gestión de prueba"));
    }

    @Test
    @DisplayName("Should expose the budget and its management")
    void shouldExposeBudgetAndManagement() {
        BudgetSummary summary = budgetSummary.summary(BUDGET_ID);

        assertThat(summary.budgetId()).isEqualTo(BUDGET_ID);
        assertThat(summary.budgetNumber()).isEqualTo(55);
        assertThat(summary.managementId()).isEqualTo(7);
        assertThat(summary.managementNumber()).isEqualTo(99);
        assertThat(summary.managementHeading()).isEqualTo("Gestión de prueba");
    }

    @Test
    @DisplayName("Should report the full total as pending when nothing was paid")
    void shouldReportFullTotalWhenNothingPaid() {
        BudgetSummary summary = budgetSummary.summary(BUDGET_ID);

        assertThat(summary.total()).isEqualTo(10000f);
        assertThat(summary.pendingBalance()).isEqualTo(10000f);
        assertThat(summary.payments()).isEmpty();
    }

    @Test
    @DisplayName("Should keep total equal to pending balance plus everything paid")
    void shouldKeepTotalConsistentWithPayments() {
        payments.given(new PaymentDetails(null, BUDGET_ID, 2500f, new Date(), null, null));
        payments.given(new PaymentDetails(null, BUDGET_ID, 1500f, new Date(), null, null));

        BudgetSummary summary = budgetSummary.summary(BUDGET_ID);

        assertThat(summary.pendingBalance()).isEqualTo(6000f);
        assertThat(summary.total()).isEqualTo(10000f);
        assertThat(summary.payments()).hasSize(2);
    }

    @Test
    @DisplayName("Should leave management fields null when the budget has no management")
    void shouldAllowMissingManagement() {
        budgets.given(2, new BudgetCharges(List.of(), 100f, 0f));
        budgets.given(2, new BudgetDescriptor(2, 8, null, null, null));

        BudgetSummary summary = budgetSummary.summary(2);

        assertThat(summary.managementId()).isNull();
        assertThat(summary.managementNumber()).isNull();
        assertThat(summary.managementHeading()).isNull();
    }

    @Test
    @DisplayName("Should reject a summary for an unknown budget")
    void shouldRejectUnknownBudget() {
        assertThatThrownBy(() -> budgetSummary.summary(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Presupuesto no encontrado con ID: 999");
    }
}
