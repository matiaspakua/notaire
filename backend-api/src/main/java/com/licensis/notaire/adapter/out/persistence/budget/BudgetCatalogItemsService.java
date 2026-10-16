package com.licensis.notaire.adapter.out.persistence.budget;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.budget.AddBudgetItemsFromCatalogUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetRepository;

/**
 * Adds copies of existing catalog Items (CU71) to a real Budget.
 * Outbound adapter implementing the inbound port (issue #1865).
 */
@Service
@Transactional
public class BudgetCatalogItemsService implements AddBudgetItemsFromCatalogUseCase {

    private final BudgetRepository budgetRepository;
    private final ItemRepositoryPort itemRepository;

    public BudgetCatalogItemsService(BudgetRepository budgetRepository, ItemRepositoryPort itemRepository) {
        this.budgetRepository = budgetRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<ItemView> addItemsFromCatalog(Integer idBudget, List<Integer> idItems) {
        Budget budget = budgetRepository
                .findById(idBudget)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + idBudget));

        List<Item> copies = idItems.stream().map(idItem -> copyItemFromCatalog(budget, idItem)).toList();

        return itemRepository.createAll(copies).stream().map(BudgetCatalogItemsService::toView).toList();
    }

    private Item copyItemFromCatalog(Budget budget, Integer idItem) {
        Item catalogItem = itemRepository
                .findById(idItem)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog item not found with ID: " + idItem));

        Item copy = new Item();
        copy.setName(catalogItem.getName());
        copy.setValue(catalogItem.getValue());
        copy.setPercentage(catalogItem.getPercentage());
        copy.setNotes(catalogItem.getNotes());
        copy.setFkIdBudget(budget);
        return copy;
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
