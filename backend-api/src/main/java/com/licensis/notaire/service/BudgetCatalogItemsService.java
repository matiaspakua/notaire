package com.licensis.notaire.service;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adds copies of existing catalog Items (CU71) to a real Presupuesto.
 */
@Service
@Transactional
public class BudgetCatalogItemsService {

    private final BudgetRepository budgetRepository;
    private final ItemRepository itemRepository;

    public BudgetCatalogItemsService(BudgetRepository budgetRepository,
            ItemRepository itemRepository) {
        this.budgetRepository = budgetRepository;
        this.itemRepository = itemRepository;
    }

    public List<Item> addItemsFromCatalog(Integer idBudget, List<Integer> idItems) {
        Budget budget = budgetRepository.findById(idBudget)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con id: " + idBudget));

        List<Item> copies = idItems.stream()
                .map(idItem -> copyItemFromCatalog(budget, idItem))
                .toList();

        return itemRepository.saveAll(copies);
    }

    private Item copyItemFromCatalog(Budget budget, Integer idItem) {
        Item catalogItem = itemRepository.findById(idItem)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ítem de catálogo no encontrado con id: " + idItem));

        Item copy = new Item();
        copy.setName(catalogItem.getName());
        copy.setValue(catalogItem.getValue());
        copy.setPercentage(catalogItem.getPercentage());
        copy.setNotes(catalogItem.getNotes());
        copy.setFkIdBudget(budget);
        return copy;
    }
}
