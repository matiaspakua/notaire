package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.management.ManagementController;
import com.licensis.notaire.adapter.in.web.procedure.ProcedureTemplateController;
import com.licensis.notaire.adapter.in.web.report.ReportController;
import com.licensis.notaire.adapter.in.web.user.UserController;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase.Result;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase.CreateUserCommand;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase.UpdateUserCommand;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.domain.user.UserNotFoundException;
import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateRepositoryPort;
import com.licensis.notaire.application.usecase.report.ReportService;
import com.licensis.notaire.config.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("Additional controllers unit tests")
class AdditionalControllersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("ProcedureTemplateController")
    class ProcedureTemplateControllerTests {

        @Test
        @DisplayName("getAll and getByProcedureType endpoints")
        void all() throws Exception {
            ProcedureTemplateRepositoryPort repo = mock(ProcedureTemplateRepositoryPort.class);
            var mvc = standaloneSetup(new ProcedureTemplateController(repo)).build();
            when(repo.findAll()).thenReturn(List.of(new ProcedureTemplate()));
            when(repo.findByProcedureTypeIdProcedureType(anyInt())).thenReturn(List.of(new ProcedureTemplate()));

            mvc.perform(get("/api/v1/procedure-templates")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/procedure-templates/procedure-types/1")).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("ReportController")
    class ReportControllerTests {

        @Test
        @DisplayName("All endpoints — success and failure paths")
        void all() throws Exception {
            ReportService service = mock(ReportService.class);
            var mvc = standaloneSetup(new ReportController(service))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
            byte[] pdf = "PDF".getBytes();
            when(service.generateBudgetReport(anyInt())).thenReturn(pdf);
            when(service.generateBudgetPropertiesReport(anyInt())).thenReturn(pdf);
            when(service.generateProcedureDocumentsListReport(any())).thenReturn(pdf);
            when(service.generateManagementHistoryReport(anyInt())).thenReturn(pdf);
            when(service.generateDocumentsDueSoonReport(anyInt())).thenReturn(pdf);
            when(service.generateDebtDocumentsReport(anyInt())).thenReturn(pdf);
            when(service.generateIndexBookReport(anyInt(), any(), any())).thenReturn(pdf);
            when(service.generateMonthlyTaxDeclarationReport(anyInt(), anyInt(), any(), any())).thenReturn(pdf);
            when(service.generateIncomeTaxDeclarationReport(anyInt(), anyInt(), any(), any())).thenReturn(pdf);

            mvc.perform(get("/api/v1/reports/budget/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/budget-properties/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/procedure-document-list?procedureTypeName=Compraventa"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/management-history/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/documents-due/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/document-debt?numberManagement=10"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/index-book?year=2024")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/monthly-sworn-statement?year=2024&month=5"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/monthly-sworn-statement?year=2024&month=13"))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/api/v1/reports/tax-sworn-statement?year=2024&month=5"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reports/tax-sworn-statement?year=2024&month=0"))
                    .andExpect(status().isBadRequest());

            // Now all error paths
            when(service.generateBudgetReport(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generateBudgetPropertiesReport(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generateProcedureDocumentsListReport(any())).thenThrow(new RuntimeException("e"));
            when(service.generateManagementHistoryReport(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generateDocumentsDueSoonReport(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generateDebtDocumentsReport(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generateIndexBookReport(anyInt(), any(), any())).thenThrow(new RuntimeException("e"));
            when(service.generateMonthlyTaxDeclarationReport(anyInt(), anyInt(), any(), any())).thenThrow(new RuntimeException("e"));
            when(service.generateIncomeTaxDeclarationReport(anyInt(), anyInt(), any(), any())).thenThrow(new RuntimeException("e"));

            mvc.perform(get("/api/v1/reports/budget/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/budget-properties/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/procedure-document-list?procedureTypeName=Compraventa"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/management-history/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/documents-due/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/document-debt?numberManagement=10"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/index-book?year=2024")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/monthly-sworn-statement?year=2024&month=5"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reports/tax-sworn-statement?year=2024&month=5"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("ManagementController")
    class ManagementControllerTests {

        @Test
        @DisplayName("All paths")
        void all() throws Exception {
            var query = mock(com.licensis.notaire.application.port.in.management.QueryManagementsUseCase.class);
            var manage = mock(com.licensis.notaire.application.port.in.management.ManageManagementsUseCase.class);
            var traceService = mock(com.licensis.notaire.application.usecase.workflow.WorkflowTraceService.class);
            var transitionUseCase = mock(com.licensis.notaire.application.port.in.management.TransitionManagementUseCase.class);
            var transitionWebMapper =
                    mock(com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper.class);
            var externalEntityDocumentService = mock(com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService.class);
            var documentReentryService = mock(com.licensis.notaire.application.usecase.workflow.DocumentReentryService.class);
            var mvc = standaloneSetup(new ManagementController(
                    query,
                    manage,
                    traceService,
                    mock(com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService.class),
                    mock(com.licensis.notaire.application.usecase.management.ManagementFinancialSummaryService.class),
                    transitionUseCase,
                    transitionWebMapper,
                    externalEntityDocumentService,
                    documentReentryService))
                    .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                    .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                    .build();

            var summary = new com.licensis.notaire.dto.DtoManagementSummary(1, 10, "Management", new Date(), null, 0, null);
            var view = new com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementView(
                    1, 10, "Management", new Date(), null, 99, null, null, 0);
            var history = new com.licensis.notaire.dto.DtoHistorySummary(1, new Date(), null, 1, null, null);
            when(query.findAll(anyInt(), anyInt(), any()))
                    .thenReturn(new com.licensis.notaire.application.port.out.management.ManagementOpsPort.PageResult(
                            List.of(summary), 0, 20, 1));
            when(query.findById(1)).thenReturn(Optional.of(summary));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.findByNumber(10)).thenReturn(Optional.of(summary));
            when(query.findByNumber(99)).thenReturn(Optional.empty());
            when(query.findByClientPersonId(5)).thenReturn(List.of(view));
            when(query.findCurrentStatus(1)).thenReturn(Optional.of(history));
            when(query.findCurrentStatus(2)).thenReturn(Optional.empty());
            when(query.findHistory(1)).thenReturn(Optional.of(List.of(history)));
            when(query.findHistory(2)).thenReturn(Optional.empty());

            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult.Created(view));
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult.Updated());
            when(manage.update(eq(2), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult.NotFound());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult.Deleted());
            when(manage.delete(2)).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult.NotFound());

            mvc.perform(get("/api/v1/managements")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/managements/number/10")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/number/99")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/managements/client/5")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/1/current-status")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/2/current-status")).andExpect(status().isNotFound());

            String managementBody = "{\"number\":1,\"heading\":\"Management\"}";
            mvc.perform(post("/api/v1/managements").contentType("application/json")
                    .content(managementBody)).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/managements/1").contentType("application/json")
                    .content(managementBody)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/managements/2").contentType("application/json")
                    .content(managementBody)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/managements/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/managements/2")).andExpect(status().isNotFound());

            var transitionOutput = new com.licensis.notaire.application.port.in.management.TransitionManagementOutput(1);
            when(transitionUseCase.execute(1, "In progress")).thenReturn(transitionOutput);
            when(transitionWebMapper.mapToHttpResponse(transitionOutput))
                    .thenReturn(new com.licensis.notaire.adapter.in.web.management.TransitionManagementWebMapper
                            .ManagementSummaryDto(1, "In progress"));
            when(transitionUseCase.execute(1, "Missing status"))
                    .thenThrow(new com.licensis.notaire.exception.BusinessValidationException(
                            "Transition not allowed"));
            when(transitionUseCase.execute(2, "In progress"))
                    .thenThrow(new com.licensis.notaire.exception.ResourceNotFoundException(
                            "Management not found with ID: 2"));

            mvc.perform(post("/api/v1/managements/1/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"In progress\"}")).andExpect(status().isOk());
            mvc.perform(post("/api/v1/managements/1/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"Missing status\"}")).andExpect(status().isBadRequest());
            mvc.perform(post("/api/v1/managements/2/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"In progress\"}")).andExpect(status().isNotFound());

            mvc.perform(get("/api/v1/managements/1/history")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/managements/2/history")).andExpect(status().isNotFound());

            when(manage.create(any())).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult.ServerError());
            when(manage.update(eq(1), any())).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult.ServerError());
            when(manage.delete(1)).thenReturn(
                    new com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult.Conflict());
            mvc.perform(post("/api/v1/managements").contentType("application/json")
                    .content(managementBody)).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/managements/1").contentType("application/json")
                    .content(managementBody)).andExpect(status().isInternalServerError());
            mvc.perform(delete("/api/v1/managements/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("UserController")
    class UserControllerTests {

        @Test
        @DisplayName("All endpoints")
        void all() throws Exception {
            QueryUsersUseCase query = mock(QueryUsersUseCase.class);
            ManageUsersUseCase manage = mock(ManageUsersUseCase.class);
            AuthenticateUserUseCase authenticate = mock(AuthenticateUserUseCase.class);
            var jwtSvc = mock(com.licensis.notaire.config.JwtTokenService.class);
            when(jwtSvc.generateToken(any())).thenReturn("mock-jwt-token");
            var metrics = mock(com.licensis.notaire.observability.MetricsUtil.class);
            var authCookies = mock(com.licensis.notaire.config.AuthCookieService.class);
            when(authCookies.createSessionCookie(any())).thenReturn(
                    org.springframework.http.ResponseCookie.from("notaire-auth-token", "mock")
                            .httpOnly(true).path("/").sameSite("Lax").build());
            var mvc = standaloneSetup(new UserController(
                            query,
                            manage,
                            authenticate,
                            jwtSvc,
                            metrics,
                            new com.licensis.notaire.security.LoginAttemptService(
                                    5, 900000, new com.licensis.notaire.security.InMemoryLoginAttemptStore()),
                            authCookies,
                            mock(com.licensis.notaire.config.TokenRevocationService.class)))
                    .build();
            User u = User.restore(1, "admin", "abc", true, "Escribano", null, null, null, null, null, 0);

            when(query.findAll()).thenReturn(List.of(u));
            when(query.findById(1)).thenReturn(Optional.of(u));
            when(query.findById(2)).thenReturn(Optional.empty());
            when(query.findByPersonId(10)).thenReturn(Optional.of(u));
            when(query.findByPersonId(99)).thenReturn(Optional.empty());

            mvc.perform(get("/api/v1/users")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/users/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/users/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/users/person/10")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/users/person/99")).andExpect(status().isNotFound());

            String noPwdJson = """
                    {"name":"user","password":"","type":"Notary","active":true}
                    """;
            when(manage.create(any(CreateUserCommand.class))).thenReturn(u);
            mvc.perform(post("/api/v1/users").contentType("application/json")
                    .content(noPwdJson)).andExpect(status().isCreated());
            String withPwdJson = """
                    {"name":"user","password":"pwd","type":"Notary","active":true}
                    """;
            mvc.perform(post("/api/v1/users").contentType("application/json")
                    .content(withPwdJson)).andExpect(status().isCreated());

            String updateJson = """
                    {"name":"admin","password":"","type":"Notary","active":true}
                    """;
            mvc.perform(put("/api/v1/users/1").contentType("application/json")
                    .content(updateJson)).andExpect(status().isOk());
            doThrow(new UserNotFoundException(2)).when(manage).update(eq(2), any(UpdateUserCommand.class));
            mvc.perform(put("/api/v1/users/2").contentType("application/json")
                    .content(updateJson)).andExpect(status().isNotFound());

            mvc.perform(delete("/api/v1/users/1")).andExpect(status().isNoContent());
            doThrow(new UserNotFoundException(2)).when(manage).delete(2);
            mvc.perform(delete("/api/v1/users/2")).andExpect(status().isNotFound());

            DtoUser loginDto = new DtoUser();
            loginDto.setName("admin");
            loginDto.setPassword("admin");
            when(authenticate.authenticate(any(), any()))
                    .thenReturn(new Result.Failed(AuthenticateUserUseCase.Failure.NOT_FOUND));
            mvc.perform(post("/api/v1/users/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            loginDto.setPassword("wrong");
            when(authenticate.authenticate(any(), any()))
                    .thenReturn(new Result.Failed(AuthenticateUserUseCase.Failure.BAD_CREDENTIALS));
            mvc.perform(post("/api/v1/users/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User found, valid login — BCrypt only (legacy MD5 verify removed, #1762)
            loginDto.setPassword("admin");
            when(authenticate.authenticate(any(), any())).thenReturn(new Result.Success(u));
            mvc.perform(post("/api/v1/users/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            when(authenticate.authenticate(any(), any()))
                    .thenReturn(new Result.Failed(AuthenticateUserUseCase.Failure.INACTIVE));
            mvc.perform(post("/api/v1/users/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            when(manage.create(any(CreateUserCommand.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/users").contentType("application/json")
                    .content(noPwdJson)).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("x")).when(manage).update(eq(1), any(UpdateUserCommand.class));
            mvc.perform(put("/api/v1/users/1").contentType("application/json")
                    .content(updateJson)).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(manage).delete(1);
            mvc.perform(delete("/api/v1/users/1")).andExpect(status().isConflict());

            when(authenticate.authenticate(any(), any())).thenThrow(new RuntimeException("db down"));
            mvc.perform(post("/api/v1/users/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());
        }
    }
}
