package com.licensis.notaire.application.usecase.item;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.application.port.out.budget.BudgetRepositoryPort;
import com.licensis.notaire.business.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CU45/CU71 - Service layer for Item domain operations, including the
 * discount/surcharge classification and its mandatory reason.
 */
@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepositoryPort itemRepository;
    private final BudgetRepositoryPort budgetRepository;

    public ItemService(ItemRepositoryPort itemRepository, BudgetRepositoryPort budgetRepository) {
        this.itemRepository = itemRepository;
        this.budgetRepository = budgetRepository;
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
        return itemRepository.findByBudgetId(idBudget);
    }

    /**
     * CU71 - Consultar descuentos y recargos: devuelve los ítems de tipo DESCUENTO o RECARGO
     * de un presupuesto, junto con su motivo.
     */
    @Transactional(readOnly = true)
    public List<Item> findDiscountsAndSurchargesByBudget(Integer idBudget) {
        if (!budgetRepository.existsById(idBudget)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + idBudget);
        }
        return itemRepository.findByBudgetId(idBudget).stream()
                .filter(item -> item.getType() == TypeItem.DESCUENTO || item.getType() == TypeItem.RECARGO)
                .toList();
    }

    @Transactional
    public Item create(Item item) {
        validateReason(item);
        return itemRepository.create(item);
    }

    @Transactional
    public Item update(Integer id, Item item) {
        if (!itemRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Item no encontrado con ID: " + id);
        }
        validateReason(item);
        item.setIdItem(id);
        return itemRepository.update(id, item);
    }

    @Transactional
    public void delete(Integer id) {
        if (itemRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Item no encontrado con ID: " + id);
        }
        itemRepository.deleteById(id);
        log.info("Item eliminado exitosamente: ID={}", id);
    }

    /**
     * CU45 - Exigir motivo estructurado en descuentos y recargos: rechaza items de tipo
     * DESCUENTO o RECARGO sin un motivo no vacío.
     */
    private void validateReason(Item item) {
        TypeItem type = item.getType();
        boolean requiresReason = type == TypeItem.DESCUENTO || type == TypeItem.RECARGO;
        boolean reasonEmpty = item.getReason() == null || item.getReason().isBlank();

        if (requiresReason && reasonEmpty) {
            throw new BusinessValidationException(
                    "El motivo es obligatorio para ítems de tipo " + type);
        }
    }
}
