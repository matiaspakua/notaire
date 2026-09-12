package com.licensis.notaire.domain.payment;

import java.util.List;

/**
 * Everything needed to value a budget (CU45): its charge lines, the fallback property
 * amount used when the budget has no lines, and the cost of the documents submitted
 * across its procedures.
 *
 * <p>Framework-free domain value object holding the budget total rule that previously
 * lived inside {@code PaymentService}. The percentage is intentionally compounded over
 * the <em>running</em> total rather than over the line value, preserving the legacy
 * arithmetic exactly.
 *
 * @param lines                  charge/discount lines, never {@code null} after construction
 * @param propertyAmount         fallback amount used only when there are no lines
 * @param submittedDocumentCosts total cost of documents submitted in the budget's procedures
 */
public record BudgetCharges(List<ChargeLine> lines, Float propertyAmount, float submittedDocumentCosts) {

    public BudgetCharges {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /**
     * Total value of the budget, including submitted document costs.
     */
    public float total() {
        float total;
        if (lines.isEmpty()) {
            total = propertyAmount != null ? propertyAmount : 0f;
        } else {
            total = 0f;
            for (ChargeLine line : lines) {
                total += line.signedValue();
                if (line.hasPercentage()) {
                    total += total * (line.percentage() / 100.0f);
                }
            }
        }
        return total + submittedDocumentCosts;
    }

    /**
     * Pending balance of the budget once the given amount has already been paid.
     *
     * @param totalPaid amount already paid; {@code null} is treated as nothing paid
     */
    public float pendingBalanceAfter(Float totalPaid) {
        return total() - (totalPaid != null ? totalPaid : 0f);
    }
}
