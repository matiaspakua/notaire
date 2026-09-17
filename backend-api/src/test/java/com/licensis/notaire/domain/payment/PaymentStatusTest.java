package com.licensis.notaire.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure domain tests for the CU15 / CU47 aggregated payment status rule (Issue #821).
 */
@DisplayName("PaymentStatus domain rules")
class PaymentStatusTest {

    @Test
    @DisplayName("Should report NoPayments when nothing was paid")
    void shouldReportNoPaymentsWhenTotalPaidIsNull() {
        assertThat(PaymentStatus.of(null, 1000.0f)).isEqualTo(PaymentStatus.NoPayments);
    }

    @Test
    @DisplayName("Should report NoPayments when the amount paid is exactly zero")
    void shouldReportNoPaymentsWhenTotalPaidIsZero() {
        assertThat(PaymentStatus.of(0f, 1000.0f)).isEqualTo(PaymentStatus.NoPayments);
    }

    @Test
    @DisplayName("Should report PARTIAL when a balance is still pending")
    void shouldReportPartialWhenBalancePending() {
        assertThat(PaymentStatus.of(400.0f, 600.0f)).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    @DisplayName("Should report PAID when the pending balance reaches zero")
    void shouldReportPaidWhenBalanceIsZero() {
        assertThat(PaymentStatus.of(1000.0f, 0f)).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should report PAID when the budget was overpaid")
    void shouldReportPaidWhenOverpaid() {
        assertThat(PaymentStatus.of(1200.0f, -200.0f)).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should keep the enum constant names that the REST contract serialises")
    void shouldKeepRestContractConstantNames() {
        assertThat(java.util.Arrays.stream(PaymentStatus.values()).map(Enum::name))
                .containsExactly("NoPayments", "PARTIAL", "PAID");
    }
}
