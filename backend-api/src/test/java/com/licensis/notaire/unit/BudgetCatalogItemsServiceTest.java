package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.adapter.out.persistence.budget.BudgetCatalogItemsService;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.exception.ResourceNotFoundException;

@DisplayName("BudgetCatalogItemsService Tests (CU71)")
@ExtendWith(MockitoExtension.class)
class BudgetCatalogItemsServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BudgetCatalogItemsService budgetCatalogItemsService;

    private Item buildCatalogItem(Integer id, String name, java.math.BigDecimal value) {
        Item item = new Item(id, name, value);
        item.setNotes("Catalog observation");
        item.setPercentage(15);
        return item;
    }

    @Test
    @DisplayName("Should add a copy of a single catalog item to the budget")
    void shouldAddSingleCatalogItem() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(10))
                .thenReturn(Optional.of(buildCatalogItem(10, "Stamp duty", new java.math.BigDecimal("500"))));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemView> result = budgetCatalogItemsService.addItemsFromCatalog(1, List.of(10));

        assertThat(result).hasSize(1);
        ItemView copy = result.get(0);
        assertThat(copy.name()).isEqualTo("Stamp duty");
        assertThat(copy.value()).isEqualByComparingTo(new java.math.BigDecimal("500"));
        assertThat(copy.notes()).isEqualTo("Catalog observation");
        assertThat(copy.budgetId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should add a copy of each catalog item in a single operation")
    void shouldAddMultipleCatalogItems() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(10))
                .thenReturn(Optional.of(buildCatalogItem(10, "Stamp duty", new java.math.BigDecimal("500"))));
        when(itemRepository.findById(11))
                .thenReturn(Optional.of(buildCatalogItem(11, "Honorarios", new java.math.BigDecimal("1000"))));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemView> result = budgetCatalogItemsService.addItemsFromCatalog(1, List.of(10, 11));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemView::name).containsExactlyInAnyOrder("Stamp duty", "Honorarios");
    }

    @Test
    @DisplayName("Should reject when a referenced catalog item does not exist")
    void shouldRejectUnknownCatalogItem() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetCatalogItemsService.addItemsFromCatalog(1, List.of(999)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should reject when the budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetCatalogItemsService.addItemsFromCatalog(999, List.of(10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
