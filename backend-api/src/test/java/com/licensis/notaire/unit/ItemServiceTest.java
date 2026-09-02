package com.licensis.notaire.unit;

import com.licensis.notaire.dto.TipoItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
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
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private ItemService itemService;

    private Item buildItem(TipoItem tipo, String motivo) {
        Item item = new Item();
        item.setNombre("Item de prueba");
        item.setValor(1000f);
        item.setTipo(tipo);
        item.setMotivo(motivo);
        return item;
    }

    @Test
    @DisplayName("Should treat an item without explicit type as normal")
    void shouldTreatItemWithoutTypeAsNormal() {
        Item item = new Item();
        item.setNombre("Item sin tipo");
        item.setValor(500f);

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getTipo()).isEqualTo(TipoItem.NORMAL);
    }

    @Test
    @DisplayName("Should accept a discount item with a reason")
    void shouldAcceptDiscountItemWithReason() {
        Item item = buildItem(TipoItem.DESCUENTO, "Descuento por pronto pago");
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getTipo()).isEqualTo(TipoItem.DESCUENTO);
        assertThat(saved.getMotivo()).isEqualTo("Descuento por pronto pago");
    }

    @Test
    @DisplayName("Should accept a surcharge item with a reason")
    void shouldAcceptSurchargeItemWithReason() {
        Item item = buildItem(TipoItem.RECARGO, "Recargo por mora");
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getTipo()).isEqualTo(TipoItem.RECARGO);
        assertThat(saved.getMotivo()).isEqualTo("Recargo por mora");
    }

    @Test
    @DisplayName("Should reject a discount item without a reason")
    void shouldRejectDiscountItemWithoutReason() {
        Item item = buildItem(TipoItem.DESCUENTO, null);

        assertThatThrownBy(() -> itemService.create(item))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should reject a surcharge item without a reason")
    void shouldRejectSurchargeItemWithoutReason() {
        Item item = buildItem(TipoItem.RECARGO, "   ");

        assertThatThrownBy(() -> itemService.create(item))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should accept a normal item without a reason")
    void shouldAcceptNormalItemWithoutReason() {
        Item item = buildItem(TipoItem.NORMAL, null);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item saved = itemService.create(item);

        assertThat(saved.getTipo()).isEqualTo(TipoItem.NORMAL);
    }

    @Test
    @DisplayName("Should reject update when item does not exist")
    void shouldRejectUpdateWhenItemDoesNotExist() {
        when(itemRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.update(999, buildItem(TipoItem.NORMAL, null)))
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
    @DisplayName("Should return only discount and surcharge items for a presupuesto")
    void shouldReturnDiscountsAndSurchargesForPresupuesto() {
        when(presupuestoRepository.existsById(1)).thenReturn(true);
        when(itemRepository.findByFkIdPresupuestoIdPresupuesto(1)).thenReturn(List.of(
                buildItem(TipoItem.NORMAL, null),
                buildItem(TipoItem.DESCUENTO, "Descuento"),
                buildItem(TipoItem.RECARGO, "Recargo")
        ));

        List<Item> result = itemService.findDescuentosYRecargosByPresupuesto(1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Item::getTipo)
                .containsExactlyInAnyOrder(TipoItem.DESCUENTO, TipoItem.RECARGO);
    }

    @Test
    @DisplayName("Should return an empty list when presupuesto has no discounts or surcharges")
    void shouldReturnEmptyListWhenNoDiscountsOrSurcharges() {
        when(presupuestoRepository.existsById(1)).thenReturn(true);
        when(itemRepository.findByFkIdPresupuestoIdPresupuesto(1)).thenReturn(List.of(
                buildItem(TipoItem.NORMAL, null)
        ));

        List<Item> result = itemService.findDescuentosYRecargosByPresupuesto(1);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw when presupuesto does not exist for the discounts/surcharges report")
    void shouldThrowWhenPresupuestoDoesNotExistForReport() {
        when(presupuestoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> itemService.findDescuentosYRecargosByPresupuesto(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
