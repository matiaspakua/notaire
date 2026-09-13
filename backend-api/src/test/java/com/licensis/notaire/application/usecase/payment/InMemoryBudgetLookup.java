package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.out.payment.BudgetDescriptor;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.domain.payment.BudgetCharges;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory fake of {@link BudgetLookupPort} for use-case unit tests.
 */
class InMemoryBudgetLookup implements BudgetLookupPort {

    private final Map<Integer, BudgetCharges> charges = new HashMap<>();
    private final Map<Integer, BudgetDescriptor> descriptors = new HashMap<>();

    void given(Integer budgetId, BudgetCharges budgetCharges) {
        charges.put(budgetId, budgetCharges);
    }

    void given(Integer budgetId, BudgetDescriptor descriptor) {
        descriptors.put(budgetId, descriptor);
    }

    @Override
    public Optional<BudgetCharges> findCharges(Integer budgetId) {
        return Optional.ofNullable(charges.get(budgetId));
    }

    @Override
    public Optional<BudgetDescriptor> findDescriptor(Integer budgetId) {
        return Optional.ofNullable(descriptors.get(budgetId));
    }
}
