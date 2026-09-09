package com.licensis.notaire.unit;

import com.licensis.notaire.api.ItemController;
import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ItemController unit tests (CU45/CU71)")
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ItemController controller = new ItemController(itemService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Item buildItem(Integer id, TypeItem type, String reason) {
        Item item = new Item(id);
        item.setName("Item de prueba");
        item.setValue(1000f);
        item.setType(type);
        item.setReason(reason);
        return item;
    }

    @Test
    @DisplayName("GET /api/v1/items should return 200 with all items")
    void shouldGetAllItems() throws Exception {
        when(itemService.findAll()).thenReturn(List.of(buildItem(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} should return 200 when item found")
    void shouldGetItemById() throws Exception {
        when(itemService.findById(1)).thenReturn(Optional.of(buildItem(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} should return 404 when item not found")
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(itemService.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/items/presupuesto/{id} should return items for presupuesto")
    void shouldGetItemsByBudget() throws Exception {
        when(itemService.findByBudget(10)).thenReturn(List.of(buildItem(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items/presupuesto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/presupuesto/{id}/descuentos-recargos should return discounts and surcharges")
    void shouldReturnDiscountsAndSurchargesForBudget() throws Exception {
        when(itemService.findDescuentosYRecargosByBudget(10)).thenReturn(List.of(
                buildItem(1, TypeItem.DESCUENTO, "Descuento por pronto pago")
        ));

        mockMvc.perform(get("/api/v1/items/presupuesto/10/descuentos-recargos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DESCUENTO"))
                .andExpect(jsonPath("$[0].reason").value("Descuento por pronto pago"));
    }

    @Test
    @DisplayName("GET /api/v1/items/presupuesto/{id}/descuentos-recargos should return empty list when none exist")
    void shouldReturnEmptyListWhenNoDiscountsOrSurcharges() throws Exception {
        when(itemService.findDescuentosYRecargosByBudget(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/items/presupuesto/10/descuentos-recargos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/items/presupuesto/{id}/descuentos-recargos should return 404 for unknown presupuesto")
    void shouldReturn404ForUnknownBudgetOnReport() throws Exception {
        when(itemService.findDescuentosYRecargosByBudget(999))
                .thenThrow(new ResourceNotFoundException("Presupuesto no encontrado con ID: 999"));

        mockMvc.perform(get("/api/v1/items/presupuesto/999/descuentos-recargos"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/items should return 201 when item created")
    void shouldCreateItem() throws Exception {
        when(itemService.create(any(Item.class))).thenReturn(buildItem(1, TypeItem.NORMAL, null));

        String json = """
                {
                    "name": "Item de prueba",
                    "value": 1000.0
                }
                """;

        mockMvc.perform(post("/api/v1/items")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idItem").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/items should return 400 when a discount item has no reason")
    void shouldReturn400WhenDiscountItemHasNoReason() throws Exception {
        when(itemService.create(any(Item.class)))
                .thenThrow(new BusinessValidationException("El motivo es obligatorio para ítems de type DESCUENTO"));

        String json = """
                {
                    "name": "Descuento sin reason",
                    "value": 500.0,
                    "type": "DESCUENTO"
                }
                """;

        mockMvc.perform(post("/api/v1/items")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/items/{id} should return 200 when updated")
    void shouldUpdateItem() throws Exception {
        when(itemService.update(anyInt(), any(Item.class))).thenReturn(buildItem(1, TypeItem.NORMAL, null));

        String json = """
                {
                    "name": "Item actualizado",
                    "value": 2000.0
                }
                """;

        mockMvc.perform(put("/api/v1/items/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/items/{id} should return 404 when item not found")
    void shouldReturn404OnUpdateNotFound() throws Exception {
        when(itemService.update(anyInt(), any(Item.class)))
                .thenThrow(new ResourceNotFoundException("Item no encontrado con ID: 999"));

        String json = """
                {
                    "name": "Item actualizado",
                    "value": 2000.0
                }
                """;

        mockMvc.perform(put("/api/v1/items/999")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/items/{id} should return 200 when deleted")
    void shouldDeleteItem() throws Exception {
        doNothing().when(itemService).delete(1);
        mockMvc.perform(delete("/api/v1/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/items/{id} should return 404 when item not found")
    void shouldReturn404OnDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Item no encontrado con ID: 999")).when(itemService).delete(999);
        mockMvc.perform(delete("/api/v1/items/999"))
                .andExpect(status().isNotFound());
    }
}
