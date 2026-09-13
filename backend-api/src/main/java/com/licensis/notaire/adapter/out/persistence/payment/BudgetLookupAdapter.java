package com.licensis.notaire.adapter.out.persistence.payment;

import com.licensis.notaire.application.port.out.payment.BudgetDescriptor;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.ChargeLine;
import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link BudgetLookupPort} on top of the existing Spring
 * Data JPA repositories.
 *
 * <p>It translates the JPA {@code Budget} aggregate into the framework-free
 * {@link BudgetCharges} domain value object, which is where the {@code TypeItem}
 * classification is collapsed into the domain's discount flag. Lazy collections are
 * walked here, inside the caller's transaction, so nothing lazily initialises later.
 */
@Component
public class BudgetLookupAdapter implements BudgetLookupPort {

    private final BudgetRepository budgetRepository;
    private final ProcedureRepository procedureRepository;

    public BudgetLookupAdapter(BudgetRepository budgetRepository, ProcedureRepository procedureRepository) {
        this.budgetRepository = budgetRepository;
        this.procedureRepository = procedureRepository;
    }

    @Override
    public Optional<BudgetCharges> findCharges(Integer budgetId) {
        return budgetRepository.findById(budgetId).map(BudgetLookupAdapter::toCharges);
    }

    @Override
    public Optional<BudgetDescriptor> findDescriptor(Integer budgetId) {
        return budgetRepository.findById(budgetId).map(budget -> toDescriptor(budget, budgetId));
    }

    private BudgetDescriptor toDescriptor(Budget budget, Integer budgetId) {
        List<Procedure> procedures = procedureRepository.findByFkIdBudgetIdBudget(budgetId);
        DeedManagement management = procedures.isEmpty() ? null : procedures.get(0).getFkIdManagement();

        return new BudgetDescriptor(
                budget.getIdBudget(),
                budget.getNumber(),
                management != null ? management.getIdManagement() : null,
                management != null ? management.getNumber() : null,
                management != null ? management.getEncabezado() : null);
    }

    private static BudgetCharges toCharges(Budget budget) {
        return new BudgetCharges(
                toChargeLines(budget.getItemList()),
                budget.getPropertyAmount(),
                sumSubmittedDocumentCosts(budget));
    }

    private static List<ChargeLine> toChargeLines(List<Item> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new ChargeLine(
                        item.getValue(),
                        item.getPercentage(),
                        item.getType() == TypeItem.DESCUENTO))
                .toList();
    }

    private static float sumSubmittedDocumentCosts(Budget budget) {
        if (budget.getProcedureList() == null) {
            return 0f;
        }
        float total = 0f;
        for (Procedure procedure : budget.getProcedureList()) {
            if (procedure.getSubmittedDocumentList() == null) {
                continue;
            }
            for (SubmittedDocument document : procedure.getSubmittedDocumentList()) {
                if (document.getAmountToPay() != null) {
                    total += document.getAmountToPay();
                }
            }
        }
        return total;
    }
}
