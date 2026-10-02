package com.licensis.notaire.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure domain tests for the CU45 budget valuation rule (no Spring, no JPA, no mocks).
 *
 * <p>These pin the exact arithmetic previously embedded in {@code PaymentService}, including
 * its order-dependent percentage compounding, so the hexagonal extraction is provably
 * behaviour-preserving — now with exact {@link BigDecimal} scale-2 money (#1061).
 */
@DisplayName("BudgetCharges domain rules")
class BudgetChargesTest {

    private static BigDecimal money(String value) {
        return Money.of(value);
    }

    @Test
    @DisplayName("Should fall back to the property amount when there are no lines")
    void shouldFallBackToPropertyAmount() {
        BudgetCharges charges = new BudgetCharges(List.of(), money("1500.00"), Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("1500.00"));
    }

    @Test
    @DisplayName("Should treat a null property amount as zero")
    void shouldTreatNullPropertyAmountAsZero() {
        BudgetCharges charges = new BudgetCharges(List.of(), null, Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(Money.zero());
    }

    @Test
    @DisplayName("Should add submitted document costs to the property amount")
    void shouldAddSubmittedDocumentCostsToPropertyAmount() {
        BudgetCharges charges = new BudgetCharges(List.of(), money("1000.00"), money("250.00"));

        assertThat(charges.total()).isEqualByComparingTo(money("1250.00"));
    }

    @Test
    @DisplayName("Should sum normal lines and ignore the property amount")
    void shouldSumNormalLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(money("100.00"), null), ChargeLine.charge(money("250.00"), null)),
                money("9999.00"),
                Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("350.00"));
    }

    @Test
    @DisplayName("Should sum currency cents exactly (10.10 + 20.20 = 30.30) without float error")
    void shouldSumTenthsExactlyWithoutFloatError() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(money("10.10"), null), ChargeLine.charge(money("20.20"), null)),
                null,
                Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("30.30"));
    }

    @Test
    @DisplayName("Should subtract discount lines")
    void shouldSubtractDiscountLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(money("1000.00"), null), ChargeLine.discount(money("200.00"), null)),
                null,
                Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("800.00"));
    }

    @Test
    @DisplayName("Should compound a percentage over the running total, preserving legacy order")
    void shouldCompoundPercentageOverRunningTotal() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(money("1000.00"), 10)), null, Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("1100.00"));
    }

    @Test
    @DisplayName("Should ignore a zero or null percentage")
    void shouldIgnoreZeroOrNullPercentage() {
        BudgetCharges withZero = new BudgetCharges(List.of(ChargeLine.charge(money("500.00"), 0)), null, Money.zero());
        BudgetCharges withNull = new BudgetCharges(List.of(ChargeLine.charge(money("500.00"), null)), null, Money.zero());

        assertThat(withZero.total()).isEqualByComparingTo(money("500.00"));
        assertThat(withNull.total()).isEqualByComparingTo(money("500.00"));
    }

    @Test
    @DisplayName("Should apply percentage compounding cumulatively across lines")
    void shouldApplyPercentageCumulativelyAcrossLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(money("100.00"), null), ChargeLine.charge(money("100.00"), 10)),
                null,
                Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("220.00"));
    }

    @Test
    @DisplayName("Should add submitted document costs on top of the line total")
    void shouldAddSubmittedDocumentCosts() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(money("400.00"), null)), null, money("100.00"));

        assertThat(charges.total()).isEqualByComparingTo(money("500.00"));
    }

    @Test
    @DisplayName("Should not be affected by mutating the source list after construction")
    void shouldCopyLines() {
        List<ChargeLine> source = new ArrayList<>();
        source.add(ChargeLine.charge(money("100.00"), null));
        BudgetCharges charges = new BudgetCharges(source, null, Money.zero());

        source.add(ChargeLine.charge(money("500.00"), null));

        assertThat(charges.total()).isEqualByComparingTo(money("100.00"));
    }

    @Test
    @DisplayName("Should treat a null line list as no lines")
    void shouldTreatNullLinesAsEmpty() {
        BudgetCharges charges = new BudgetCharges(null, money("750.00"), Money.zero());

        assertThat(charges.total()).isEqualByComparingTo(money("750.00"));
    }

    @Test
    @DisplayName("Should subtract the amount already paid to derive the pending balance")
    void shouldDerivePendingBalance() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(money("1000.00"), null)), null, Money.zero());

        assertThat(charges.pendingBalanceAfter(money("250.00"))).isEqualByComparingTo(money("750.00"));
    }

    @Test
    @DisplayName("Should treat a null amount paid as nothing paid")
    void shouldTreatNullTotalPaidAsZero() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(money("1000.00"), null)), null, Money.zero());

        assertThat(charges.pendingBalanceAfter(null)).isEqualByComparingTo(money("1000.00"));
    }
}
