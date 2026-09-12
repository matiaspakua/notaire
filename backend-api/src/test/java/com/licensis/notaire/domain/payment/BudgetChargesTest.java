package com.licensis.notaire.domain.payment;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure domain tests for the CU45 budget valuation rule (no Spring, no JPA, no mocks).
 *
 * <p>These pin the exact arithmetic previously embedded in {@code PaymentService}, including
 * its order-dependent percentage compounding, so the hexagonal extraction is provably
 * behaviour-preserving.
 */
@DisplayName("BudgetCharges domain rules")
class BudgetChargesTest {

    private static final Offset<Float> TOLERANCE = Offset.offset(0.001f);

    @Test
    @DisplayName("Should fall back to the property amount when there are no lines")
    void shouldFallBackToPropertyAmount() {
        BudgetCharges charges = new BudgetCharges(List.of(), 1500.0f, 0f);

        assertThat(charges.total()).isEqualTo(1500.0f);
    }

    @Test
    @DisplayName("Should treat a null property amount as zero")
    void shouldTreatNullPropertyAmountAsZero() {
        BudgetCharges charges = new BudgetCharges(List.of(), null, 0f);

        assertThat(charges.total()).isEqualTo(0f);
    }

    @Test
    @DisplayName("Should add submitted document costs to the property amount")
    void shouldAddSubmittedDocumentCostsToPropertyAmount() {
        BudgetCharges charges = new BudgetCharges(List.of(), 1000.0f, 250.0f);

        assertThat(charges.total()).isEqualTo(1250.0f);
    }

    @Test
    @DisplayName("Should sum normal lines and ignore the property amount")
    void shouldSumNormalLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(100.0f, null), ChargeLine.charge(250.0f, null)),
                9999.0f,
                0f);

        assertThat(charges.total()).isEqualTo(350.0f);
    }

    @Test
    @DisplayName("Should subtract discount lines")
    void shouldSubtractDiscountLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(1000.0f, null), ChargeLine.discount(200.0f, null)),
                null,
                0f);

        assertThat(charges.total()).isEqualTo(800.0f);
    }

    @Test
    @DisplayName("Should compound a percentage over the running total, preserving legacy order")
    void shouldCompoundPercentageOverRunningTotal() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(1000.0f, 10)), null, 0f);

        assertThat(charges.total()).isCloseTo(1100.0f, TOLERANCE);
    }

    @Test
    @DisplayName("Should ignore a zero or null percentage")
    void shouldIgnoreZeroOrNullPercentage() {
        BudgetCharges withZero = new BudgetCharges(List.of(ChargeLine.charge(500.0f, 0)), null, 0f);
        BudgetCharges withNull = new BudgetCharges(List.of(ChargeLine.charge(500.0f, null)), null, 0f);

        assertThat(withZero.total()).isEqualTo(500.0f);
        assertThat(withNull.total()).isEqualTo(500.0f);
    }

    @Test
    @DisplayName("Should apply percentage compounding cumulatively across lines")
    void shouldApplyPercentageCumulativelyAcrossLines() {
        BudgetCharges charges = new BudgetCharges(
                List.of(ChargeLine.charge(100.0f, null), ChargeLine.charge(100.0f, 10)),
                null,
                0f);

        assertThat(charges.total()).isCloseTo(220.0f, TOLERANCE);
    }

    @Test
    @DisplayName("Should add submitted document costs on top of the line total")
    void shouldAddSubmittedDocumentCosts() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(400.0f, null)), null, 100.0f);

        assertThat(charges.total()).isEqualTo(500.0f);
    }

    @Test
    @DisplayName("Should not be affected by mutating the source list after construction")
    void shouldCopyLines() {
        List<ChargeLine> source = new ArrayList<>();
        source.add(ChargeLine.charge(100.0f, null));
        BudgetCharges charges = new BudgetCharges(source, null, 0f);

        source.add(ChargeLine.charge(500.0f, null));

        assertThat(charges.total()).isEqualTo(100.0f);
    }

    @Test
    @DisplayName("Should treat a null line list as no lines")
    void shouldTreatNullLinesAsEmpty() {
        BudgetCharges charges = new BudgetCharges(null, 750.0f, 0f);

        assertThat(charges.total()).isEqualTo(750.0f);
    }

    @Test
    @DisplayName("Should subtract the amount already paid to derive the pending balance")
    void shouldDerivePendingBalance() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(1000.0f, null)), null, 0f);

        assertThat(charges.pendingBalanceAfter(250.0f)).isEqualTo(750.0f);
    }

    @Test
    @DisplayName("Should treat a null amount paid as nothing paid")
    void shouldTreatNullTotalPaidAsZero() {
        BudgetCharges charges = new BudgetCharges(List.of(ChargeLine.charge(1000.0f, null)), null, 0f);

        assertThat(charges.pendingBalanceAfter(null)).isEqualTo(1000.0f);
    }
}
