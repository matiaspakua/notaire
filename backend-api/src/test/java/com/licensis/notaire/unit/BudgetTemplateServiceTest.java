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
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemRepositoryPort;
import com.licensis.notaire.adapter.out.persistence.budget.BudgetTemplateService;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.BudgetTemplateRepository;

@DisplayName("BudgetTemplateService Tests (CU39)")
@ExtendWith(MockitoExtension.class)
class BudgetTemplateServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetTemplateRepository budgetTemplateRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @InjectMocks
    private BudgetTemplateService budgetTemplateService;

    private BudgetTemplate buildTemplate(String name, java.math.BigDecimal value, int percentage) {
        Concept concept = new Concept();
        concept.setName(name);
        concept.setValue(value);
        concept.setPercentage(percentage);

        BudgetTemplate template = new BudgetTemplate();
        template.setConcept(concept);
        return template;
    }

    @Test
    @DisplayName("Should load one item per concept of the type of procedure's template")
    void shouldLoadItemsFromTemplate() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(5))
                .thenReturn(List.of(
                        buildTemplate("Honorarios", new java.math.BigDecimal("1000"), 10),
                        buildTemplate("Stamp duty", new java.math.BigDecimal("500"), 0)));
        when(itemRepository.createAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemView> items = budgetTemplateService.loadItemsFromTemplate(1, 5);

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemView::name).containsExactlyInAnyOrder("Honorarios", "Stamp duty");
        assertThat(items)
                .extracting(ItemView::value)
                .containsExactlyInAnyOrder(new java.math.BigDecimal("1000"), new java.math.BigDecimal("500"));
        assertThat(items).allMatch(item -> Integer.valueOf(1).equals(item.budgetId()));
    }

    @Test
    @DisplayName("Should not recalculate loaded items when the budget template changes later")
    void shouldNotRecalculateLoadedItemsWhenTemplateChanges() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        BudgetTemplate template = buildTemplate("Honorarios", new java.math.BigDecimal("1000"), 10);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of(template));
        when(itemRepository.createAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ItemView> items = budgetTemplateService.loadItemsFromTemplate(1, 5);

        // The item copied the concept's value at load time; changing the template's
        // concept afterwards must not retroactively change the already-loaded item.
        template.getConcept().setValue(new java.math.BigDecimal("9999"));

        assertThat(items).extracting(ItemView::value).containsExactly(new java.math.BigDecimal("1000"));
    }

    @Test
    @DisplayName("Should reject loading when the type of procedure has not template configured")
    void shouldRejectWhenNoTemplateForTypeProcedure() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(99)).thenReturn(List.of());

        assertThatThrownBy(() -> budgetTemplateService.loadItemsFromTemplate(1, 99))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("template");
    }

    @Test
    @DisplayName("Should reject loading when the budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetTemplateService.loadItemsFromTemplate(999, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
