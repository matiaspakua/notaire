package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.BudgetCatalogoItemsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@DisplayName("PresupuestoCatalogoItemsService Tests (CU71)")
@ExtendWith(MockitoExtension.class)
class BudgetCatalogoItemsServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BudgetCatalogoItemsService budgetCatalogoItemsService;

    private Item buildCatalogItem(Integer id, String name, float value) {
        Item item = new Item(id, name, value);
        item.setNotes("Observación de catálogo");
        item.setPercentage(15);
        return item;
    }

    @Test
    @DisplayName("Should add a copy of a single catalog item to the budget")
    void shouldAddSingleCatalogItem() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(10)).thenReturn(Optional.of(buildCatalogItem(10, "Sellado", 500f)));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> result = budgetCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(10));

        assertThat(result).hasSize(1);
        Item copy = result.get(0);
        assertThat(copy.getName()).isEqualTo("Sellado");
        assertThat(copy.getValue()).isEqualTo(500f);
        assertThat(copy.getNotes()).isEqualTo("Observación de catálogo");
        assertThat(copy.getFkIdBudget()).isEqualTo(budget);
    }

    @Test
    @DisplayName("Should add a copy of each catalog item in a single operation")
    void shouldAddMultipleCatalogItems() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(10)).thenReturn(Optional.of(buildCatalogItem(10, "Sellado", 500f)));
        when(itemRepository.findById(11)).thenReturn(Optional.of(buildCatalogItem(11, "Honorarios", 1000f)));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> result = budgetCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(10, 11));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Item::getName).containsExactlyInAnyOrder("Sellado", "Honorarios");
    }

    @Test
    @DisplayName("Should reject when a referenced catalog item does not exist")
    void shouldRejectUnknownCatalogItem() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(999)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should reject when the budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetCatalogoItemsService.agregarItemsDesdeCatalogo(999, List.of(10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
