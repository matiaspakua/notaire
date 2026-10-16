package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.item.ItemController;
import com.licensis.notaire.application.port.in.item.ManageItemsUseCase;
import com.licensis.notaire.application.port.in.item.QueryItemsUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.ListResult;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.SaveResult;
import com.licensis.notaire.dto.TypeItem;

@DisplayName("ItemController unit tests (CU45/CU71)")
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private QueryItemsUseCase queryItems;

    @Mock
    private ManageItemsUseCase manageItems;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ItemController(queryItems, manageItems)).build();
    }

    private ItemView view(Integer id, TypeItem type, String reason) {
        return new ItemView(
                id,
                "Test item",
                new java.math.BigDecimal("1000"),
                null,
                null,
                type,
                reason,
                false,
                null,
                0);
    }

    @Test
    @DisplayName("GET /api/v1/items should return 200 with all items")
    void shouldGetAllItems() throws Exception {
        when(queryItems.findAll()).thenReturn(List.of(view(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} should return 200 when item found")
    void shouldGetItemById() throws Exception {
        when(queryItems.findById(1)).thenReturn(Optional.of(view(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/{id} should return 404 when item not found")
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(queryItems.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/items/budget/{id} should return items for the budget")
    void shouldGetItemsByBudget() throws Exception {
        when(queryItems.findByBudget(10)).thenReturn(List.of(view(1, TypeItem.NORMAL, null)));
        mockMvc.perform(get("/api/v1/items/budget/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idItem").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/items/budget/{id}/discounts-surcharges should return discounts and surcharges")
    void shouldReturnDiscountsAndSurchargesForBudget() throws Exception {
        when(queryItems.findDiscountsAndSurchargesByBudget(10))
                .thenReturn(new ListResult.Ok(List.of(view(1, TypeItem.DESCUENTO, "Discount for early payment"))));

        mockMvc.perform(get("/api/v1/items/budget/10/discounts-surcharges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DESCUENTO"))
                .andExpect(jsonPath("$[0].reason").value("Discount for early payment"));
    }

    @Test
    @DisplayName("GET /api/v1/items/budget/{id}/discounts-surcharges should return empty list when none exist")
    void shouldReturnEmptyListWhenNoDiscountsOrSurcharges() throws Exception {
        when(queryItems.findDiscountsAndSurchargesByBudget(10)).thenReturn(new ListResult.Ok(List.of()));

        mockMvc.perform(get("/api/v1/items/budget/10/discounts-surcharges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/items/budget/{id}/discounts-surcharges should return 404 for an unknown budget")
    void shouldReturn404ForUnknownBudgetOnReport() throws Exception {
        when(queryItems.findDiscountsAndSurchargesByBudget(999)).thenReturn(new ListResult.NotFound());

        mockMvc.perform(get("/api/v1/items/budget/999/discounts-surcharges"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/items should return 201 when item created")
    void shouldCreateItem() throws Exception {
        when(manageItems.create(any())).thenReturn(new SaveResult.Created(view(1, TypeItem.NORMAL, null)));

        String json = """
                {
                    "name": "Test item",
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
        when(manageItems.create(any())).thenReturn(new SaveResult.BadRequest());

        String json = """
                {
                    "name": "Discount without reason",
                    "value": 500.0,
                    "type": "DESCUENTO",
                    "fixedConcept": false
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
        when(manageItems.update(eq(1), any())).thenReturn(new SaveResult.Updated());

        String json = """
                {
                    "name": "Updated item",
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
        when(manageItems.update(eq(999), any())).thenReturn(new SaveResult.NotFound());

        String json = """
                {
                    "name": "Updated item",
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
        when(manageItems.delete(1)).thenReturn(new DeleteResult.Deleted());
        mockMvc.perform(delete("/api/v1/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/items/{id} should return 404 when item not found")
    void shouldReturn404OnDeleteNotFound() throws Exception {
        when(manageItems.delete(999)).thenReturn(new DeleteResult.NotFound());
        mockMvc.perform(delete("/api/v1/items/999"))
                .andExpect(status().isNotFound());
    }
}
