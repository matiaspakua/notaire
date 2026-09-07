package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.PresupuestoCatalogoItemsService;
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
class PresupuestoCatalogoItemsServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private PresupuestoCatalogoItemsService presupuestoCatalogoItemsService;

    private Item buildCatalogItem(Integer id, String nombre, float valor) {
        Item item = new Item(id, nombre, valor);
        item.setObservaciones("Observación de catálogo");
        item.setPorcentaje(15);
        return item;
    }

    @Test
    @DisplayName("Should add a copy of a single catalog item to the presupuesto")
    void shouldAddSingleCatalogItem() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(itemRepository.findById(10)).thenReturn(Optional.of(buildCatalogItem(10, "Sellado", 500f)));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> result = presupuestoCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(10));

        assertThat(result).hasSize(1);
        Item copia = result.get(0);
        assertThat(copia.getNombre()).isEqualTo("Sellado");
        assertThat(copia.getValor()).isEqualTo(500f);
        assertThat(copia.getObservaciones()).isEqualTo("Observación de catálogo");
        assertThat(copia.getFkIdPresupuesto()).isEqualTo(presupuesto);
    }

    @Test
    @DisplayName("Should add a copy of each catalog item in a single operation")
    void shouldAddMultipleCatalogItems() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(itemRepository.findById(10)).thenReturn(Optional.of(buildCatalogItem(10, "Sellado", 500f)));
        when(itemRepository.findById(11)).thenReturn(Optional.of(buildCatalogItem(11, "Honorarios", 1000f)));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> result = presupuestoCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(10, 11));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Item::getNombre).containsExactlyInAnyOrder("Sellado", "Honorarios");
    }

    @Test
    @DisplayName("Should reject when a referenced catalog item does not exist")
    void shouldRejectUnknownCatalogItem() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> presupuestoCatalogoItemsService.agregarItemsDesdeCatalogo(1, List.of(999)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should reject when the presupuesto does not exist")
    void shouldRejectWhenPresupuestoDoesNotExist() {
        when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> presupuestoCatalogoItemsService.agregarItemsDesdeCatalogo(999, List.of(10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
