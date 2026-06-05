package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.GestionController;
import com.licensis.notaire.api.PlantillaTramiteController;
import com.licensis.notaire.api.ReporteController;
import com.licensis.notaire.api.UsuarioController;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import com.licensis.notaire.servicios.ReporteService;
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
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("Additional controllers unit tests")
class AdditionalControllersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("PlantillaTramiteController")
    class PlantillaTramiteControllerTests {

        @Test
        @DisplayName("getAll and getByTipoTramite endpoints")
        void all() throws Exception {
            PlantillaTramiteRepository repo = mock(PlantillaTramiteRepository.class);
            var mvc = standaloneSetup(new PlantillaTramiteController(repo)).build();
            when(repo.findAll()).thenReturn(List.of(new PlantillaTramite()));
            when(repo.findByTipoDeTramiteIdTipoTramite(anyInt())).thenReturn(List.of(new PlantillaTramite()));

            mvc.perform(get("/api/v1/plantilla-tramite")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/plantilla-tramite/tipo-tramite/1")).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("ReporteController")
    class ReporteControllerTests {

        @Test
        @DisplayName("All endpoints — success and failure paths")
        void all() throws Exception {
            ReporteService service = mock(ReporteService.class);
            var mvc = standaloneSetup(new ReporteController(service)).build();
            byte[] pdf = "PDF".getBytes();
            when(service.generarReportePresupuesto(anyInt())).thenReturn(pdf);
            when(service.generarReportePresupuestoInmuebles(anyInt())).thenReturn(pdf);
            when(service.generarReporteListaDocumentosTramite(any())).thenReturn(pdf);
            when(service.generarReporteHistorialGestion(anyInt())).thenReturn(pdf);
            when(service.generarReporteDocumentosPorVencer(anyInt())).thenReturn(pdf);
            when(service.generarReporteConsultarDeudaDocumentos(anyInt())).thenReturn(pdf);
            when(service.generarReporteLibroIndice(anyInt())).thenReturn(pdf);
            when(service.generarReporteDeclaracionJuradaMensual(anyInt(), anyInt())).thenReturn(pdf);
            when(service.generarReporteDeclaracionJuradaRentas(anyInt(), anyInt())).thenReturn(pdf);

            mvc.perform(get("/api/v1/reportes/presupuesto/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/lista-documentos-tramite?nombreTipoTramite=Compraventa"))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/historial-gestion/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/reportes/consultar-deuda-documentos?numeroGestion=10"))
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
            when(service.generarReportePresupuesto(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReportePresupuestoInmuebles(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteListaDocumentosTramite(any())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteHistorialGestion(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDocumentosPorVencer(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteConsultarDeudaDocumentos(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteLibroIndice(anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDeclaracionJuradaMensual(anyInt(), anyInt())).thenThrow(new RuntimeException("e"));
            when(service.generarReporteDeclaracionJuradaRentas(anyInt(), anyInt())).thenThrow(new RuntimeException("e"));

            mvc.perform(get("/api/v1/reportes/presupuesto/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/lista-documentos-tramite?nombreTipoTramite=Compraventa"))
                    .andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/historial-gestion/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andExpect(status().isInternalServerError());
            mvc.perform(get("/api/v1/reportes/consultar-deuda-documentos?numeroGestion=10"))
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
    class GestionControllerTests {

        @Test
        @DisplayName("All paths")
        void all() throws Exception {
            GestionDeEscrituraRepository repo = mock(GestionDeEscrituraRepository.class);
            HistorialRepository histRepo = mock(HistorialRepository.class);
            var mvc = standaloneSetup(new GestionController(repo, histRepo)).build();

            GestionDeEscritura g = new GestionDeEscritura(1);
            com.licensis.notaire.negocio.Persona escr = new com.licensis.notaire.negocio.Persona();
            escr.setIdPersona(99);
            g.setFkIdPersonaEscribano(escr);
            when(repo.findAll()).thenReturn(List.of(g));
            when(repo.findById(1)).thenReturn(Optional.of(g));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.findByNumero(10)).thenReturn(Optional.of(g));
            when(repo.findByNumero(99)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);
            Historial h = new Historial();
            h.setIdHistorial(1);
            h.setFecha(new Date());
            when(histRepo.findByFkIdGestionIdGestion(1)).thenReturn(List.of(h));
            when(histRepo.findByFkIdGestionIdGestion(2)).thenReturn(List.of());

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

            when(repo.save(any(GestionDeEscritura.class))).thenThrow(new RuntimeException("x"));
            mvc.perform(post("/api/v1/gestiones").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isInternalServerError());
            mvc.perform(put("/api/v1/gestiones/1").contentType("application/json")
                    .content(mapper.writeValueAsString(g))).andExpect(status().isInternalServerError());
            doThrow(new RuntimeException("fk")).when(repo).deleteById(1);
            mvc.perform(delete("/api/v1/gestiones/1")).andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("UsuarioController")
    class UsuarioControllerTests {

        @Test
        @DisplayName("All endpoints")
        void all() throws Exception {
            UsuarioRepository repo = mock(UsuarioRepository.class);
            var mvc = standaloneSetup(new UsuarioController(repo)).build();
            Usuario u = new Usuario(1, "admin", "abc", true, "Escribano");

            when(repo.findAll()).thenReturn(List.of(u));
            when(repo.findById(1)).thenReturn(Optional.of(u));
            when(repo.findById(2)).thenReturn(Optional.empty());
            when(repo.findFirstByFkIdPersonaIdPersona(10)).thenReturn(Optional.of(u));
            when(repo.findFirstByFkIdPersonaIdPersona(99)).thenReturn(Optional.empty());
            when(repo.existsById(1)).thenReturn(true);
            when(repo.existsById(2)).thenReturn(false);

            mvc.perform(get("/api/v1/usuarios")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/1")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/2")).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/usuarios/persona/10")).andExpect(status().isOk());
            mvc.perform(get("/api/v1/usuarios/persona/99")).andExpect(status().isNotFound());

            // create with no password — uses UsuarioRequest format (activo, not estado)
            String noPwdJson = """
                    {"nombre":"user","contrasenia":"","tipo":"Escribano","activo":true}
                    """;
            when(repo.save(any(Usuario.class))).thenReturn(u);
            mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(noPwdJson)).andExpect(status().isCreated());
            // create with password
            String withPwdJson = """
                    {"nombre":"user","contrasenia":"pwd","tipo":"Escribano","activo":true}
                    """;
            mvc.perform(post("/api/v1/usuarios").contentType("application/json")
                    .content(withPwdJson)).andExpect(status().isCreated());

            String updateJson = """
                    {"nombre":"admin","contrasenia":"","tipo":"Escribano","activo":true}
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
            DtoUsuario loginDto = new DtoUsuario();
            loginDto.setNombre("admin");
            loginDto.setContrasenia("admin");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User found, wrong password
            when(repo.findAll()).thenReturn(List.of(u));
            loginDto.setContrasenia("wrong");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User found, valid login - the password 'admin' has known MD5
            // 21232f297a57a5a743894a0e4a801fc3 is MD5("admin")
            u.setContrasenia("21232f297a57a5a743894a0e4a801fc3");
            u.setEstado(true);
            loginDto.setContrasenia("admin");
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // User inactive
            u.setEstado(false);
            mvc.perform(post("/api/v1/usuarios/login").contentType("application/json")
                    .content(mapper.writeValueAsString(loginDto))).andExpect(status().isOk());

            // Failure paths
            when(repo.save(any(Usuario.class))).thenThrow(new RuntimeException("x"));
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
