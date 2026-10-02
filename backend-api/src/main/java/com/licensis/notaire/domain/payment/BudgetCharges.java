package com.licensis.notaire.domain.payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything needed to value a budget (CU45): its charge lines, the fallback property
 * amount used when the budget has no lines, and the cost of the documents submitted
 * across its procedures.
 *
 * <p>Framework-free domain value object holding the budget total rule that previously
 * lived inside {@code PaymentService}. The percentage is intentionally compounded over
 * the <em>running</em> total rather than over the line value, preserving the legacy
 * arithmetic order with exact {@link BigDecimal} scale-2 results.
 *
 * @param lines                  charge/discount lines, never {@code null} after construction
 * @param propertyAmount         fallback amount used only when there are no lines
 * @param submittedDocumentCosts total cost of documents submitted in the budget's procedures
 */
public record BudgetCharges(List<ChargeLine> lines, BigDecimal propertyAmount, BigDecimal submittedDocumentCosts) {

    public BudgetCharges {
        lines = lines == null ? List.of() : List.copyOf(lines);
        submittedDocumentCosts = Money.nullToZero(submittedDocumentCosts);
    }

    /**
     * Total value of the budget, including submitted document costs.
     */
    public BigDecimal total() {
        BigDecimal total;
        if (lines.isEmpty()) {
            total = Money.nullToZero(propertyAmount);
        } else {
            total = Money.zero();
            for (ChargeLine line : lines) {
                total = Money.of(total.add(line.signedValue()));
                if (line.hasPercentage()) {
                    BigDecimal surcharge = total
                            .multiply(BigDecimal.valueOf(line.percentage()))
                            .divide(BigDecimal.valueOf(100), Money.SCALE, Money.ROUNDING);
                    total = Money.of(total.add(surcharge));
                }
            }
        }
        return Money.of(total.add(submittedDocumentCosts));
    }

    /**
     * Pending balance of the budget once the given amount has already been paid.
     *
     * @param totalPaid amount already paid; {@code null} is treated as nothing paid
     */
    public BigDecimal pendingBalanceAfter(BigDecimal totalPaid) {
        return Money.of(total().subtract(Money.nullToZero(totalPaid)));
    }
}
