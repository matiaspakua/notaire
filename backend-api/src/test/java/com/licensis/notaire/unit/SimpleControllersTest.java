package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.CopyController;
import com.licensis.notaire.api.DeedController;
import com.licensis.notaire.api.ManagementStatusController;
import com.licensis.notaire.api.HistoryController;
import com.licensis.notaire.api.TestimonyMovementController;
import com.licensis.notaire.api.PersonController;
import com.licensis.notaire.api.BudgetController;
import com.licensis.notaire.api.TestimonyController;
import com.licensis.notaire.api.DocumentTypeController;
import com.licensis.notaire.api.FolioTypeController;
import com.licensis.notaire.api.ProcedureTypeController;
import com.licensis.notaire.api.IdentificationTypeController;
import com.licensis.notaire.api.ProcedureController;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoDocumentType;
import com.licensis.notaire.dto.DtoFolioType;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.CopyRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.DeedFirmaService;
import com.licensis.notaire.service.DeedService;
import com.licensis.notaire.service.TestimonyMovementService;
import com.licensis.notaire.service.PersonService;
import com.licensis.notaire.service.BudgetService;
import com.licensis.notaire.service.TestimonyGenerationVerificacionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    @DisplayName("CopiaController")
    class CopyControllerTests {
        private final CopyRepository repo = mock(CopyRepository.class);
        private final TestimonyMovementRepository testimonyMovementRepository = mock(TestimonyMovementRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new CopyController(repo, testimonyMovementRepository)).build();

        @Test
        @DisplayName("GET all should return 200")
        void getAll() throws Exception {
            Copy c = new Copy();
            c.setIdCopy(1);
            when(repo.findAll()).thenReturn(List.of(c));
            mvc.perform(get("/api/v1/copia")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET by id should return 200 when found and 404 when not")
        void getById() throws Exception {
            Copy c = new Copy();
            c.setIdCopy(1);
            when(repo.findById(1)).thenReturn(Optional.of(c));
            when(repo.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/copia/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/copia/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST should return 201 on success and 500 on failure")
        void create() throws Exception {
            Copy c = new Copy();
            mvc.perform(post("/api/v1/copia").contentType("application/json")
                            .content(mapper.writeValueAsString(c)))
                    .andExpect(status().isCreated());
            when(repo.save(any(Copy.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/copia").contentType("application/json")
                            .content(mapper.writeValueAsString(c)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("PUT should return 200 when present, 404 when missing, 500 on failure")
        void update() throws Exception {
            Copy c = new Copy();
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            mvc.perform(put("/api/v1/copia/1").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/copia/2").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isNotFound());
            when(repo.save(any(Copy.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(put("/api/v1/copia/1").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("DELETE should return 200 when present, 404 when not, 409 on failure")
        void deleteCopy() throws Exception {
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            mvc.perform(delete("/api/v1/copia/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/copia/2")).andExpect(status().isNotFound());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/copia/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("EstadoDeGestionController")
    class ManagementStatusControllerTests {
        private final ManagementStatusRepository repo = mock(ManagementStatusRepository.class);
        private final DeedManagementRepository managementRepo = mock(DeedManagementRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new ManagementStatusController(repo, managementRepo)).build();

        private ManagementStatus build() {
            ManagementStatus e = new ManagementStatus(1, "Activo");
            return e;
        }

        @Test
        @DisplayName("GET all should return list of DTOs")
        void getAll() throws Exception {
            when(repo.findAll()).thenReturn(List.of(build()));
            mvc.perform(get("/api/v1/estado-gestion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idManagementStatus").value(1));
        }

        @Test
        @DisplayName("GET by id should return 200 when found and 404 when not")
        void getById() throws Exception {
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/estado-gestion/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/estado-gestion/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST should return 201 when valid and 409 when save fails")
        void create() throws Exception {
            DtoManagementStatus dto = new DtoManagementStatus();
            dto.setIdManagementStatus(1);
            dto.setName("Activo");
            when(repo.save(any(ManagementStatus.class))).thenReturn(build());
            mvc.perform(post("/api/v1/estado-gestion").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            when(repo.save(any(ManagementStatus.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/estado-gestion").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT should return 200 when present and not in-use, 404 when missing, 409 when in-use")
        void update() throws Exception {
            DtoManagementStatus dto = new DtoManagementStatus();
            dto.setName("Updated");
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(managementRepo.findByFkIdManagementStatusIdManagementStatus(anyInt())).thenReturn(List.of());
            mvc.perform(put("/api/v1/estado-gestion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/estado-gestion/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            when(managementRepo.findByFkIdManagementStatusIdManagementStatus(1))
                    .thenReturn(List.of(new com.licensis.notaire.business.DeedManagement()));
            mvc.perform(put("/api/v1/estado-gestion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("DELETE should return 200, 404, or 409 appropriately")
        void deleteOne() throws Exception {
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(managementRepo.findByFkIdManagementStatusIdManagementStatus(anyInt())).thenReturn(List.of());
            mvc.perform(delete("/api/v1/estado-gestion/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/estado-gestion/2")).andExpect(status().isNotFound());
            when(managementRepo.findByFkIdManagementStatusIdManagementStatus(1))
                    .thenReturn(List.of(new com.licensis.notaire.business.DeedManagement()));
            mvc.perform(delete("/api/v1/estado-gestion/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("HistorialController")
    class HistoryControllerTests {
        private final HistoryRepository repo = mock(HistoryRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new HistoryController(repo)).build();

        @Test
        @DisplayName("GET all and by id and by gestion should work")
        void getEndpoints() throws Exception {
            History h = new History();
            h.setIdHistory(1);
            when(repo.findAll()).thenReturn(List.of(h));
            when(repo.findById(1)).thenReturn(Optional.of(h));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.findByFkIdManagementIdManagement(10)).thenReturn(List.of(h));
            mvc.perform(get("/api/v1/historial")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/historial/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/historial/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/historial/gestion/10")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST/PUT/DELETE should cover happy and error paths")
        void writeEndpoints() throws Exception {
            History h = new History();
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            mvc.perform(post("/api/v1/historial").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/historial/1").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/historial/2").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isNotFound());

            History toDelete = new History();
            com.licensis.notaire.business.ManagementStatus status =
                    new com.licensis.notaire.business.ManagementStatus();
            status.setHistoryList(new java.util.HashSet<>(List.of(toDelete)));
            toDelete.setFkIdManagementStatus(status);
            when(repo.findById(1)).thenReturn(Optional.of(toDelete));
            when(repo.findById(2)).thenReturn(Optional.empty());
            mvc.perform(delete("/api/v1/historial/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/historial/2")).andExpect(status().isNotFound());

            when(repo.save(any(History.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/historial").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/historial/1").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isInternalServerError());

            doThrow(new RuntimeException("fk")).when(repo).delete(toDelete);
            mvc.perform(delete("/api/v1/historial/1")).andExpect(status().isConflict());
        }
    }

    // ItemController tests moved to dedicated ItemControllerTest (Issue #822),
    // since ItemController now depends on ItemService rather than ItemRepository.

    @Nested
    @DisplayName("MovimientoTestimonioController")
    class TestimonyMovementControllerTests {
        private final TestimonyMovementRepository repo = mock(TestimonyMovementRepository.class);
        private final TestimonyMovementService testimonyMovementService = mock(TestimonyMovementService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TestimonyMovementController(repo, testimonyMovementService)).build();

        private TestimonyMovement build() {
            TestimonyMovement m = new TestimonyMovement();
            m.setIdTestimonyMovement(1);
            Testimony t = new Testimony();
            t.setIdTestimony(1);
            m.setTestimony(t);
            return m;
        }

        @Test
        @DisplayName("GET endpoints should work")
        void getEndpoints() throws Exception {
            when(repo.findAll()).thenReturn(List.of(build()));
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/movimiento-testimonio")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/movimiento-testimonio/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/movimiento-testimonio/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST/PUT/DELETE happy and error paths")
        void writeEndpoints() throws Exception {
            DtoTestimonyMovement dto = new DtoTestimonyMovement();
            dto.setIdTestimonyMovement(1);
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            when(repo.save(any(TestimonyMovement.class))).thenReturn(build());
            mvc.perform(post("/api/v1/movimiento-testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/movimiento-testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/movimiento-testimonio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/movimiento-testimonio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/movimiento-testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(TestimonyMovement.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/movimiento-testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/movimiento-testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());

            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/movimiento-testimonio/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("EscrituraController")
    class DeedControllerTests {
        private final DeedService service = mock(DeedService.class);
        private final DeedFirmaService firmaService = mock(DeedFirmaService.class);
        private final com.licensis.notaire.repository.FolioRepository folioRepository =
                mock(com.licensis.notaire.repository.FolioRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new DeedController(service, firmaService, folioRepository))
                        .setControllerAdvice(new com.licensis.notaire.config.GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Should cover all paths")
        void allPaths() throws Exception {
            Deed e = new Deed();
            e.setIdDeed(1);
            when(service.findAllPaged(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e), PageRequest.of(0, 20), 1));
            when(service.findById(1)).thenReturn(Optional.of(e));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.findEscribanosDisponibles()).thenReturn(List.of());
            when(service.searchPorNumber(any())).thenReturn(List.of(e));
            when(service.save(any(Deed.class))).thenReturn(e);

            mvc.perform(get("/api/v1/escrituras")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/escrituras/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/escrituras/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/escrituras/escribanos-disponibles")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/escrituras/buscar?numero=10")).andExpect(status().isOk());

            mvc.perform(post("/api/v1/escrituras").contentType("application/json")
                    .content(mapper.writeValueAsString(e))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/escrituras/1").contentType("application/json")
                    .content(mapper.writeValueAsString(e))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/escrituras/2").contentType("application/json")
                    .content(mapper.writeValueAsString(e))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/escrituras/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/escrituras/2")).andExpect(status().isNotFound());

            when(service.save(any(Deed.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/escrituras").contentType("application/json")
                    .content(mapper.writeValueAsString(e))).andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PresupuestoController")
    class BudgetControllerTests {
        private final BudgetService service = mock(BudgetService.class);
        private final com.licensis.notaire.service.BudgetResumenService budgetResumenService =
                mock(com.licensis.notaire.service.BudgetResumenService.class);
        private final com.licensis.notaire.service.BudgetTemplateService budgetTemplateService =
                mock(com.licensis.notaire.service.BudgetTemplateService.class);
        private final com.licensis.notaire.service.BudgetCatalogoItemsService budgetCatalogoItemsService =
                mock(com.licensis.notaire.service.BudgetCatalogoItemsService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new BudgetController(service, budgetResumenService,
                        budgetTemplateService, budgetCatalogoItemsService))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Should cover all paths")
        void allPaths() throws Exception {
            Budget p = new Budget();
            p.setIdBudget(1);
            when(service.findAllPaged(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1));
            when(service.findById(1)).thenReturn(Optional.of(p));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.findByPerson(5)).thenReturn(List.of(p));
            when(service.findByStatus(any())).thenReturn(List.of(p));
            when(service.create(any(Budget.class))).thenReturn(p);
            when(service.update(any(Integer.class), any(Budget.class))).thenReturn(p);

            mvc.perform(get("/api/v1/presupuestos")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/presupuestos/persona/5")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/buscar?estado=activo")).andExpect(status().isOk());

            mvc.perform(post("/api/v1/presupuestos").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/presupuestos/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isOk());

            when(service.update(any(Integer.class), any(Budget.class)))
                    .thenThrow(new ResourceNotFoundException("not found"));
            mvc.perform(put("/api/v1/presupuestos/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isNotFound());

            mvc.perform(delete("/api/v1/presupuestos/1")).andExpect(status().isNoContent());
            doThrow(new ResourceNotFoundException("not found")).when(service).deleteById(99);
            mvc.perform(delete("/api/v1/presupuestos/99")).andExpect(status().isNotFound());

            com.licensis.notaire.business.Item item = new com.licensis.notaire.business.Item(1, "Sellado", 500f);
            when(budgetTemplateService.cargarItemsDesdeTemplate(1, 5)).thenReturn(List.of(item));
            mvc.perform(post("/api/v1/presupuestos/1/items-desde-plantilla?tipoTramiteId=5"))
                    .andExpect(status().isOk());

            when(budgetCatalogoItemsService.agregarItemsDesdeCatalogo(eq(1), anyList()))
                    .thenReturn(List.of(item));
            mvc.perform(post("/api/v1/presupuestos/1/items-desde-catalogo").contentType("application/json")
                    .content(mapper.writeValueAsString(List.of(1))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("TestimonioController")
    class TestimonyControllerTests {
        private final TestimonyRepository repo = mock(TestimonyRepository.class);
        private final TestimonyGenerationVerificacionService generationVerificacionService =
                mock(TestimonyGenerationVerificacionService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TestimonyController(repo, generationVerificacionService)).build();

        private Testimony build() {
            Testimony t = new Testimony();
            t.setIdTestimony(1);
            return t;
        }

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            DtoTestimony dto = new DtoTestimony();
            dto.setIdTestimony(1);
            when(repo.findAll()).thenReturn(List.of(build()));
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/testimonio")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonio/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(Testimony.class))).thenReturn(build());
            mvc.perform(post("/api/v1/testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/testimonio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/testimonio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(Testimony.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/testimonio/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoDeDocumentoController")
    class DocumentTypeControllerTests {
        private final DocumentTypeRepository repo = mock(DocumentTypeRepository.class);
        private final com.licensis.notaire.repository.ProcedureTemplateRepository templateRepo =
                mock(com.licensis.notaire.repository.ProcedureTemplateRepository.class);
        private final com.licensis.notaire.repository.SubmittedDocumentRepository docSubmittedRepo =
                mock(com.licensis.notaire.repository.SubmittedDocumentRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new DocumentTypeController(repo, templateRepo, docSubmittedRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            DocumentType t = new DocumentType();
            t.setIdDocumentType(1);
            t.setName("DNI");
            DtoDocumentType dto = new DtoDocumentType();
            dto.setIdDocumentType(1);
            dto.setName("DNI");
            dto.setExpires(false);
            dto.setEnabled(true);

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(templateRepo.findByDocumentTypeIdDocumentType(anyInt())).thenReturn(List.of());
            when(docSubmittedRepo.existsByFkIdDocumentType(anyInt())).thenReturn(false);

            mvc.perform(get("/api/v1/tipo-de-documento")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-de-documento/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-de-documento/2")).andExpect(status().isNotFound());

            when(repo.save(any(DocumentType.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-de-documento").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-de-documento/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-de-documento/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-de-documento/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/tipo-de-documento/2")).andExpect(status().isNotFound());

            when(repo.save(any(DocumentType.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-de-documento").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/tipo-de-documento/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());

            when(templateRepo.findByDocumentTypeIdDocumentType(1)).thenReturn(List.of(new com.licensis.notaire.business.ProcedureTemplate()));
            mvc.perform(delete("/api/v1/tipo-de-documento/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoDeFolioController")
    class FolioTypeControllerTests {
        private final FolioTypeRepository repo = mock(FolioTypeRepository.class);
        private final com.licensis.notaire.repository.FolioRepository folioRepo =
                mock(com.licensis.notaire.repository.FolioRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new FolioTypeController(repo, folioRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            FolioType t = new FolioType();
            t.setIdFolioType(1);
            t.setName("Protocolo");
            DtoFolioType dto = new DtoFolioType();
            dto.setIdFolioType(1);
            dto.setName("Protocolo");

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/tipo-folio")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-folio/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-folio/2")).andExpect(status().isNotFound());

            when(repo.save(any(FolioType.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-folio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-folio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-folio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-folio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/tipo-folio/2")).andExpect(status().isNotFound());

            when(repo.save(any(FolioType.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-folio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/tipo-folio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/tipo-folio/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoDeTramiteController")
    class ProcedureTypeControllerTests {
        private final ProcedureTypeRepository repo = mock(ProcedureTypeRepository.class);
        private final com.licensis.notaire.repository.ProcedureTemplateRepository templateRepo =
                mock(com.licensis.notaire.repository.ProcedureTemplateRepository.class);
        private final com.licensis.notaire.repository.BudgetTemplateRepository budgetRepo =
                mock(com.licensis.notaire.repository.BudgetTemplateRepository.class);
        private final com.licensis.notaire.repository.ProcedureRepository procedureRepo =
                mock(com.licensis.notaire.repository.ProcedureRepository.class);
        private final com.licensis.notaire.repository.WorkflowDefinitionRepository workflowRepo =
                mock(com.licensis.notaire.repository.WorkflowDefinitionRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new ProcedureTypeController(repo, budgetRepo, procedureRepo, templateRepo, workflowRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            ProcedureType t = new ProcedureType();
            t.setIdProcedureType(1);
            t.setName("Compraventa");
            DtoProcedureType dto = new DtoProcedureType();
            dto.setIdProcedureType(1);
            dto.setName("Compraventa");
            dto.setIsArchived(false);
            dto.setIsRegistered(false);
            dto.setAssociatesProperties(false);
            dto.setEnabled(true);
            dto.setVersion(0);

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(templateRepo.findByProcedureTypeIdProcedureType(anyInt())).thenReturn(List.of());
            when(budgetRepo.findByProcedureTypeIdProcedureType(anyInt())).thenReturn(List.of());
            when(procedureRepo.findByFkIdProcedureTypeIdProcedureType(anyInt())).thenReturn(List.of());

            mvc.perform(get("/api/v1/tipo-tramite")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-tramite/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-tramite/2")).andExpect(status().isNotFound());

            when(repo.save(any(ProcedureType.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-tramite").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-tramite/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-tramite/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-tramite/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/tipo-tramite/2")).andExpect(status().isNotFound());

            when(repo.save(any(ProcedureType.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-tramite").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/tipo-tramite/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            when(templateRepo.findByProcedureTypeIdProcedureType(1)).thenReturn(List.of(new com.licensis.notaire.business.ProcedureTemplate()));
            mvc.perform(delete("/api/v1/tipo-tramite/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoIdentificacionController")
    class IdentificationTypeControllerTests {
        private final IdentificationTypeRepository repo = mock(IdentificationTypeRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new IdentificationTypeController(repo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            IdentificationType t = new IdentificationType();
            t.setIdIdentificationType(1);
            t.setName("DNI");

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/tipo-identificacion")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-identificacion/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-identificacion/2")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/tipo-identificacion").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-identificacion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-identificacion/2").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-identificacion/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/tipo-identificacion/2")).andExpect(status().isNotFound());

            when(repo.save(any(IdentificationType.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-identificacion").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/tipo-identificacion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/tipo-identificacion/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TramiteController")
    class ProcedureControllerTests {
        private final ProcedureRepository repo = mock(ProcedureRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new ProcedureController(repo))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            Procedure t = new Procedure();
            t.setIdProcedure(1);
            when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 20), 1));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/tramites")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tramites/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tramites/2")).andExpect(status().isNotFound());

            mvc.perform(post("/api/v1/tramites").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tramites/1").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tramites/2").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tramites/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/tramites/2")).andExpect(status().isNotFound());

            when(repo.save(any(Procedure.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tramites").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/tramites/1").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/tramites/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PersonController")
    class PersonControllerTests {
        private final PersonService service = mock(PersonService.class);
        private final IdentificationTypeRepository typeRepo = mock(IdentificationTypeRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new PersonController(service, typeRepo)).build();

        @Test
        @DisplayName("Cover all paths including search and default type identificacion")
        void all() throws Exception {
            Person p = new Person();
            p.setPersonId(1);
            p.setFirstName("Juan");
            p.setLastName("Perez");
            p.setIdentificationNumber("12345678");
            IdentificationType type = new IdentificationType(1, "DNI");
            p.setFkIdIdentificationType(type);

            when(service.findAll()).thenReturn(List.of(p));
            when(service.findById(1)).thenReturn(Optional.of(p));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.save(any(Person.class))).thenReturn(p);
            when(service.search(any(), any(), any(), any(), any())).thenReturn(List.of(p));
            when(typeRepo.findById(1)).thenReturn(Optional.of(type));

            mvc.perform(get("/api/v1/people")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/people/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/people/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/people/search?firstName=Juan")).andExpect(status().isOk());

            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/people/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/people/2").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/people/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/people/2")).andExpect(status().isNotFound());

            // POST with missing tipo identificacion should use default
            Person person2 = new Person();
            person2.setPersonId(2);
            person2.setFirstName("Ana");
            person2.setLastName("Gomez");
            person2.setIdentificationNumber("87654321");
            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(mapper.writeValueAsString(person2))).andExpect(status().isCreated());

            // POST when save fails
            when(service.save(any(Person.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST should create default type identificacion when missing in DB")
        void postShouldCreateDefaultType() throws Exception {
            Person p = new Person();
            p.setFirstName("Juan");
            p.setLastName("Perez");
            p.setIdentificationNumber("12345678");
            when(typeRepo.findById(1)).thenReturn(Optional.empty());
            IdentificationType created = new IdentificationType();
            created.setIdIdentificationType(1);
            created.setName("DNI");
            when(typeRepo.save(any(IdentificationType.class))).thenReturn(created);
            when(service.save(any(Person.class))).thenReturn(p);

            mvc.perform(post("/api/v1/people").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            verify(typeRepo).save(any(IdentificationType.class));
        }
    }
}
