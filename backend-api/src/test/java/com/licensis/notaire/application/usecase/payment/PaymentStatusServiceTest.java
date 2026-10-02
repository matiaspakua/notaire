package com.licensis.notaire.application.usecase.payment;


import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.ChargeLine;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the CU47 pending-balance and payment-status use case, driven through
 * the ports with in-memory fakes.
 */
@DisplayName("PaymentStatusService (CU47)")
class PaymentStatusServiceTest {

    private static final Integer BUDGET_ID = 1;

    private InMemoryPaymentRepository payments;
    private InMemoryBudgetLookup budgets;
    private PaymentStatusService paymentStatus;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRepository();
        budgets = new InMemoryBudgetLookup();
        paymentStatus = new PaymentStatusService(payments, budgets);
        budgets.given(BUDGET_ID, new BudgetCharges(List.of(ChargeLine.charge(new java.math.BigDecimal("10000"), null)), null, new java.math.BigDecimal("0")));
    }

    private void givenPaymentOf(java.math.BigDecimal amount) {
        payments.given(new PaymentDetails(null, BUDGET_ID, amount, new Date(), null, null));
    }

    @Test
    @DisplayName("Should return the full total as pending when nothing was paid")
    void shouldReturnFullTotalWhenNothingPaid() {
        assertThat(paymentStatus.pendingBalance(BUDGET_ID)).isEqualByComparingTo(new java.math.BigDecimal("10000"));
    }

    @Test
    @DisplayName("Should subtract registered payments from the pending balance")
    void shouldSubtractRegisteredPayments() {
        givenPaymentOf(new java.math.BigDecimal("2500"));
        givenPaymentOf(new java.math.BigDecimal("1500"));

        assertThat(paymentStatus.pendingBalance(BUDGET_ID)).isEqualByComparingTo(new java.math.BigDecimal("6000"));
    }

    @Test
    @DisplayName("Should use the property amount when the budget has no charge lines")
    void shouldUsePropertyAmountWhenNoLines() {
        budgets.given(2, new BudgetCharges(List.of(), new java.math.BigDecimal("4200"), new java.math.BigDecimal("0")));

        assertThat(paymentStatus.pendingBalance(2)).isEqualByComparingTo(new java.math.BigDecimal("4200"));
    }

    @Test
    @DisplayName("Should report NoPayments when the budget has no payments")
    void shouldReportNoPayments() {
        assertThat(paymentStatus.status(BUDGET_ID)).isEqualTo(PaymentStatus.NoPayments);
    }

    @Test
    @DisplayName("Should report PARTIAL when a balance is still pending")
    void shouldReportPartial() {
        givenPaymentOf(new java.math.BigDecimal("4000"));

        assertThat(paymentStatus.status(BUDGET_ID)).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    @DisplayName("Should report PAID when the budget is fully settled")
    void shouldReportPaid() {
        givenPaymentOf(new java.math.BigDecimal("10000"));

        assertThat(paymentStatus.status(BUDGET_ID)).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should reject a pending balance query for an unknown budget")
    void shouldRejectUnknownBudgetOnPendingBalance() {
        assertThatThrownBy(() -> paymentStatus.pendingBalance(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Presupuesto no encontrado con ID: 999");
    }

    @Test
    @DisplayName("Should reject a status query for an unknown budget")
    void shouldRejectUnknownBudgetOnStatus() {
        assertThatThrownBy(() -> paymentStatus.status(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Presupuesto no encontrado con ID: 999");
    }
}
