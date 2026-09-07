package com.licensis.notaire.service;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Adds copies of existing catalog Items (CU71) to a real Presupuesto.
 */
@Service
@Transactional
public class PresupuestoCatalogoItemsService {

    private final PresupuestoRepository presupuestoRepository;
    private final ItemRepository itemRepository;

    public PresupuestoCatalogoItemsService(PresupuestoRepository presupuestoRepository,
            ItemRepository itemRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.itemRepository = itemRepository;
    }

    public List<Item> agregarItemsDesdeCatalogo(Integer idPresupuesto, List<Integer> idItems) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con id: " + idPresupuesto));

        List<Item> copias = idItems.stream()
                .map(idItem -> copiarItemDeCatalogo(presupuesto, idItem))
                .toList();

        return itemRepository.saveAll(copias);
    }

    private Item copiarItemDeCatalogo(Presupuesto presupuesto, Integer idItem) {
        Item catalogItem = itemRepository.findById(idItem)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ítem de catálogo no encontrado con id: " + idItem));

        Item copia = new Item();
        copia.setNombre(catalogItem.getNombre());
        copia.setValor(catalogItem.getValor());
        copia.setPorcentaje(catalogItem.getPorcentaje());
        copia.setObservaciones(catalogItem.getObservaciones());
        copia.setFkIdPresupuesto(presupuesto);
        return copia;
    }
}
