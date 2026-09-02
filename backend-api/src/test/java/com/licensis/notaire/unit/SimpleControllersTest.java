package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.CopiaController;
import com.licensis.notaire.api.EscrituraController;
import com.licensis.notaire.api.EstadoDeGestionController;
import com.licensis.notaire.api.HistorialController;
import com.licensis.notaire.api.MovimientoTestimonioController;
import com.licensis.notaire.api.PersonaController;
import com.licensis.notaire.api.PresupuestoController;
import com.licensis.notaire.api.TestimonioController;
import com.licensis.notaire.api.TipoDeDocumentoController;
import com.licensis.notaire.api.TipoDeFolioController;
import com.licensis.notaire.api.TipoDeTramiteController;
import com.licensis.notaire.api.TipoIdentificacionController;
import com.licensis.notaire.api.TramiteController;
import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.CopiaRepository;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.EscrituraFirmaService;
import com.licensis.notaire.service.EscrituraService;
import com.licensis.notaire.service.MovimientoTestimonioService;
import com.licensis.notaire.service.PersonaService;
import com.licensis.notaire.service.PresupuestoService;
import com.licensis.notaire.service.TestimonioGeneracionVerificacionService;
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
    class CopiaControllerTests {
        private final CopiaRepository repo = mock(CopiaRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc = standaloneSetup(new CopiaController(repo)).build();

        @Test
        @DisplayName("GET all should return 200")
        void getAll() throws Exception {
            Copia c = new Copia();
            c.setIdCopia(1);
            when(repo.findAll()).thenReturn(List.of(c));
            mvc.perform(get("/api/v1/copia")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET by id should return 200 when found and 404 when not")
        void getById() throws Exception {
            Copia c = new Copia();
            c.setIdCopia(1);
            when(repo.findById(1)).thenReturn(Optional.of(c));
            when(repo.findById(2)).thenReturn(Optional.empty());
            mvc.perform(get("/api/v1/copia/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/copia/2")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST should return 201 on success and 500 on failure")
        void create() throws Exception {
            Copia c = new Copia();
            mvc.perform(post("/api/v1/copia").contentType("application/json")
                            .content(mapper.writeValueAsString(c)))
                    .andExpect(status().isCreated());
            when(repo.save(any(Copia.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/copia").contentType("application/json")
                            .content(mapper.writeValueAsString(c)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("PUT should return 200 when present, 404 when missing, 500 on failure")
        void update() throws Exception {
            Copia c = new Copia();
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            mvc.perform(put("/api/v1/copia/1").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/copia/2").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isNotFound());
            when(repo.save(any(Copia.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(put("/api/v1/copia/1").contentType("application/json")
                    .content(mapper.writeValueAsString(c))).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("DELETE should return 200 when present, 404 when not, 409 on failure")
        void deleteCopia() throws Exception {
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
    class EstadoDeGestionControllerTests {
        private final EstadoDeGestionRepository repo = mock(EstadoDeGestionRepository.class);
        private final GestionDeEscrituraRepository gestionRepo = mock(GestionDeEscrituraRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new EstadoDeGestionController(repo, gestionRepo)).build();

        private EstadoDeGestion build() {
            EstadoDeGestion e = new EstadoDeGestion(1, "Activo");
            return e;
        }

        @Test
        @DisplayName("GET all should return list of DTOs")
        void getAll() throws Exception {
            when(repo.findAll()).thenReturn(List.of(build()));
            mvc.perform(get("/api/v1/estado-gestion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idEstadoGestion").value(1));
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
            DtoEstadoDeGestion dto = new DtoEstadoDeGestion();
            dto.setIdEstadoGestion(1);
            dto.setNombre("Activo");
            when(repo.save(any(EstadoDeGestion.class))).thenReturn(build());
            mvc.perform(post("/api/v1/estado-gestion").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            when(repo.save(any(EstadoDeGestion.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/estado-gestion").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT should return 200 when present and not in-use, 404 when missing, 409 when in-use")
        void update() throws Exception {
            DtoEstadoDeGestion dto = new DtoEstadoDeGestion();
            dto.setNombre("Updated");
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(gestionRepo.findByFkIdEstadoDeGestionIdEstadoGestion(anyInt())).thenReturn(List.of());
            mvc.perform(put("/api/v1/estado-gestion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/estado-gestion/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            when(gestionRepo.findByFkIdEstadoDeGestionIdEstadoGestion(1))
                    .thenReturn(List.of(new com.licensis.notaire.negocio.GestionDeEscritura()));
            mvc.perform(put("/api/v1/estado-gestion/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("DELETE should return 200, 404, or 409 appropriately")
        void deleteOne() throws Exception {
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(gestionRepo.findByFkIdEstadoDeGestionIdEstadoGestion(anyInt())).thenReturn(List.of());
            mvc.perform(delete("/api/v1/estado-gestion/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/estado-gestion/2")).andExpect(status().isNotFound());
            when(gestionRepo.findByFkIdEstadoDeGestionIdEstadoGestion(1))
                    .thenReturn(List.of(new com.licensis.notaire.negocio.GestionDeEscritura()));
            mvc.perform(delete("/api/v1/estado-gestion/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("HistorialController")
    class HistorialControllerTests {
        private final HistorialRepository repo = mock(HistorialRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new HistorialController(repo)).build();

        @Test
        @DisplayName("GET all and by id and by gestion should work")
        void getEndpoints() throws Exception {
            Historial h = new Historial();
            h.setIdHistorial(1);
            when(repo.findAll()).thenReturn(List.of(h));
            when(repo.findById(1)).thenReturn(Optional.of(h));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.findByFkIdGestionIdGestion(10)).thenReturn(List.of(h));
            mvc.perform(get("/api/v1/historial")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/historial/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/historial/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/historial/gestion/10")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST/PUT/DELETE should cover happy and error paths")
        void writeEndpoints() throws Exception {
            Historial h = new Historial();
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            mvc.perform(post("/api/v1/historial").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/historial/1").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/historial/2").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/historial/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/historial/2")).andExpect(status().isNotFound());

            when(repo.save(any(Historial.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/historial").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/historial/1").contentType("application/json")
                    .content(mapper.writeValueAsString(h))).andExpect(status().isInternalServerError());

            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/historial/1")).andExpect(status().isConflict());
        }
    }

    // ItemController tests moved to dedicated ItemControllerTest (Issue #822),
    // since ItemController now depends on ItemService rather than ItemRepository.

    @Nested
    @DisplayName("MovimientoTestimonioController")
    class MovimientoTestimonioControllerTests {
        private final MovimientoTestimonioRepository repo = mock(MovimientoTestimonioRepository.class);
        private final MovimientoTestimonioService movimientoTestimonioService = mock(MovimientoTestimonioService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new MovimientoTestimonioController(repo, movimientoTestimonioService)).build();

        private MovimientoTestimonio build() {
            MovimientoTestimonio m = new MovimientoTestimonio();
            m.setIdMovimientoTestimonio(1);
            Testimonio t = new Testimonio();
            t.setIdTestimonio(1);
            m.setTestimonio(t);
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
            DtoMovimientoTestimonio dto = new DtoMovimientoTestimonio();
            dto.setIdMovimientoTestimonio(1);
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            when(repo.save(any(MovimientoTestimonio.class))).thenReturn(build());
            mvc.perform(post("/api/v1/movimiento-testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/movimiento-testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/movimiento-testimonio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/movimiento-testimonio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/movimiento-testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(MovimientoTestimonio.class))).thenThrow(new RuntimeException("x"));
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
    class EscrituraControllerTests {
        private final EscrituraService service = mock(EscrituraService.class);
        private final EscrituraFirmaService firmaService = mock(EscrituraFirmaService.class);
        private final com.licensis.notaire.repository.FolioRepository folioRepository =
                mock(com.licensis.notaire.repository.FolioRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new EscrituraController(service, firmaService, folioRepository))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Should cover all paths")
        void allPaths() throws Exception {
            Escritura e = new Escritura();
            e.setIdEscritura(1);
            when(service.findAllPaged(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e), PageRequest.of(0, 20), 1));
            when(service.findById(1)).thenReturn(Optional.of(e));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.findEscribanosDisponibles()).thenReturn(List.of());
            when(service.buscarPorNumero(any())).thenReturn(List.of(e));
            when(service.save(any(Escritura.class))).thenReturn(e);

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

            when(service.save(any(Escritura.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/escrituras").contentType("application/json")
                    .content(mapper.writeValueAsString(e))).andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PresupuestoController")
    class PresupuestoControllerTests {
        private final PresupuestoService service = mock(PresupuestoService.class);
        private final com.licensis.notaire.service.PresupuestoResumenService presupuestoResumenService =
                mock(com.licensis.notaire.service.PresupuestoResumenService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new PresupuestoController(service, presupuestoResumenService))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Should cover all paths")
        void allPaths() throws Exception {
            Presupuesto p = new Presupuesto();
            p.setIdPresupuesto(1);
            when(service.findAllPaged(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1));
            when(service.findById(1)).thenReturn(Optional.of(p));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.findByPersona(5)).thenReturn(List.of(p));
            when(service.findByEstado(any())).thenReturn(List.of(p));
            when(service.create(any(Presupuesto.class))).thenReturn(p);
            when(service.update(any(Integer.class), any(Presupuesto.class))).thenReturn(p);

            mvc.perform(get("/api/v1/presupuestos")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/presupuestos/persona/5")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/presupuestos/buscar?estado=activo")).andExpect(status().isOk());

            mvc.perform(post("/api/v1/presupuestos").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/presupuestos/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isOk());

            when(service.update(any(Integer.class), any(Presupuesto.class)))
                    .thenThrow(new ResourceNotFoundException("not found"));
            mvc.perform(put("/api/v1/presupuestos/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isNotFound());

            mvc.perform(delete("/api/v1/presupuestos/1")).andExpect(status().isNoContent());
            doThrow(new ResourceNotFoundException("not found")).when(service).deleteById(99);
            mvc.perform(delete("/api/v1/presupuestos/99")).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("TestimonioController")
    class TestimonioControllerTests {
        private final TestimonioRepository repo = mock(TestimonioRepository.class);
        private final TestimonioGeneracionVerificacionService generacionVerificacionService =
                mock(TestimonioGeneracionVerificacionService.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TestimonioController(repo, generacionVerificacionService)).build();

        private Testimonio build() {
            Testimonio t = new Testimonio();
            t.setIdTestimonio(1);
            return t;
        }

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            DtoTestimonio dto = new DtoTestimonio();
            dto.setIdTestimonio(1);
            when(repo.findAll()).thenReturn(List.of(build()));
            when(repo.findById(1)).thenReturn(Optional.of(build()));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/testimonio")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonio/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(Testimonio.class))).thenReturn(build());
            mvc.perform(post("/api/v1/testimonio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/testimonio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/testimonio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/testimonio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/testimonio/2")).andExpect(status().isNotFound());

            when(repo.save(any(Testimonio.class))).thenThrow(new RuntimeException("x"));
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
    class TipoDeDocumentoControllerTests {
        private final TipoDeDocumentoRepository repo = mock(TipoDeDocumentoRepository.class);
        private final com.licensis.notaire.repository.PlantillaTramiteRepository plantillaRepo =
                mock(com.licensis.notaire.repository.PlantillaTramiteRepository.class);
        private final com.licensis.notaire.repository.DocumentoPresentadoRepository docPresentadoRepo =
                mock(com.licensis.notaire.repository.DocumentoPresentadoRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TipoDeDocumentoController(repo, plantillaRepo, docPresentadoRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            TipoDeDocumento t = new TipoDeDocumento();
            t.setIdTipoDocumento(1);
            t.setNombre("DNI");
            DtoTipoDeDocumento dto = new DtoTipoDeDocumento();
            dto.setIdTipoDocumento(1);
            dto.setNombre("DNI");
            dto.setVence(false);
            dto.setHabilitado(true);

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(plantillaRepo.findByTipoDeDocumentoIdTipoDocumento(anyInt())).thenReturn(List.of());
            when(docPresentadoRepo.existsByFkIdTipoDocumento(anyInt())).thenReturn(false);

            mvc.perform(get("/api/v1/tipo-de-documento")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-de-documento/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-de-documento/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeDocumento.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-de-documento").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-de-documento/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-de-documento/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-de-documento/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/tipo-de-documento/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeDocumento.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-de-documento").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/tipo-de-documento/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());

            when(plantillaRepo.findByTipoDeDocumentoIdTipoDocumento(1)).thenReturn(List.of(new com.licensis.notaire.negocio.PlantillaTramite()));
            mvc.perform(delete("/api/v1/tipo-de-documento/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoDeFolioController")
    class TipoDeFolioControllerTests {
        private final TipoDeFolioRepository repo = mock(TipoDeFolioRepository.class);
        private final com.licensis.notaire.repository.FolioRepository folioRepo =
                mock(com.licensis.notaire.repository.FolioRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TipoDeFolioController(repo, folioRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            TipoDeFolio t = new TipoDeFolio();
            t.setIdTipoFolio(1);
            t.setNombre("Protocolo");
            DtoTipoDeFolio dto = new DtoTipoDeFolio();
            dto.setIdTipoFolio(1);
            dto.setNombre("Protocolo");

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/tipo-folio")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-folio/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-folio/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeFolio.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-folio").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-folio/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-folio/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-folio/1")).andExpect(status().isOk());
            mvc.perform(delete("/api/v1/tipo-folio/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeFolio.class))).thenThrow(new RuntimeException("x"));
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
    class TipoDeTramiteControllerTests {
        private final TipoDeTramiteRepository repo = mock(TipoDeTramiteRepository.class);
        private final com.licensis.notaire.repository.PlantillaTramiteRepository plantillaRepo =
                mock(com.licensis.notaire.repository.PlantillaTramiteRepository.class);
        private final com.licensis.notaire.repository.PlantillaPresupuestoRepository presupuestoRepo =
                mock(com.licensis.notaire.repository.PlantillaPresupuestoRepository.class);
        private final com.licensis.notaire.repository.TramiteRepository tramiteRepo =
                mock(com.licensis.notaire.repository.TramiteRepository.class);
        private final com.licensis.notaire.repository.WorkflowDefinitionRepository workflowRepo =
                mock(com.licensis.notaire.repository.WorkflowDefinitionRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TipoDeTramiteController(repo, presupuestoRepo, tramiteRepo, plantillaRepo, workflowRepo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            TipoDeTramite t = new TipoDeTramite();
            t.setIdTipoTramite(1);
            t.setNombre("Compraventa");
            DtoTipoDeTramite dto = new DtoTipoDeTramite();
            dto.setIdTipoTramite(1);
            dto.setNombre("Compraventa");
            dto.setSeArchiva(false);
            dto.setSeInscribe(false);
            dto.setAsociaInmuebles(false);
            dto.setHabilitado(true);
            dto.setVersion(0);

            when(repo.findAll()).thenReturn(List.of(t));
            when(repo.findById(1)).thenReturn(Optional.of(t));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            when(plantillaRepo.findByTipoDeTramiteIdTipoTramite(anyInt())).thenReturn(List.of());
            when(presupuestoRepo.findByTipoDeTramiteIdTipoTramite(anyInt())).thenReturn(List.of());
            when(tramiteRepo.findByFkIdTipoTramiteIdTipoTramite(anyInt())).thenReturn(List.of());

            mvc.perform(get("/api/v1/tipo-tramite")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-tramite/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/tipo-tramite/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeTramite.class))).thenReturn(t);
            mvc.perform(post("/api/v1/tipo-tramite").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/tipo-tramite/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/tipo-tramite/2").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/tipo-tramite/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/tipo-tramite/2")).andExpect(status().isNotFound());

            when(repo.save(any(TipoDeTramite.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tipo-tramite").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isConflict());
            mvc.perform(put("/api/v1/tipo-tramite/1").contentType("application/json")
                    .content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
            when(plantillaRepo.findByTipoDeTramiteIdTipoTramite(1)).thenReturn(List.of(new com.licensis.notaire.negocio.PlantillaTramite()));
            mvc.perform(delete("/api/v1/tipo-tramite/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("TipoIdentificacionController")
    class TipoIdentificacionControllerTests {
        private final TipoIdentificacionRepository repo = mock(TipoIdentificacionRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TipoIdentificacionController(repo)).build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            TipoIdentificacion t = new TipoIdentificacion();
            t.setIdTipoIdentificacion(1);
            t.setNombre("DNI");

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

            when(repo.save(any(TipoIdentificacion.class))).thenThrow(new RuntimeException("x"));
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
    class TramiteControllerTests {
        private final TramiteRepository repo = mock(TramiteRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new TramiteController(repo))
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        @Test
        @DisplayName("Cover all paths")
        void all() throws Exception {
            Tramite t = new Tramite();
            t.setIdTramite(1);
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

            when(repo.save(any(Tramite.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/tramites").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/tramites/1").contentType("application/json")
                    .content(mapper.writeValueAsString(t))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/tramites/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PersonaController")
    class PersonaControllerTests {
        private final PersonaService service = mock(PersonaService.class);
        private final TipoIdentificacionRepository tipoRepo = mock(TipoIdentificacionRepository.class);
        private final org.springframework.test.web.servlet.MockMvc mvc =
                standaloneSetup(new PersonaController(service, tipoRepo)).build();

        @Test
        @DisplayName("Cover all paths including buscar and default tipo identificacion")
        void all() throws Exception {
            Persona p = new Persona();
            p.setIdPersona(1);
            p.setNombre("Juan");
            p.setApellido("Perez");
            p.setNumeroIdentificacion("12345678");
            TipoIdentificacion tipo = new TipoIdentificacion(1, "DNI");
            p.setFkIdTipoIdentificacion(tipo);

            when(service.findAll()).thenReturn(List.of(p));
            when(service.findById(1)).thenReturn(Optional.of(p));
            when(service.findById(2)).thenReturn(Optional.empty());
            when(service.save(any(Persona.class))).thenReturn(p);
            when(service.buscar(any(), any(), any(), any(), any())).thenReturn(List.of(p));
            when(tipoRepo.findById(1)).thenReturn(Optional.of(tipo));

            mvc.perform(get("/api/v1/personas")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/personas/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/personas/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/personas/buscar?nombre=Juan")).andExpect(status().isOk());

            mvc.perform(post("/api/v1/personas").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            mvc.perform(put("/api/v1/personas/1").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isOk());
            mvc.perform(put("/api/v1/personas/2").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/v1/personas/1")).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/personas/2")).andExpect(status().isNotFound());

            // POST with missing tipo identificacion should use default
            Persona persona2 = new Persona();
            persona2.setIdPersona(2);
            persona2.setNombre("Ana");
            persona2.setApellido("Gomez");
            persona2.setNumeroIdentificacion("87654321");
            mvc.perform(post("/api/v1/personas").contentType("application/json")
                    .content(mapper.writeValueAsString(persona2))).andExpect(status().isCreated());

            // POST when save fails
            when(service.save(any(Persona.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/personas").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST should create default tipo identificacion when missing in DB")
        void postShouldCreateDefaultTipo() throws Exception {
            Persona p = new Persona();
            p.setNombre("Juan");
            p.setApellido("Perez");
            p.setNumeroIdentificacion("12345678");
            when(tipoRepo.findById(1)).thenReturn(Optional.empty());
            TipoIdentificacion created = new TipoIdentificacion();
            created.setIdTipoIdentificacion(1);
            created.setNombre("DNI");
            when(tipoRepo.save(any(TipoIdentificacion.class))).thenReturn(created);
            when(service.save(any(Persona.class))).thenReturn(p);

            mvc.perform(post("/api/v1/personas").contentType("application/json")
                    .content(mapper.writeValueAsString(p))).andExpect(status().isCreated());
            verify(tipoRepo).save(any(TipoIdentificacion.class));
        }
    }
}
