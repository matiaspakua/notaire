package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.ManagementController;
import com.licensis.notaire.api.ProcedureTemplateController;
import com.licensis.notaire.api.ReportController;
import com.licensis.notaire.api.UserController;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.service.ReporteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("Additional controllers unit tests")
class AdditionalControllersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("PlantillaTramiteController")
    class ProcedureTemplateControllerTests {

        @Test
        @DisplayName("getAll and getByTipoTramite endpoints")
        void all() throws Exception {
            ProcedureTemplateRepository repo = mock(ProcedureTemplateRepository.class);
            var mvc = standaloneSetup(new ProcedureTemplateController(repo)).build();
            when(repo.findAll()).thenReturn(List.of(new ProcedureTemplate()));
            when(repo.findByProcedureTypeIdProcedureType(anyInt())).thenReturn(List.of(new ProcedureTemplate()));

            mvc.perform(get("/api/v1/plantilla-tramite")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/plantilla-tramite/tipo-tramite/1")).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("ReportController")
    class ReportControllerTests {

        @Test
        @DisplayName("All endpoints — success and failure paths")
        void all() throws Exception {
            ReporteService service = mock(ReporteService.class);
            var mvc = standaloneSetup(new ReportController(service)).build();
            byte[] pdf = "PDF".getBytes();
            when(service.generarReporteBudget(anyInt())).thenReturn(pdf);
            when(service.generarReporteBudgetProperties(anyInt())).thenReturn(pdf);
            when(service.generarReporteListaDocumentsProcedure(any())).thenReturn(pdf);
            when(service.generarReporteHistoryManagement(anyInt())).thenReturn(pdf);
            when(service.generarReporteDocumentsPorVencer(anyInt())).thenReturn(pdf);
            when(service.generarReporteConsultarDebtDocuments(anyInt())).thenReturn(pdf);
            when(service.generarReporteLibroIndice(anyInt())).thenReturn(pdf);
            when(service.generarReporteDeclaracionJuradaMensual(anyInt(), anyInt())).thenReturn(pdf);
            when(service.generarReporteDeclaracionJuradaRentas(anyInt(), anyInt())).thenReturn(pdf);

            mvc.perform(get("/api/v1/reportes/presupuesto/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/lista-documentos-tramite?nombreTipoTramite=Compraventa"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/historial-gestion/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/consultar-deuda-documentos?numberManagement=10"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/libro-indice?anio=2024")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual?anio=2024&mes=5"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual?anio=2024&mes=13"))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas?anio=2024&mes=5"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas?anio=2024&mes=0"))
                    .andExpect(status().isBadRequest());

            // Now all error paths
            when(service.generarReporteBudget(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteBudgetProperties(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteListaDocumentsProcedure(any())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteHistoryManagement(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDocumentsPorVencer(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteConsultarDebtDocuments(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteLibroIndice(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDeclaracionJuradaMensual(anyInt(), anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDeclaracionJuradaRentas(anyInt(), anyInt())).thenThrow(new RuntimeException("e"));

            mvc.perform(get("/api/v1/reportes/presupuesto/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/lista-documentos-tramite?nombreTipoTramite=Compraventa"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/historial-gestion/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/consultar-deuda-documentos?numberManagement=10"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/libro-indice?anio=2024")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual?anio=2024&mes=5"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas?anio=2024&mes=5"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GestionController")
    class ManagementControllerTests {

        @Test
        @DisplayName("All paths")
        void all() throws Exception {
            DeedManagementRepository repo = mock(DeedManagementRepository.class);
            HistoryRepository histRepo = mock(HistoryRepository.class);
            var traceService = mock(com.licensis.notaire.service.WorkflowTraceService.class);
            var queryService = mock(com.licensis.notaire.service.ManagementQueryService.class);
            var transitionService = mock(com.licensis.notaire.service.ManagementTransitionService.class);
            var bitacoraService = mock(com.licensis.notaire.service.ManagementBitacoraService.class);
            var documentEntidadExternaService = mock(com.licensis.notaire.service.DocumentEntidadExternaService.class);
            var reingresoDocumentacionService = mock(com.licensis.notaire.service.ReingresoDocumentacionService.class);
            var mvc = standaloneSetup(new ManagementController(repo, histRepo, traceService, queryService,
                    mock(com.licensis.notaire.repository.PersonRepository.class),
                    mock(com.licensis.notaire.repository.ManagementStatusRepository.class),
                    mock(com.licensis.notaire.repository.BudgetRepository.class),
                    mock(com.licensis.notaire.repository.ProcedureTypeRepository.class),
                    mock(com.licensis.notaire.repository.ProcedureRepository.class),
                    mock(com.licensis.notaire.repository.PropertyRepository.class),
                    mock(com.licensis.notaire.service.ManagementArchiveDebtService.class),
                    mock(com.licensis.notaire.service.ManagementSubstitutionService.class),
                    mock(com.licensis.notaire.service.ManagementResumenFinancieroService.class),
                    bitacoraService, transitionService, documentEntidadExternaService,
                    reingresoDocumentacionService, mock(com.licensis.notaire.service.ProcedureFolderService.class)))
                    .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                    .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                    .build();

            DeedManagement g = new DeedManagement(1);
            com.licensis.notaire.business.Person escr = new com.licensis.notaire.business.Person();
            escr.setPersonId(99);
            g.setFkIdNotaryPerson(escr);
            var summary = new com.licensis.notaire.dto.DtoManagementSummary(1, 10, "Gestion", new Date(), null, 0, null);
            when(queryService.findAll(any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(summary), org.springframework.data.domain.PageRequest.of(0, 20), 1));
            when(queryService.findById(1)).thenReturn(Optional.of(summary));
            when(queryService.findById(2)).thenReturn(Optional.empty());
            when(queryService.findByNumber(10)).thenReturn(Optional.of(summary));
            when(queryService.findByNumber(99)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            History h = new History();
            h.setIdHistory(1);
            h.setDate(new Date());
            when(histRepo.findByFkIdManagementIdManagement(1)).thenReturn(List.of(h));
            when(histRepo.findByFkIdManagementIdManagement(2)).thenReturn(List.of());

            mvc.perform(get("/api/v1/gestiones")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/gestiones/numero/10")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/numero/99")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/gestiones/cliente/5")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/1/estado-actual")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/2/estado-actual")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/gestiones").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/gestiones/1").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/gestiones/2").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/gestiones/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/gestiones/2")).andExpect(status().isNotFound());

            when(transitionService.transition(1, "En Progreso")).thenReturn(g);
            when(transitionService.transition(1, "Estado Inexistente"))
                    .thenThrow(new com.licensis.notaire.exception.BusinessValidationException(
                            "Transición no permitida"));
            when(transitionService.transition(2, "En Progreso"))
                    .thenThrow(new com.licensis.notaire.exception.ResourceNotFoundException(
                            "Gestión no encontrada con ID: 2"));

            mvc.perform(post("/api/v1/gestiones/1/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"En Progreso\"}")).andExpect(status().isOk());
            mvc.perform(post("/api/v1/gestiones/1/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"Estado Inexistente\"}")).andExpect(status().isBadRequest());
            mvc.perform(post("/api/v1/gestiones/2/transition").contentType("application/json")
                    .content("{\"statusDestination\": \"En Progreso\"}")).andExpect(status().isNotFound());

            mvc.perform(get("/api/v1/gestiones/1/historial")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/gestiones/2/historial")).andExpect(status().isNotFound());

            when(repo.save(any(DeedManagement.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/gestiones").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/gestiones/1").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/gestiones/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("UserController")
    class UserControllerTests {

        @Test
        @DisplayName("All endpoints")
        void all() throws Exception {
            UserRepository repo = mock(UserRepository.class);
            var jwtSvc = mock(com.licensis.notaire.config.JwtTokenService.class);
            when(jwtSvc.generateToken(any())).thenReturn("mock-jwt-token");
            var metrics = mock(com.licensis.notaire.observability.MetricsUtil.class);
            var passwordEncoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
            var mvc = standaloneSetup(new UserController(repo, jwtSvc, metrics, passwordEncoder,
                    new com.licensis.notaire.security.LoginAttemptService(5, 900000))).build();
            User u = new User(1, "admin", "abc", true, "Escribano");

            when(repo.findAll()).thenReturn(List.of(u));
            when(repo.findById(1)).thenReturn(Optional.of(u));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.findFirstByFkIdPersonIdPerson(10)).thenReturn(Optional.of(u));
            when(repo.findFirstByFkIdPersonIdPerson(99)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/usuarios")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/usuarios/persona/10")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/persona/99")).andExpect(status().isNotFound());

            // create with no password — uses UserRequest format (active, not estado)
            String noPwdJson = """
                    {"name":"user","password":"","type":"Notary","active":true}
                    """;
            when(repo.save(any(User.class))).thenReturn(u);
            mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(noPwdJson)).andExpect(status().isCreated());
            // create with password
            String withPwdJson = """
                    {"name":"user","password":"pwd","type":"Notary","active":true}
                    """;
            mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(withPwdJson)).andExpect(status().isCreated());

            String updateJson = """
                    {"name":"admin","password":"","type":"Notary","active":true}
                    """;
            mvc.perform(put("/api/v1/usuarios/1").contentType("application/json")
                    .content(updateJson)).andExpect(status().isOk());
            mvc.perform(put("/api/v1/usuarios/2").contentType("application/json")
                    .content(updateJson)).andExpect(status().isNotFound());

            mvc.perform(delete("/api/v1/usuarios/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/usuarios/2")).andExpect(status().isNotFound());

            // Login flow - all branches
            // Empty users list
            when(repo.findAll()).thenReturn(List.of());
            DtoUser loginDto = new DtoUser();
            loginDto.setName("admin");
            loginDto.setPassword("admin");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User found, wrong password
            when(repo.findAll()).thenReturn(List.of(u));
            loginDto.setPassword("wrong");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User found, valid login - the password 'admin' has known MD5
            // 21232f297a57a5a743894a0e4a801fc3 is MD5("admin")
            u.setPassword("21232f297a57a5a743894a0e4a801fc3");
            u.setStatus(true);
            loginDto.setPassword("admin");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User inactive
            u.setStatus(false);
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // Failure paths
            when(repo.save(any(User.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(noPwdJson)).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/usuarios/1").contentType("application/json")
                    .content(updateJson)).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/usuarios/1")).andExpect(status().isConflict());

            // Exception during login should still return 200 with valido=false
            when(repo.findAll()).thenThrow(new RuntimeException("db down"));
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());
        }
    }
}
