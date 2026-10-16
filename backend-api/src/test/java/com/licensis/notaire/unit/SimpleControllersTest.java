package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.copy.CopyController;
import com.licensis.notaire.adapter.in.web.deed.DeedController;
import com.licensis.notaire.adapter.in.web.history.HistoryController;
import com.licensis.notaire.adapter.in.web.testimony.TestimonyMovementController;
import com.licensis.notaire.adapter.in.web.person.PersonController;
import com.licensis.notaire.adapter.in.web.budget.BudgetController;
import com.licensis.notaire.adapter.in.web.testimony.TestimonyController;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureTypeController;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureController;
import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoProcedureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU07", "CU08", "CU09", "CU10", "CU11", "CU12", "CU14", "CU22", "CU56"})
@DisplayName("Simple Controller unit tests")
class SimpleControllersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("CopyController")
    class CopyControllerTests {
        private final com.licensis.notaire.application.port.in.copy.QueryCopiesUseCase query =
                mock(com.licensis.notaire.application.port.in.copy.QueryCopiesUseCase.class);
        private final com.licensis.notaire.application.port.in.copy.ManageCopiesUseCase manage =
                mock(com.licensis.notaire.application.port.in.copy.ManageCopiesUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new CopyController(query, manage))
                        .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                        .build();

        private static com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyView view(int id) {
            return new com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyView(
                    id, 1, null, null, "n", null, null, 0);
        }

        @Test
        @DisplayName("GET all should return 200")
        void getAll() throws Exception {
            when(query.findAll()).thenReturn(List.of(view(1)));
            mvc.perform(get("/api/v1/copies")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET by id should return 200 when found and 404 when not")
        void getById() throws Exception {
            when(query.findById(1)).thenReturn(Optional.of(view(1)));
            when(query.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/copies/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/copies/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST should return 201 on success")
        void create() throws Exception {
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult.Created(view(1)));
            String body = "{\"number\":1,\"datePrinting\":\"2026-10-09\",\"notes\":\"n\"}";
            mvc.perform(post("/api/v1/copies").contentType("application/json")
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST should return 500 on save failure (#916)")
        void createErrorOnSave() throws Exception {
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult.ServerError());
            String body = "{\"number\":1,\"datePrinting\":\"2026-10-09\",\"notes\":\"n\"}";
            mvc.perform(post("/api/v1/copies").contentType("application/json")
                            .content(body))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("PUT should return 200 when present, 404 when missing")
        void update() throws Exception {
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult.NotFound());
            String body = "{\"number\":1,\"datePrinting\":\"2026-10-09\",\"notes\":\"n\"}";
            mvc.perform(put("/api/v1/copies/1").contentType("application/json")
                    .content(body)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/copies/2").contentType("application/json")
                    .content(body)).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT should return 500 on save failure (#916)")
        void updateErrorOnSave() throws Exception {
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.SaveResult.ServerError());
            String body = "{\"number\":1,\"datePrinting\":\"2026-10-09\",\"notes\":\"n\"}";
            mvc.perform(put("/api/v1/copies/1").contentType("application/json")
                    .content(body)).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("DELETE should return 200 when present, 404 when not, 409 on failure")
        void deleteCopy() throws Exception {
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult.Deleted(),
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult.Conflict());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.copy.CopyOpsPort.DeleteResult.NotFound());
            mvc.perform(delete("/api/v1/copies/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/copies/2")).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/copies/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("HistoryController")
    class HistoryControllerTests {
        private final com.licensis.notaire.application.port.in.history.QueryHistoriesUseCase query =
                mock(com.licensis.notaire.application.port.in.history.QueryHistoriesUseCase.class);
        private final com.licensis.notaire.application.port.in.history.ManageHistoriesUseCase manage =
                mock(com.licensis.notaire.application.port.in.history.ManageHistoriesUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new HistoryController(query, manage))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                        .build();

        @Test
        @DisplayName("GET all and by id and by management should work")
        void getEndpoints() throws Exception {
            var summary = new com.licensis.notaire.dto.DtoHistorySummary(1, null, null, null, null, null);
            when(query.findAll(anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.history.HistoryOpsPort.PageResult(
                            List.of(summary), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(summary));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.findByManagementId(10)).thenReturn(List.of(summary));
            mvc.perform(get("/api/v1/history")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/history/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/history/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/history/management/10")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST/PUT/DELETE should cover happy paths")
        void writeEndpoints() throws Exception {
            String body = "{\"notes\":\"n\",\"managementStatusId\":1,\"managementId\":2}";
            var summary = new com.licensis.notaire.dto.DtoHistorySummary(1, null, "n", 2, 1, null);
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult.Created(summary));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult.NotFound());

            mvc.perform(post("/api/v1/history").contentType("application/json")
                    .content(body)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/history/1").contentType("application/json")
                    .content(body)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/history/2").contentType("application/json")
                    .content(body)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/history/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/history/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST/PUT/DELETE error paths without polluting happy stubs (#916)")
        void writeEndpointsErrorPaths() throws Exception {
            String body = "{\"notes\":\"n\",\"managementStatusId\":1,\"managementId\":2}";
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult.ServerError());
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.SaveResult.ServerError());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.history.HistoryOpsPort.DeleteResult.Conflict());

            mvc.perform(post("/api/v1/history").contentType("application/json")
                    .content(body)).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/history/1").contentType("application/json")
                    .content(body)).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/history/1")).andExpect(status().isConflict());
        }
    }

    // ItemController tests moved to dedicated ItemControllerTest (Issue #822),
    // since ItemController now depends on ItemService rather than ItemRepository.

    @Nested
    @DisplayName("TestimonyMovementController")
    class TestimonyMovementControllerTests {
        private final com.licensis.notaire.application.port.in.testimony.QueryTestimonyMovementsUseCase query =
                mock(com.licensis.notaire.application.port.in.testimony.QueryTestimonyMovementsUseCase.class);
        private final com.licensis.notaire.application.port.in.testimony.ManageTestimonyMovementsUseCase manage =
                mock(com.licensis.notaire.application.port.in.testimony.ManageTestimonyMovementsUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TestimonyMovementController(query, manage)).build();

        private DtoTestimonyMovement buildDto() {
            DtoTestimonyMovement dto = new DtoTestimonyMovement();
            dto.setIdTestimonyMovement(1);
            return dto;
        }

        @Test
        @DisplayName("GET endpoints should work")
        void getEndpoints() throws Exception {
            when(query.findAll()).thenReturn(List.of(buildDto()));
            when(query.findById(1)).thenReturn(Optional.of(buildDto()));
            when(query.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/testimony-movements")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimony-movements/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimony-movements/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST/PUT/DELETE happy paths")
        void writeEndpoints() throws Exception {
            DtoTestimonyMovement dto = buildDto();
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult.Created(
                            dto));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult.NotFound());

            mvc.perform(post("/api/v1/testimony-movements").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/testimony-movements/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/testimony-movements/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/testimony-movements/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/testimony-movements/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST/PUT/DELETE error paths without polluting happy stubs (#916)")
        void writeEndpointsErrorPaths() throws Exception {
            DtoTestimonyMovement dto = buildDto();
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult.Conflict(
                            "fk"));

            mvc.perform(post("/api/v1/testimony-movements").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/testimony-movements/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/testimony-movements/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DeedController")
    class DeedControllerTests {
        private final com.licensis.notaire.application.port.in.deed.QueryDeedsUseCase query =
                mock(com.licensis.notaire.application.port.in.deed.QueryDeedsUseCase.class);
        private final com.licensis.notaire.application.port.in.deed.ManageDeedsUseCase manage =
                mock(com.licensis.notaire.application.port.in.deed.ManageDeedsUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new DeedController(query, manage))
                        .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        private static com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView view(
                int id, String status, int number) {
            return new com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView(
                    id, number, "x", status, null, null, null, null, 0);
        }

        @Test
        @DisplayName("Should cover happy paths")
        void allPaths() throws Exception {
            var e = view(1, "BORRADOR", 10);
            // GET /deeds without ?status= asks for every status (#1340).
            when(query.findAll(isNull(), anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.deed.DeedOpsPort.PageResult(
                            List.of(e), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(e));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.findAvailableNotaries()).thenReturn(List.of());
            when(query.searchByNumber(any())).thenReturn(List.of(e));
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult.Created(e));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult.Updated(e));
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeleteResult.NotFound());

            mvc.perform(get("/api/v1/deeds")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/deeds/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/deeds/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/deeds/available-notaries")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/deeds/search?number=10")).andExpect(status().isOk());

            String deedBody = "{\"number\":10,\"status\":\"BORRADOR\",\"body\":\"x\"}";
            mvc.perform(post("/api/v1/deeds").contentType("application/json")
                    .content(deedBody)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/deeds/1").contentType("application/json")
                    .content(deedBody)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/deeds/2").contentType("application/json")
                    .content(deedBody)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/deeds/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/deeds/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 500 when deed save throws (#916)")
        void allPathsErrorOnSave() throws Exception {
            when(manage.create(any())).thenThrow(new RuntimeException("x"));
            String deedBody = "{\"number\":10,\"status\":\"BORRADOR\",\"body\":\"x\"}";
            mvc.perform(post("/api/v1/deeds").contentType("application/json")
                    .content(deedBody)).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should map Sin Firmar when create omits status (CU05 / #1124 fallout)")
        void shouldDefaultStatusWhenOmittedOnCreate() throws Exception {
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult.Created(
                            view(42, "Sin Firmar", 55)));

            mvc.perform(post("/api/v1/deeds").contentType("application/json")
                            .content("""
                                    {"number":55,"dateDeedrecording":"2026-08-05","notes":"seed","idFolio":1}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("Sin Firmar"));
        }
    }

    @Nested
    @DisplayName("BudgetController")
    class BudgetControllerTests {
        private final com.licensis.notaire.application.port.in.budget.QueryBudgetsUseCase query =
                mock(com.licensis.notaire.application.port.in.budget.QueryBudgetsUseCase.class);
        private final com.licensis.notaire.application.port.in.budget.ManageBudgetsUseCase manage =
                mock(com.licensis.notaire.application.port.in.budget.ManageBudgetsUseCase.class);
        private final com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase budgetSummaryUseCase =
                mock(com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase.class);
        private final com.licensis.notaire.application.usecase.budget.BudgetTemplateService budgetTemplateService =
                mock(com.licensis.notaire.application.usecase.budget.BudgetTemplateService.class);
        private final com.licensis.notaire.application.usecase.budget.BudgetCatalogItemsService budgetCatalogItemsService =
                mock(com.licensis.notaire.application.usecase.budget.BudgetCatalogItemsService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new BudgetController(query, manage, budgetSummaryUseCase,
                        budgetTemplateService, budgetCatalogItemsService))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        private static com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView view(
                int id, String status, String heading, int number,
                com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PersonView person) {
            return new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.BudgetView(
                    id, number, null, heading, status, null, null, person, 0);
        }

        @Test
        @DisplayName("Should cover all paths")
        void allPaths() throws Exception {
            var p = view(1, "Pending", "Test", 1, null);
            when(query.findAll(anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PageResult(
                            List.of(p), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(p));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.findByPerson(5)).thenReturn(List.of(p));
            when(query.findByStatus(any())).thenReturn(List.of(p));
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult.Created(p));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult.Updated(p));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult.Updated(p),
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.DeleteResult.Deleted());
            when(manage.delete(99)).thenReturn(
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.DeleteResult.NotFound());

            mvc.perform(get("/api/v1/budgets")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/budgets/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/budgets/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/budgets/person/5")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/budgets/search?status=PENDIENTE")).andExpect(status().isOk());

            String budgetBody = """
                    {"number":1,"heading":"Test","status":"PENDIENTE","propertyAmount":10.0}
                    """;
            mvc.perform(post("/api/v1/budgets").contentType("application/json")
                    .content(budgetBody)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/budgets/1").contentType("application/json")
                    .content(budgetBody)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/budgets/1").contentType("application/json")
                    .content(budgetBody)).andExpect(status().isNotFound());

            mvc.perform(delete("/api/v1/budgets/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/budgets/99")).andExpect(status().isNotFound());

            var itemView = new com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView(
                    1,
                    "Stamp duty",
                    new java.math.BigDecimal("500"),
                    null,
                    null,
                    null,
                    null,
                    false,
                    1,
                    0);
            when(budgetTemplateService.loadItemsFromTemplate(1, 5)).thenReturn(List.of(itemView));
            mvc.perform(post("/api/v1/budgets/1/items-from-template?procedureTypeId=5"))
                    .andExpect(status().isOk());

            when(budgetCatalogItemsService.addItemsFromCatalog(eq(1), anyList()))
                    .thenReturn(List.of(itemView));
            mvc.perform(post("/api/v1/budgets/1/items-from-catalog").contentType("application/json")
                    .content(mapper.writeValueAsString(List.of(1))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should expose person name and lastName on BudgetResponse (DtoPerson)")
        void shouldExposePersonNameAndLastNameOnGetById() throws Exception {
            when(query.findById(7)).thenReturn(Optional.of(view(
                    7, "BORRADOR", "With client", 7,
                    new com.licensis.notaire.application.port.out.budget.BudgetOpsPort.PersonView(
                            42, "Ana", "García"))));

            mvc.perform(get("/api/v1/budgets/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person.personId").value(42))
                    .andExpect(jsonPath("$.person.name").value("Ana"))
                    .andExpect(jsonPath("$.person.lastName").value("García"));
        }
    }

    @Nested
    @DisplayName("TestimonyController")
    class TestimonyControllerTests {
        private final com.licensis.notaire.application.port.in.testimony.QueryTestimoniesUseCase query =
                mock(com.licensis.notaire.application.port.in.testimony.QueryTestimoniesUseCase.class);
        private final com.licensis.notaire.application.port.in.testimony.ManageTestimoniesUseCase manage =
                mock(com.licensis.notaire.application.port.in.testimony.ManageTestimoniesUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TestimonyController(query, manage)).build();

        @Test
        @DisplayName("Cover happy paths")
        void all() throws Exception {
            DtoTestimony dto = new DtoTestimony();
            dto.setIdTestimony(1);
            com.licensis.notaire.dto.DtoDeed deed = new com.licensis.notaire.dto.DtoDeed();
            deed.setIdDeed(5);
            dto.setDeed(deed);
            when(query.findAll()).thenReturn(List.of(dto));
            when(query.findById(1)).thenReturn(Optional.of(dto));
            when(query.findById(2)).thenReturn(Optional.empty());

            mvc.perform(get("/api/v1/testimonies")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonies/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonies/2")).andExpect(status().isNotFound());

            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult.Created(dto));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult.Deleted(),
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult.InUse(
                            com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.IN_USE_MESSAGE));
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult.NotFound());

            mvc.perform(post("/api/v1/testimonies").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/testimonies/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/testimonies/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/testimonies/1")).andExpect(status().isNoContent());
            // #1364: a testimony with registry movements is kept (409 with the reason)
            mvc.perform(delete("/api/v1/testimonies/1")).andExpect(status().isConflict());
            mvc.perform(delete("/api/v1/testimonies/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Cover error paths without polluting happy stubs (#916)")
        void allErrorPaths() throws Exception {
            DtoTestimony dto = new DtoTestimony();
            dto.setIdTestimony(1);
            com.licensis.notaire.dto.DtoDeed deed = new com.licensis.notaire.dto.DtoDeed();
            deed.setIdDeed(5);
            dto.setDeed(deed);
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult.Conflict(
                            "Cannot delete: the testimony is referenced by other records."));
            mvc.perform(post("/api/v1/testimonies").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/testimonies/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/testimonies/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("ProcedureTypeController")
    class ProcedureTypeControllerTests {
        private final com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase query =
                mock(com.licensis.notaire.application.port.in.procedure.QueryProcedureTypesUseCase.class);
        private final com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase manage =
                mock(com.licensis.notaire.application.port.in.procedure.ManageProcedureTypesUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new ProcedureTypeController(query, manage)).build();

        @Test
        @DisplayName("Cover happy paths")
        void all() throws Exception {
            DtoProcedureType dto = new DtoProcedureType();
            dto.setIdProcedureType(1);
            dto.setName("Compraventa");
            dto.setIsArchived(false);
            dto.setIsRegistered(false);
            dto.setAssociatesProperties(false);
            dto.setEnabled(true);
            dto.setVersion(0);

            when(query.findAll()).thenReturn(List.of(dto));
            when(query.findById(1)).thenReturn(Optional.of(dto));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult.Created(dto));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult.NotFound());

            mvc.perform(get("/api/v1/procedure-types")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/procedure-types/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/procedure-types/2")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/procedure-types").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/procedure-types/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/procedure-types/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/procedure-types/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/procedure-types/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Cover error paths without polluting happy stubs (#916)")
        void allErrorPaths() throws Exception {
            DtoProcedureType dto = new DtoProcedureType();
            dto.setIdProcedureType(1);
            dto.setName("Compraventa");
            dto.setIsArchived(false);
            dto.setIsRegistered(false);
            dto.setAssociatesProperties(false);
            dto.setEnabled(true);
            dto.setVersion(0);
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.SaveResult.Failed(
                            new RuntimeException("x")));
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureTypeOpsPort.DeleteResult.InUse(
                            "in use"));
            mvc.perform(post("/api/v1/procedure-types").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/procedure-types/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/procedure-types/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("ProcedureController")
    class ProcedureControllerTests {
        private final com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase query =
                mock(com.licensis.notaire.application.port.in.procedure.QueryProceduresUseCase.class);
        private final com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase manage =
                mock(com.licensis.notaire.application.port.in.procedure.ManageProceduresUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new ProcedureController(query, manage))
                        .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        private static com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureView view(
                int id) {
            return new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.ProcedureView(
                    id, "n", null, null, null, null, null, 0);
        }

        @Test
        @DisplayName("Cover happy paths")
        void all() throws Exception {
            when(query.findAll(anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.PageResult(
                            List.of(view(1)), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(view(1)));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult.Created(
                            view(1)));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult.Updated(
                            view(1)));
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult.NotFound());

            String requestBody = """
                    {"idProcedureType": 1}
                    """;

            mvc.perform(get("/api/v1/procedures")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/procedures/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/procedures/2")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/procedures").contentType("application/json")
                    .content(requestBody)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/procedures/1").contentType("application/json")
                    .content(requestBody)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/procedures/2").contentType("application/json")
                    .content(requestBody)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/procedures/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/procedures/2")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/procedures").contentType("application/json")
                    .content("{}")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Cover error paths without polluting happy stubs (#916)")
        void allErrorPaths() throws Exception {
            String requestBody = """
                    {"idProcedureType": 1}
                    """;
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult.ServerError());
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.SaveResult.ServerError());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.procedure.ProcedureOpsPort.DeleteResult.Conflict());
            mvc.perform(post("/api/v1/procedures").contentType("application/json")
                    .content(requestBody)).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/procedures/1").contentType("application/json")
                    .content(requestBody)).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/procedures/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PersonController")
    class PersonControllerTests {
        private final com.licensis.notaire.application.port.in.person.QueryPeopleUseCase query =
                mock(com.licensis.notaire.application.port.in.person.QueryPeopleUseCase.class);
        private final com.licensis.notaire.application.port.in.person.ManagePeopleUseCase manage =
                mock(com.licensis.notaire.application.port.in.person.ManagePeopleUseCase.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new PersonController(query, manage))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        private static com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView view(int id) {
            return new com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView(
                    id, "Juan", "Perez", "12345678", true, null, null, null, null, null, null, null, null, null,
                    null, null, 1, 0);
        }

        @Test
        @DisplayName("Cover happy paths including search")
        void all() throws Exception {
            var p = view(1);
            when(query.findAll(anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.person.PersonOpsPort.PageResult(
                            List.of(p), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(p));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.search(any(), any(), any(), any(), any())).thenReturn(List.of(p));
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult.Created(p));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult.Updated(p));
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.DeleteResult.NotFound());

            mvc.perform(get("/api/v1/people")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/people/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/people/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/people/search?firstName=Juan")).andExpect(status().isOk());

            // POST with missing identification type should use default (ports adapter, #1361)
            String personBody = """
                    {"firstName":"Juan","lastName":"Perez","identificationNumber":"12345678","isClient":true}
                    """;
            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(personBody)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/people/1").contentType("application/json")
                    .content(personBody)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/people/2").contentType("application/json")
                    .content(personBody)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/people/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/people/2")).andExpect(status().isNotFound());

            String person2Body = """
                    {"firstName":"Ana","lastName":"Gomez","identificationNumber":"87654321","isClient":false}
                    """;
            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(person2Body)).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Cover person save error path without polluting happy stubs (#916)")
        void allErrorPaths() throws Exception {
            String personBody = """
                    {"firstName":"Juan","lastName":"Perez","identificationNumber":"12345678","isClient":true}
                    """;
            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult.Failed(
                            new RuntimeException("x"), true));
            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(personBody)).andExpect(status().isConflict());
        }
    }
}
