package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetTemplateRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.BudgetTemplateService;
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

@DisplayName("PresupuestoPlantillaService Tests (CU39)")
@ExtendWith(MockitoExtension.class)
class BudgetTemplateServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetTemplateRepository budgetTemplateRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BudgetTemplateService budgetTemplateService;

    private BudgetTemplate buildTemplate(String name, float value, int percentage) {
        Concept concept = new Concept();
        concept.setName(name);
        concept.setValue(value);
        concept.setPercentage(percentage);

        BudgetTemplate template = new BudgetTemplate();
        template.setConcept(concept);
        return template;
    }

    @Test
    @DisplayName("Should load one item per concepto of the type de trámite's plantilla")
    void shouldLoadItemsFromTemplate() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of(
                buildTemplate("Honorarios", 1000f, 10),
                buildTemplate("Sellado", 500f, 0)
        ));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> items = budgetTemplateService.cargarItemsDesdeTemplate(1, 5);

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName).containsExactlyInAnyOrder("Honorarios", "Sellado");
        assertThat(items).extracting(Item::getValue).containsExactlyInAnyOrder(1000f, 500f);
        assertThat(items).allMatch(item -> item.getFkIdBudget() == budget);
    }

    @Test
    @DisplayName("Should not recalculate loaded items when the plantilla changes later")
    void shouldNotRecalculateLoadedItemsWhenTemplateChanges() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        BudgetTemplate template = buildTemplate("Honorarios", 1000f, 10);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(5))
                .thenReturn(List.of(template));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> items = budgetTemplateService.cargarItemsDesdeTemplate(1, 5);

        // The item copied the concepto's value at load time; changing the plantilla's
        // concepto afterwards must not retroactively change the already-loaded item.
        template.getConcept().setValue(9999f);

        assertThat(items).extracting(Item::getValue).containsExactly(1000f);
    }

    @Test
    @DisplayName("Should reject loading when the type de trámite has no plantilla configured")
    void shouldRejectWhenNoTemplateForTypeProcedure() {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
        when(budgetTemplateRepository.findByProcedureTypeIdProcedureType(99)).thenReturn(List.of());

        assertThatThrownBy(() -> budgetTemplateService.cargarItemsDesdeTemplate(1, 99))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("plantilla");
    }

    @Test
    @DisplayName("Should reject loading when the budget does not exist")
    void shouldRejectWhenBudgetDoesNotExist() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetTemplateService.cargarItemsDesdeTemplate(999, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
