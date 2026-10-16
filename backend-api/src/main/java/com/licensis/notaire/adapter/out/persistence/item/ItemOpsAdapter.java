package com.licensis.notaire.adapter.out.persistence.item;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.item.ItemOpsPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetRepository;

/**
 * Persistence/ops adapter for budget items. Holds entity/BudgetRepository access previously on
 * {@code ItemController} (issue #1809).
 */
@Component
public class ItemOpsAdapter implements ItemOpsPort {

    private static final Logger log = LoggerFactory.getLogger(ItemOpsAdapter.class);

    private final ItemService itemService;
    private final BudgetRepository budgetRepository;

    public ItemOpsAdapter(ItemService itemService, BudgetRepository budgetRepository) {
        this.itemService = itemService;
        this.budgetRepository = budgetRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> findAll() {
        return itemService.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemView> findById(Integer id) {
        return itemService.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemView> findByBudget(Integer budgetId) {
        return itemService.findByBudget(budgetId).stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ListResult findDiscountsAndSurchargesByBudget(Integer budgetId) {
        try {
            return new ListResult.Ok(
                    itemService.findDiscountsAndSurchargesByBudget(budgetId).stream().map(this::toView).toList());
        } catch (ResourceNotFoundException e) {
            return new ListResult.NotFound();
        }
    }

    @Override
    public SaveResult create(ItemCommand command) {
        try {
            Item entity = new Item();
            applyCommand(entity, command);
            return new SaveResult.Created(toView(itemService.create(entity)));
        } catch (BusinessValidationException e) {
            log.warn("Validation error while creating the item: {}", e.getMessage());
            return new SaveResult.BadRequest();
        } catch (Exception e) {
            log.error("Failed to create item", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public SaveResult update(Integer id, ItemCommand command) {
        try {
            Item entity = itemService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
            applyCommand(entity, command);
            itemService.update(id, entity);
            return new SaveResult.Updated();
        } catch (ResourceNotFoundException e) {
            return new SaveResult.NotFound();
        } catch (BusinessValidationException e) {
            log.warn("Validation error while updating item id {}: {}", id, e.getMessage());
            return new SaveResult.BadRequest();
        } catch (Exception e) {
            log.error("Failed to update item id {}", id, e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        try {
            itemService.delete(id);
            return new DeleteResult.Deleted();
        } catch (ResourceNotFoundException e) {
            return new DeleteResult.NotFound();
        } catch (Exception e) {
            log.error("Failed to delete item id {}", id, e);
            return new DeleteResult.Conflict();
        }
    }

    private void applyCommand(Item item, ItemCommand command) {
        item.setName(command.name());
        item.setValue(command.value());
        item.setPercentage(command.percentage());
        item.setNotes(command.notes());
        if (command.type() != null) {
            item.setType(command.type());
        }
        item.setReason(command.reason());
        if (command.fixedConcept() != null) {
            item.setFixedConcept(command.fixedConcept());
        }
        if (command.budgetId() != null) {
            Budget budget = budgetRepository.findById(command.budgetId()).orElse(null);
            item.setFkIdBudget(budget);
        }
    }

    private ItemView toView(Item item) {
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
