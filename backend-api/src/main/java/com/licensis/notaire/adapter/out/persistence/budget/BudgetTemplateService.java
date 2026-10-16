package com.licensis.notaire.adapter.out.persistence.budget;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.budget.LoadBudgetItemsFromTemplateUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;

/**
 * Loads the concepts of a ProcedureType's BudgetTemplate as Items of a
 * real Budget (CU39). Outbound adapter implementing the inbound port (issue #1865).
 */
@Service
@Transactional
public class BudgetTemplateService implements LoadBudgetItemsFromTemplateUseCase {

    private final BudgetRepository budgetRepository;
    private final BudgetTemplateRepository budgetTemplateRepository;
    private final ItemRepositoryPort itemRepository;

    public BudgetTemplateService(
            BudgetRepository budgetRepository,
            BudgetTemplateRepository budgetTemplateRepository,
            ItemRepositoryPort itemRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetTemplateRepository = budgetTemplateRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<ItemView> loadItemsFromTemplate(Integer idBudget, Integer typeProcedureId) {
        Budget budget = budgetRepository
                .findById(idBudget)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + idBudget));

        List<BudgetTemplate> templates =
                budgetTemplateRepository.findByProcedureTypeIdProcedureType(typeProcedureId);

        if (templates.isEmpty()) {
            throw new BusinessValidationException("The procedure type does not have template configured");
        }

        List<Item> items =
                templates.stream().map(template -> createItemFromConcept(budget, template.getConcept())).toList();

        return itemRepository.createAll(items).stream().map(BudgetTemplateService::toView).toList();
    }

    private Item createItemFromConcept(Budget budget, Concept concept) {
        Item item = new Item();
        item.setName(concept.getName());
        item.setValue(concept.getValue());
        item.setPercentage(concept.getPercentage());
        item.setFkIdBudget(budget);
        return item;
    }

    private static ItemView toView(Item item) {
        Integer budgetId = item.getFkIdBudget() != null ? item.getFkIdBudget().getIdBudget() : null;
        return new ItemView(
                item.getIdItem(),
                item.getName(),
                item.getValue(),
                item.getPercentage(),
                item.getNotes(),
                item.getType(),
                item.getReason(),
                item.isFixed(),
                budgetId,
                item.getVersion());
    }
}
