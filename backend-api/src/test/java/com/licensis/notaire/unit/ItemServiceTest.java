package com.licensis.notaire.unit;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ItemService Tests (CU45/CU71)")
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ItemService itemService;

    private Item buildItem(TypeItem type, String reason) {
        Item item = new Item();
        item.setName("Item de prueba");
        item.setValue(1000f);
        item.setType(type);
        item.setReason(reason);
        return item;
    }

    @Test
    @DisplayName("Should treat an item without explicit type as normal")
    void shouldTreatItemWithoutTypeAsNormal() {
        Item item = new Item();
        item.setName("Item sin type");
        item.setValue(500f);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getType()).isEqualTo(TypeItem.NORMAL);
    }

    @Test
    @DisplayName("Should accept a discount item with a reason")
    void shouldAcceptDiscountItemWithReason() {
        Item item = buildItem(TypeItem.DESCUENTO, "Descuento por pronto pago");
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getType()).isEqualTo(TypeItem.DESCUENTO);
        assertThat(saved.getReason()).isEqualTo("Descuento por pronto pago");
    }

    @Test
    @DisplayName("Should accept a surcharge item with a reason")
    void shouldAcceptSurchargeItemWithReason() {
        Item item = buildItem(TypeItem.RECARGO, "Recargo por mora");
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getType()).isEqualTo(TypeItem.RECARGO);
        assertThat(saved.getReason()).isEqualTo("Recargo por mora");
    }

    @Test
    @DisplayName("Should reject a discount item without a reason")
    void shouldRejectDiscountItemWithoutReason() {
        Item item = buildItem(TypeItem.DESCUENTO, null);

        assertThatThrownBy(() -> itemService.create(item))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should reject a surcharge item without a reason")
    void shouldRejectSurchargeItemWithoutReason() {
        Item item = buildItem(TypeItem.RECARGO, "   ");

        assertThatThrownBy(() -> itemService.create(item))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should accept a normal item without a reason")
    void shouldAcceptNormalItemWithoutReason() {
        Item item = buildItem(TypeItem.NORMAL, null);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getType()).isEqualTo(TypeItem.NORMAL);
    }

    @Test
    @DisplayName("Should reject update when item does not exist")
    void shouldRejectUpdateWhenItemDoesNotExist() {
        when(itemRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.update(999, buildItem(TypeItem.NORMAL, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject deletion when item does not exist")
    void shouldRejectDeletionWhenItemDoesNotExist() {
        when(itemRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return only discount and surcharge items for a budget")
    void shouldReturnDiscountsAndSurchargesForBudget() {
        when(budgetRepository.existsById(1)).thenReturn(true);
        when(itemRepository.findByFkIdBudgetIdBudget(1)).thenReturn(List.of(
                buildItem(TypeItem.NORMAL, null),
                buildItem(TypeItem.DESCUENTO, "Descuento"),
                buildItem(TypeItem.RECARGO, "Recargo")
        ));

        List<Item> result = itemService.findDiscountsAndSurchargesByBudget(1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Item::getType)
                .containsExactlyInAnyOrder(TypeItem.DESCUENTO, TypeItem.RECARGO);
    }

    @Test
    @DisplayName("Should return an empty list when budget has no discounts or surcharges")
    void shouldReturnEmptyListWhenNoDiscountsOrSurcharges() {
        when(budgetRepository.existsById(1)).thenReturn(true);
        when(itemRepository.findByFkIdBudgetIdBudget(1)).thenReturn(List.of(
                buildItem(TypeItem.NORMAL, null)
        ));

        List<Item> result = itemService.findDiscountsAndSurchargesByBudget(1);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw when budget does not exist for the discounts/surcharges report")
    void shouldThrowWhenBudgetDoesNotExistForReport() {
        when(budgetRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.findDiscountsAndSurchargesByBudget(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
