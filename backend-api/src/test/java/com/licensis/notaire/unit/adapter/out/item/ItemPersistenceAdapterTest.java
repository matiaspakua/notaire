package com.licensis.notaire.unit.adapter.out.item;

import com.licensis.notaire.adapter.out.persistence.item.ItemPersistenceAdapter;
import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Item Persistence Adapter")
class ItemPersistenceAdapterTest {

    @Mock
    private ItemRepository repository;

    private ItemRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new ItemPersistenceAdapter(repository);
    }

    @Test
    @DisplayName("Should find all items")
    void shouldFindAll() {
        Item item1 = new Item();
        item1.setIdItem(1);
        Item item2 = new Item();
        item2.setIdItem(2);

        when(repository.findAll()).thenReturn(List.of(item1, item2));

        List<Item> result = adapter.findAll();

        assertThat(result).hasSize(2).containsExactly(item1, item2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should find item by ID")
    void shouldFindById() {
        Item item = new Item();
        item.setIdItem(1);

        when(repository.findById(1)).thenReturn(Optional.of(item));

        Optional<Item> result = adapter.findById(1);

        assertThat(result).isPresent().contains(item);
        verify(repository).findById(1);
    }

    @Test
    @DisplayName("Should find items by budget ID")
    void shouldFindByBudgetId() {
        Item item = new Item();
        item.setIdItem(1);

        when(repository.findByFkIdBudgetIdBudget(1)).thenReturn(List.of(item));

        List<Item> result = adapter.findByBudgetId(1);

        assertThat(result).hasSize(1).contains(item);
        verify(repository).findByFkIdBudgetIdBudget(1);
    }

    @Test
    @DisplayName("Should create item")
    void shouldCreate() {
        Item item = new Item();
        Item savedItem = new Item();
        savedItem.setIdItem(1);

        when(repository.save(item)).thenReturn(savedItem);

        Item result = adapter.create(item);

        assertThat(result).isNotNull().isEqualTo(savedItem);
        verify(repository).save(item);
    }

    @Test
    @DisplayName("Should update item")
    void shouldUpdate() {
        Item item = new Item();
        Item updated = new Item();
        updated.setIdItem(1);

        when(repository.save(item)).thenReturn(updated);

        Item result = adapter.update(1, item);

        assertThat(result.getIdItem()).isEqualTo(1);
        verify(repository).save(item);
    }

    @Test
    @DisplayName("Should delete item by ID")
    void shouldDeleteById() {
        adapter.deleteById(1);

        verify(repository).deleteById(1);
    }
}
