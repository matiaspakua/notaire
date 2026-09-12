package com.licensis.notaire.unit;

import com.licensis.notaire.api.BudgetController;
import com.licensis.notaire.service.BudgetCatalogItemsService;
import com.licensis.notaire.service.BudgetTemplateService;
import com.licensis.notaire.service.BudgetResumenService;
import com.licensis.notaire.service.BudgetService;
import com.licensis.notaire.business.Budget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pagination support on list endpoints")
class PaginationTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private BudgetResumenService budgetResumenService;

    @Mock
    private BudgetTemplateService budgetTemplateService;

    @Mock
    private BudgetCatalogItemsService budgetCatalogoItemsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new BudgetController(budgetService, budgetResumenService,
                        budgetTemplateService, budgetCatalogoItemsService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /presupuestos returns page with content and metadata")
    void shouldReturnPagedPresupuestos() throws Exception {
        Page<Budget> page = new PageImpl<>(List.of(new Budget()), PageRequest.of(0, 10), 1);
        when(budgetService.findAllPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/presupuestos?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /presupuestos defaults to page=0 size=20 when no params")
    void shouldUseDefaultPaginationParams() throws Exception {
        Page<Budget> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(budgetService.findAllPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/presupuestos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /presupuestos with page=1 returns correct page number")
    void shouldReturnCorrectPageNumber() throws Exception {
        Page<Budget> page = new PageImpl<>(List.of(), PageRequest.of(1, 5), 12);
        when(budgetService.findAllPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/presupuestos?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }
}
