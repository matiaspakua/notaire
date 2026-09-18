package com.licensis.notaire.application.port.out.item;

import com.licensis.notaire.business.Item;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port describing item persistence capabilities application needs.
 *
 * <p>Exposes only operations use cases actually exercise, enabling
 * manipulation Item entities independently JPA repository details.
 */
public interface ItemRepositoryPort {

    List<Item> findAll();

    Optional<Item> findById(Integer id);

    List<Item> findByBudgetId(Integer idBudget);

    Item create(Item item);

    List<Item> createAll(List<Item> items);

    Item update(Integer id, Item item);

    void deleteById(Integer id);
}
