package com.licensis.notaire.service;

import com.licensis.notaire.dto.TipoItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
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
    private final PresupuestoRepository presupuestoRepository;

    public ItemService(ItemRepository itemRepository, PresupuestoRepository presupuestoRepository) {
        this.itemRepository = itemRepository;
        this.presupuestoRepository = presupuestoRepository;
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
    public List<Item> findByPresupuesto(Integer idPresupuesto) {
        return itemRepository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto);
    }

    /**
     * CU71 - Consultar descuentos y recargos: devuelve los ítems de tipo DESCUENTO o RECARGO
     * de un presupuesto, junto con su motivo.
     */
    @Transactional(readOnly = true)
    public List<Item> findDescuentosYRecargosByPresupuesto(Integer idPresupuesto) {
        if (!presupuestoRepository.existsById(idPresupuesto)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + idPresupuesto);
        }
        return itemRepository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto).stream()
                .filter(item -> item.getTipo() == TipoItem.DESCUENTO || item.getTipo() == TipoItem.RECARGO)
                .toList();
    }

    @Transactional
    public Item create(Item item) {
        validarMotivo(item);
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Integer id, Item item) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item no encontrado con ID: " + id);
        }
        validarMotivo(item);
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
    private void validarMotivo(Item item) {
        TipoItem tipo = item.getTipo();
        boolean requiereMotivo = tipo == TipoItem.DESCUENTO || tipo == TipoItem.RECARGO;
        boolean motivoVacio = item.getMotivo() == null || item.getMotivo().isBlank();

        if (requiereMotivo && motivoVacio) {
            throw new BusinessValidationException(
                    "El motivo es obligatorio para ítems de tipo " + tipo);
        }
    }
}
