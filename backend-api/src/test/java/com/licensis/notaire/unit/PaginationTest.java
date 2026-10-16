package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.budget.BudgetController;
import com.licensis.notaire.application.port.in.budget.ManageBudgetsUseCase;
import com.licensis.notaire.application.port.in.budget.QueryBudgetsUseCase;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView;
import com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PageResult;
import com.licensis.notaire.application.usecase.budget.BudgetCatalogItemsService;
import com.licensis.notaire.application.usecase.budget.BudgetTemplateService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pagination support on list endpoints")
class PaginationTest {

    @Mock
    private QueryBudgetsUseCase queryBudgets;

    @Mock
    private ManageBudgetsUseCase manageBudgets;

    @Mock
    private GetBudgetSummaryUseCase budgetSummaryUseCase;

    @Mock
    private BudgetTemplateService budgetTemplateService;

    @Mock
    private BudgetCatalogItemsService budgetCatalogItemsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new BudgetController(queryBudgets, manageBudgets, budgetSummaryUseCase,
                        budgetTemplateService, budgetCatalogItemsService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private static BudgetView emptyView() {
        return new BudgetView(null, 0, null, null, null, null, null, null, 0);
    }

    @Test
    @DisplayName("GET /budgets returns page with content and metadata")
    void shouldReturnPagedBudgets() throws Exception {
        when(queryBudgets.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(new PageResult(List.of(emptyView()), 0, 10, 1));

        mockMvc.perform(get("/api/v1/budgets?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /budgets defaults to page=0 size=20 when no params")
    void shouldUseDefaultPaginationParams() throws Exception {
        when(queryBudgets.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(new PageResult(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /budgets with page=1 returns correct page number")
    void shouldReturnCorrectPageNumber() throws Exception {
        when(queryBudgets.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(new PageResult(List.of(), 1, 5, 12));

        mockMvc.perform(get("/api/v1/budgets?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }
}
