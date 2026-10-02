package com.licensis.notaire.domain.payment;

import java.math.BigDecimal;

/**
 * A single valued line of a budget (CU45): either a charge that adds to the budget
 * total or a discount that subtracts from it, optionally carrying a percentage
 * surcharge applied over the running total.
 *
 * <p>Framework-free domain value object: it deliberately knows nothing about the JPA
 * {@code Item} entity or the {@code TypeItem} enum. Translating persistence types into
 * this shape is the outbound adapter's responsibility.
 *
 * @param value      absolute value of the line, always positive
 * @param percentage optional percentage surcharge; {@code null} or non-positive means none
 * @param discount   whether the line subtracts from the budget total
 */
public record ChargeLine(BigDecimal value, Integer percentage, boolean discount) {

    public ChargeLine {
        value = Money.nullToZero(value);
    }

    public static ChargeLine charge(BigDecimal value, Integer percentage) {
        return new ChargeLine(value, percentage, false);
    }

    public static ChargeLine discount(BigDecimal value, Integer percentage) {
        return new ChargeLine(value, percentage, true);
    }

    /**
     * Value as it contributes to the budget total: negative for discounts.
     */
    public BigDecimal signedValue() {
        return discount ? value.negate() : value;
    }

    public boolean hasPercentage() {
        return percentage != null && percentage > 0;
    }
}
