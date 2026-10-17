package com.licensis.notaire.adapter.out.persistence.item;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CU45/CU71 - Outbound persistence helper for Item domain operations, including the
 * discount/surcharge classification and its mandatory reason (issue #1904 — Spring Data
 * ItemRepository locally; no entity-speaking application port).
 */
@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final BudgetOpsPort budgetOps;

    public ItemService(ItemRepository itemRepository, BudgetOpsPort budgetOps) {
        this.itemRepository = itemRepository;
        this.budgetOps = budgetOps;
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Item> findById(Integer id) {
        return itemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Item> findByBudget(Integer idBudget) {
        return itemRepository.findByFkIdBudgetIdBudget(idBudget);
    }

    /**
     * CU71 - Query discounts and surcharges: returns the items of type DESCUENTO or RECARGO
     * of a budget, together with its reason.
     */
    @Transactional(readOnly = true)
    public List<Item> findDiscountsAndSurchargesByBudget(Integer idBudget) {
        if (!budgetOps.existsById(idBudget)) {
            throw new ResourceNotFoundException("Budget not found with ID: " + idBudget);
        }
        return itemRepository.findByFkIdBudgetIdBudget(idBudget).stream()
                .filter(item -> item.getType() == TypeItem.DESCUENTO || item.getType() == TypeItem.RECARGO)
                .toList();
    }

    @Transactional
    public Item create(Item item) {
        validateReason(item);
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Integer id, Item item) {
        if (!itemRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Item not found with ID: " + id);
        }
        validateReason(item);
        item.setIdItem(id);
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(Integer id) {
        if (itemRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Item not found with ID: " + id);
        }
        itemRepository.deleteById(id);
        log.info("Item deleted successfully: ID={}", id);
    }

    /**
     * CU45 - A structured reason is required for discounts and surcharges: rejects items of type
     * DESCUENTO or RECARGO when the reason is empty.
     */
    private void validateReason(Item item) {
        TypeItem type = item.getType();
        boolean requiresReason = type == TypeItem.DESCUENTO || type == TypeItem.RECARGO;
        boolean reasonEmpty = item.getReason() == null || item.getReason().isBlank();

        if (requiresReason && reasonEmpty) {
            throw new BusinessValidationException(
                    "The reason is required for items of type " + type);
        }
    }
}
