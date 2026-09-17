package com.licensis.notaire.adapter.out.persistence.item;

import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.repository.ItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link ItemRepositoryPort} on top the existing
 * Spring Data JPA ItemRepository.
 *
 * <p>This is the only place where JPA Item entity is accessed within the
 * item slice: every value returned is a detached Item instance.
 */
@Component
public class ItemPersistenceAdapter implements ItemRepositoryPort {

    private final ItemRepository itemRepository;

    public ItemPersistenceAdapter(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Override
    public Optional<Item> findById(Integer id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findByBudgetId(Integer idBudget) {
        return itemRepository.findByFkIdBudgetIdBudget(idBudget);
    }

    @Override
    public Item create(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public List<Item> createAll(List<Item> items) {
        return itemRepository.saveAll(items);
    }

    @Override
    public Item update(Integer id, Item item) {
        item.setIdItem(id);
        return itemRepository.save(item);
    }

    @Override
    public void deleteById(Integer id) {
        itemRepository.deleteById(id);
    }
}
