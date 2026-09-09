package com.licensis.notaire.service;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetRepository;
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

    private final ItemRepository itemRepository;
    private final BudgetRepository budgetRepository;

    public ItemService(ItemRepository itemRepository, BudgetRepository budgetRepository) {
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
        return itemRepository.findByFkIdBudgetIdBudget(idBudget);
    }

    /**
     * CU71 - Consultar descuentos y recargos: devuelve los ítems de tipo DESCUENTO o RECARGO
     * de un presupuesto, junto con su motivo.
     */
    @Transactional(readOnly = true)
    public List<Item> findDescuentosYRecargosByBudget(Integer idBudget) {
        if (!budgetRepository.existsById(idBudget)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + idBudget);
        }
        return itemRepository.findByFkIdBudgetIdBudget(idBudget).stream()
                .filter(item -> item.getType() == TypeItem.DESCUENTO || item.getType() == TypeItem.RECARGO)
                .toList();
    }

    @Transactional
    public Item create(Item item) {
        validarReason(item);
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Integer id, Item item) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item no encontrado con ID: " + id);
        }
        validarReason(item);
        item.setIdItem(id);
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(Integer id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item no encontrado con ID: " + id);
        }
        itemRepository.deleteById(id);
        log.info("Item eliminado exitosamente: ID={}", id);
    }

    /**
     * CU45 - Exigir motivo estructurado en descuentos y recargos: rechaza items de tipo
     * DESCUENTO o RECARGO sin un motivo no vacío.
     */
    private void validarReason(Item item) {
        TypeItem type = item.getType();
        boolean requiereReason = type == TypeItem.DESCUENTO || type == TypeItem.RECARGO;
        boolean reasonVacio = item.getReason() == null || item.getReason().isBlank();

        if (requiereReason && reasonVacio) {
            throw new BusinessValidationException(
                    "El motivo es obligatorio para ítems de tipo " + type);
        }
    }
}
