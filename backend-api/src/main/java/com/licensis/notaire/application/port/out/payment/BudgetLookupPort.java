package com.licensis.notaire.application.port.out.payment;

import com.licensis.notaire.domain.payment.BudgetCharges;

import java.util.Optional;

/**
 * Outbound port giving the payment use cases the budget information they need,
 * without exposing the JPA {@code Budget} aggregate.
 *
 * <p>Modelled as two distinct capabilities rather than a generic "find budget":
 * valuing a budget and describing a budget for the CU47 summary are different
 * reads with different costs.
 */
public interface BudgetLookupPort {

    /**
     * Charge lines and fallback amounts needed to value the budget (CU45).
     *
     * @return empty when the budget does not exist
     */
    Optional<BudgetCharges> findCharges(Integer budgetId);

    /**
     * Identity of the budget and of the management it belongs to (CU47).
     *
     * @return empty when the budget does not exist
     */
    Optional<BudgetDescriptor> findDescriptor(Integer budgetId);
}
