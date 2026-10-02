package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.ChargeLine;
import com.licensis.notaire.domain.payment.Money;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the CU15 "Procesar Pago" use case, driven entirely through the ports
 * with in-memory fakes — no Spring context, no JPA, no mocks.
 *
 * <p>These carry over the scenarios previously asserted against {@code PaymentService}:
 * happy path, non-positive amounts, unknown budget and the overpayment rule.
 */
@DisplayName("ProcessPaymentService (CU15)")
class ProcessPaymentServiceTest {

    private static final Integer BUDGET_ID = 1;

    private InMemoryPaymentRepository payments;
    private InMemoryBudgetLookup budgets;
    private ProcessPaymentService processPayment;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRepository();
        budgets = new InMemoryBudgetLookup();
        processPayment = new ProcessPaymentService(payments, budgets);
        givenBudgetWorth(Money.of("10000.00"));
    }

    private void givenBudgetWorth(BigDecimal amount) {
        budgets.given(BUDGET_ID, new BudgetCharges(List.of(ChargeLine.charge(amount, null)), null, Money.zero()));
    }

    private ProcessPaymentCommand command(BigDecimal amount) {
        return new ProcessPaymentCommand(BUDGET_ID, amount, new Date(), "Pago de prueba", "EFECTIVO");
    }

    @Test
    @DisplayName("Should register a payment when the amount fits the pending balance")
    void shouldRegisterPayment() {
        PaymentDetails registered = processPayment.process(command(Money.of("2500.00")));

        assertThat(registered.id()).isNotNull();
        assertThat(registered.amount()).isEqualByComparingTo(Money.of("2500.00"));
        assertThat(registered.budgetId()).isEqualTo(BUDGET_ID);
        assertThat(registered.notes()).isEqualTo("Pago de prueba");
        assertThat(registered.paymentMethod()).isEqualTo("EFECTIVO");
        assertThat(payments.all()).hasSize(1);
    }

    @Test
    @DisplayName("Should default the date to now when none is supplied")
    void shouldDefaultDateWhenMissing() {
        PaymentDetails registered = processPayment.process(
                new ProcessPaymentCommand(BUDGET_ID, Money.of("100.00"), null, null, null));

        assertThat(registered.date()).isNotNull();
    }

    @Test
    @DisplayName("Should allow a payment that exactly settles the pending balance")
    void shouldAllowExactSettlement() {
        PaymentDetails registered = processPayment.process(command(Money.of("10000.00")));

        assertThat(registered.amount()).isEqualByComparingTo(Money.of("10000.00"));
    }

    @Test
    @DisplayName("Should reject a payment that exceeds the pending balance")
    void shouldRejectOverpayment() {
        assertThatThrownBy(() -> processPayment.process(command(Money.of("10001.00"))))
                .isInstanceOf(PendingBalanceExceededException.class)
                .hasMessageContaining("no puede exceder el saldo pendiente");

        assertThat(payments.all()).isEmpty();
    }

    @Test
    @DisplayName("Should reject overpayment when balance is exact tenths (0.1 + 0.2 against 0.3)")
    void shouldRejectOverpaymentWhenBalanceIsExactTenths() {
        givenBudgetWorth(Money.of("0.30"));
        processPayment.process(command(Money.of("0.10")));
        processPayment.process(command(Money.of("0.20")));

        assertThatThrownBy(() -> processPayment.process(command(Money.of("0.01"))))
                .isInstanceOf(PendingBalanceExceededException.class);

        assertThat(payments.all()).hasSize(2);
    }

    @Test
    @DisplayName("Should account for previous payments when checking the pending balance")
    void shouldAccountForPreviousPayments() {
        processPayment.process(command(Money.of("6000.00")));

        assertThatThrownBy(() -> processPayment.process(command(Money.of("4001.00"))))
                .isInstanceOf(PendingBalanceExceededException.class);

        assertThat(processPayment.process(command(Money.of("4000.00"))).amount())
                .isEqualByComparingTo(Money.of("4000.00"));
    }

    @Test
    @DisplayName("Should reject a null amount")
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> processPayment.process(command(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto del pago debe ser mayor a cero");
    }

    @Test
    @DisplayName("Should reject a zero or negative amount")
    void shouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> processPayment.process(command(Money.of("0.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto del pago debe ser mayor a cero");

        assertThatThrownBy(() -> processPayment.process(command(Money.of("-50.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto del pago debe ser mayor a cero");
    }

    @Test
    @DisplayName("Should reject a payment for an unknown budget")
    void shouldRejectUnknownBudget() {
        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(999, Money.of("100.00"), new Date(), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Presupuesto no encontrado con ID: 999");
    }

    @Test
    @DisplayName("Should reject an unknown budget before validating the amount")
    void shouldCheckBudgetBeforeAmount() {
        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(999, Money.of("-1.00"), new Date(), null, null)))
                .hasMessage("Presupuesto no encontrado con ID: 999");
    }
}
